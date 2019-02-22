package org.reactiveminds.kron.core;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.master.LeaderObserver;
import org.reactiveminds.kron.dto.CommandAndTarget;
import org.reactiveminds.kron.model.JobEntry;
import org.reactiveminds.kron.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The service apis for distributed computing. The default implementation uses Hazelcast.
 * @author Sutanu_Dalui
 *
 */
public interface DistributionService {

	Logger LOG = LoggerFactory.getLogger("DistributionService");
	String COMM_CHANNEL = "MASTER_TO_SLAVE";
	String ACK_CHANNEL = "SLAVE_TO_MASTER";
	String JOB_MASTER = "JOB_MASTER";
	String EXEC_STATUS = "EXEC_STATUS";
	String EXEC_QUEUE = "EXEC_QUEUE";
	String WORKERID = "WORKERID";
	String NODE_INFO = "NODE_INFO";
	
	void registerLeaderCallback(LeaderObserver observer);
	void tryElectAsLeader();
	
	void registerWorkerChannel(MessageCallback<CommandAndTarget> callback);
	void registerMasterChannel(MessageCallback<String> callback);
	
	void submitWorkerCommand(CommandAndTarget command);
	void submitMasterAck(String ack);
	//void saveJobEntry(JobEntry entry);
	List<JobEntry> getJobEntries(JobEntryFilter filter);
	boolean isElectedLeader();
	String getSelfId();
	NavigableSet<NodeInfo> getWorkerSnapshot();
	boolean isWorkerNode();
	void updateWorkerSystemInfo(NodeInfo info);
	
	void countdownLatch(String name);
	boolean awaitWorkerLatch(String name, long expiry, TimeUnit unit);
	/**
	 * 
	 * @param listener
	 */
	void setJobListener(JobEntryListener listener);
}
