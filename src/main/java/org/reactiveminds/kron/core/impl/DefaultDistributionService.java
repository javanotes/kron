package org.reactiveminds.kron.core.impl;

import java.util.Date;
import java.util.List;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobEntryFilter;
import org.reactiveminds.kron.core.JobEntryListener;
import org.reactiveminds.kron.core.LeaderElectNotifier;
import org.reactiveminds.kron.core.MessageCallback;
import org.reactiveminds.kron.core.vo.CommandTarget;
import org.reactiveminds.kron.err.OperationNotPermittedException;
import org.reactiveminds.kron.model.JobEntry;
import org.reactiveminds.kron.model.JobEntryRepo;
import org.reactiveminds.kron.model.JobRunEntry;
import org.reactiveminds.kron.model.JobRunEntry.RunState;
import org.reactiveminds.kron.model.NodeInfo;
import org.reactiveminds.kron.utils.JsonMapper;
import org.reactiveminds.kron.utils.NodeInfoComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hazelcast.core.Client;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.internal.ascii.rest.RestValue;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

@Service
class DefaultDistributionService implements DistributionService {

	@Autowired
	private HazelcastInstance hazelcast;
	@Autowired
	private JobEntryRepo jobRepo;
	@Value("${kron.master.workerStatExpirySecs:10}")
	private int nodeinfoExpiry;
	private volatile boolean isElectedLeader;
	@Override
	public boolean isElectedLeader() {
		return isElectedLeader;
	}
	private Observable leaderNotifer = new Observable() {
		@Override
		public void notifyObservers(Object o) {
			setChanged();
			super.notifyObservers(o);
		}
	};
	private Thread electorThread = new Thread("MasterElectorRunner") {
		@Override
		public void run() {
			ILock lock = hazelcast.getLock("LEADER");
			isElectedLeader = lock.tryLock();
			if(!isElectedLeader) {
				lock.lock();
				isElectedLeader = true;
			}
			LOG.info("* Node elected as the Leader *");
			leaderNotifer.notifyObservers("ELECT");
		}
	};
	@Override
	public void tryElectAsLeader() {
		if(!isWorkerNode()) {
			if(!electorThread.isAlive()) {
				electorThread.setDaemon(true);
				electorThread.start();
			}
		}
	}
	
	@Override
	public void registerWorkerChannel(MessageCallback<CommandTarget> callback) {
		if(!isWorkerNode())
			throw new OperationNotPermittedException("Cannot register worker channel in MASTER mode");
		
		ITopic<CommandTarget> channel = hazelcast.getTopic(COMM_CHANNEL);
		channel.addMessageListener(new MessageListener<CommandTarget>() {
			
			@Override
			public void onMessage(Message<CommandTarget> message) {
				callback.onMessage(message.getMessageObject());
			}
		});
		
	}
	@Override
	public void submitWorkerCommand(CommandTarget command) {
		ITopic<CommandTarget> channel = hazelcast.getTopic(COMM_CHANNEL);
		channel.publish(command);
	}
	@Async
	@Override
	public void registerMasterChannel(MessageCallback<String> callback) {
		if(isWorkerNode())
			throw new OperationNotPermittedException("Cannot register master channel in WORKER mode");
		IQueue<String> channel = hazelcast.getQueue(ACK_CHANNEL);
		while(true) {
			try {
				String ack = channel.poll(60, TimeUnit.SECONDS);
				if(ack != null)
					callback.onMessage(ack);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	@Override
	public void submitMasterAck(String ack) {
		IQueue<String> channel = hazelcast.getQueue(ACK_CHANNEL);
		channel.offer(ack);
	}
	
	@Override
	public void registerLeaderCallback(LeaderElectNotifier observer) {
		leaderNotifer.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				observer.onElect(arg.toString());
			}
		});
	}

	@Override
	public String getSelfId() {
		return hazelcast.getLocalEndpoint().getUuid();
	}

	@Override
	public void updateWorkerSystemInfo(NodeInfo info) {
		IMap<String, NodeInfo> map = hazelcast.getMap(NODE_INFO);
		map.set(info.getWorkerId(), info, nodeinfoExpiry, TimeUnit.SECONDS);
	}
	
