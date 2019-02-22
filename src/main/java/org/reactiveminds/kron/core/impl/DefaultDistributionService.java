package org.reactiveminds.kron.core.impl;

import java.util.List;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobEntryFilter;
import org.reactiveminds.kron.core.JobEntryListener;
import org.reactiveminds.kron.core.MessageCallback;
import org.reactiveminds.kron.core.master.LeaderObserver;
import org.reactiveminds.kron.dto.CommandAndTarget;
import org.reactiveminds.kron.err.OperationNotPermittedException;
import org.reactiveminds.kron.model.JobEntry;
import org.reactiveminds.kron.model.NodeInfo;
import org.reactiveminds.kron.utils.JsonMapper;
import org.reactiveminds.kron.utils.NodeInfoComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
import com.hazelcast.util.StringUtil;

@Service
class DefaultDistributionService extends Observable implements DistributionService {

	@Autowired
	private HazelcastInstance hazelcast;
	@Value("${kron.master.workerStatExpirySecs:10}")
	private int nodeinfoExpiry;
	private volatile boolean isElectedLeader;
	@Override
	public boolean isElectedLeader() {
		return isElectedLeader;
	}
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
			setChanged();
			notifyObservers();
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
	public void registerWorkerChannel(MessageCallback<CommandAndTarget> callback) {
		if(!isWorkerNode())
			throw new OperationNotPermittedException("Cannot register worker channel in MASTER mode");
		
		ITopic<CommandAndTarget> channel = hazelcast.getTopic(COMM_CHANNEL);
		channel.addMessageListener(new MessageListener<CommandAndTarget>() {
			
			@Override
			public void onMessage(Message<CommandAndTarget> message) {
				callback.onMessage(message.getMessageObject());
			}
		});
		
	}
	@Override
	public void submitWorkerCommand(CommandAndTarget command) {
		ITopic<CommandAndTarget> channel = hazelcast.getTopic(COMM_CHANNEL);
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
	public void registerLeaderCallback(LeaderObserver observer) {
		addObserver(observer);
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
		return map.entrySet().stream().map(e -> toJobEntry(e.getValue()))
		.filter(filter)
		.collect(Collectors.toList());
		
	}

	@Override
	public boolean isWorkerNode() {
		return hazelcast.getLocalEndpoint() instanceof Client;
	}

	private static JobEntry toJobEntry(RestValue val) {
		String cType = StringUtil.bytesToString(val.getContentType());
		Assert.isTrue(cType.toLowerCase().contains("json"), "Entry added over rest is not a permitted content-type: "+cType);
		return JsonMapper.deserialize(val.getValue(), JobEntry.class);
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
			listener.onEntryAdded(toJobEntry(event.getValue()));
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

}