	@Override
	public NavigableSet<NodeInfo> getWorkerSnapshot() {
		IMap<String, NodeInfo> map = hazelcast.getMap(NODE_INFO);
		TreeSet<NodeInfo> sorted = new TreeSet<>(new NodeInfoComparator());
		map.entrySet().forEach(e -> sorted.add(e.getValue()));
		return sorted;
	}

	@Override
	public void countdownLatch(String name) {
		hazelcast.getCountDownLatch(name).countDown();
	}

	private int connectedClientSize() {
		return hazelcast.getClientService().getConnectedClients().size();
	}
	@Override
	public boolean awaitWorkerLatch(String name, long expiry, TimeUnit unit) {
		ICountDownLatch latch = hazelcast.getCountDownLatch(name);
		boolean set = latch.trySetCount(connectedClientSize());
		if(set) {
			try {
				set = latch.await(expiry, unit);
				return set;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return false;
	}

	@Override
	public List<JobEntry> getJobEntries(JobEntryFilter filter) {
		IMap<String, RestValue> map = hazelcast.getMap(JOB_MASTER);
		//client side filtering, but it should be okay?
		Stream<JobEntry> dbStream = StreamSupport.stream(jobRepo.findAll().spliterator(), false)
		.filter(filter);
		Stream<JobEntry> hzStream = map.entrySet().stream().map(e -> JsonMapper.toJobEntry(e.getValue()))
		.filter(filter);
		return Stream.concat(hzStream, dbStream).distinct().collect(Collectors.toList());
	}

	@Override
	public boolean isWorkerNode() {
		return hazelcast.getLocalEndpoint() instanceof Client;
	}

	private static class RestListener implements EntryAddedListener<String, RestValue>, EntryUpdatedListener<String, RestValue>{

		private RestListener(JobEntryListener listener) {
			super();
			this.listener = listener;
		}

		private final JobEntryListener listener;
		@Override
		public void entryUpdated(EntryEvent<String, RestValue> event) {
			entryAdded(event);
		}

		@Override
		public void entryAdded(EntryEvent<String, RestValue> event) {
			listener.onEntryAdded(JsonMapper.toJobEntry(event.getValue()));
		}
		
	}
	@Override
	public void setJobListener(JobEntryListener listener) {
		if(isWorkerNode())
			throw new OperationNotPermittedException("Worker node cannot be a job listener");
		if(!isElectedLeader())
			throw new OperationNotPermittedException("Master not an elected leader");
		
		hazelcast.getMap(JOB_MASTER).addEntryListener(new RestListener(listener), true);
	}

	@Override
	public long getNextSequence(String key) {
		return hazelcast.getFlakeIdGenerator(key).newId();
	}

	
	@Override
	public void createJobRunEntry(JobRunEntry entry) {
		 IMap<String, JobRunEntry> map = hazelcast.getMap(JOB_RUN);
		 map.set(entry.getDBKey(), entry);
	}

	@Override
	public void updateJobRunStart(String id, long startTime) {
		IMap<String, JobRunEntry> map = hazelcast.getMap(JOB_RUN);
		if(map.containsKey(id)) {
			map.lock(id);
			try {
				JobRunEntry entry = map.get(id);
				entry.setState(RunState.RUNNING);
				entry.setStartTime(new Date(startTime));
				map.set(id, entry);
			}
			finally {
				map.unlock(id);
			}
		}
	}

	@Override
	public void updateJobRunEnd(String id, long endTime, int exitCode, Throwable exception) {
		IMap<String, JobRunEntry> map = hazelcast.getMap(JOB_RUN);
		if(map.containsKey(id)) {
			map.lock(id);
			try {
				JobRunEntry entry = map.get(id);
				entry.setEndTime(new Date(endTime));
				entry.setExitCode(exitCode);
				if(exception != null) {
					entry.setState(RunState.ERROR);
					entry.setError(exception.getMessage());
				}
				else {
					entry.setState(RunState.FINISH);
				}
				map.set(id, entry);
			}
			finally {
				map.unlock(id);
			}
		}
	}

	/*
	 * @Override
	public void submitJob(ScheduleCommand command, JobRunEntry entry) {
		TransactionContext ctx = hazelcast.newTransactionContext();
		hazelcast.executeTransaction(new TransactionalTask<Void>() {

			@Override
			public Void execute(TransactionalTaskContext context) throws TransactionException {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	 */
}
