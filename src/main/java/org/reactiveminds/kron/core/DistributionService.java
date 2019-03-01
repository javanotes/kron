package org.reactiveminds.kron.core;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.model.JobEntry;
import org.reactiveminds.kron.core.model.JobRunEntry;
import org.reactiveminds.kron.core.model.NodeInfo;
import org.reactiveminds.kron.core.vo.CommandTarget;
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
	String JOB_RUN = "EXEC_STATUS";
	String WORKERID = "WORKERID";
	String NODE_INFO = "NODE_INFO";
	/**
	 * 
	 * @param observer
	 */
	void registerLeaderCallback(LeaderElectNotifier observer);
	/**
	 * 
	 */
	void tryElectAsLeader();
	/**
	 * 
	 * @param key
	 * @return
	 */
	long getNextSequence(String key);
	/**
	 * 
	 * @param callback
	 */
	void registerWorkerChannel(MessageCallback<CommandTarget> callback);
	/**
	 * 
	 * @param callback
	 */
	void registerMasterChannel(MessageCallback<String> callback);
	/**
	 * 
	 * @param command
	 */
	void submitWorkerCommand(CommandTarget command);
	/**
	 * 
	 * @param ack
	 */
	void submitMasterAck(String ack);
	/**
	 * 
	 * @param entry
	 */
	void createJobRunEntry(JobRunEntry entry);
	/**
	 * 
	 * @param id
	 * @param startTime
	 */
	void updateJobRunStart(String id, long startTime);
	/**
	 * 
	 * @param id
	 * @param endTime
	 * @param exception
	 */
	void updateJobRunEnd(String id, long endTime, int exitCode, Throwable exception);
	/**
	 * 
	 * @param filter
	 * @return
	 */
	List<JobEntry> getJobEntries(JobEntryFilter filter);
	/**
	 * 
	 * @return
	 */
	boolean isElectedLeader();
	/**
	 * 
	 * @return
	 */
	String getSelfId();
	/**
	 * 
	 * @return
	 */
	NavigableSet<NodeInfo> getWorkerSnapshot();
	/**
	 * 
	 * @return
	 */
	boolean isWorkerNode();
	/**
	 * 
	 * @param info
	 */
	void updateWorkerSystemInfo(NodeInfo info);
	/**
	 * 
	 * @param name
	 */
	void countdownLatch(String name);
	/**
	 * 
	 * @param name
	 * @param expiry
	 * @param unit
	 * @return
	 */
	boolean awaitWorkerLatch(String name, long expiry, TimeUnit unit);
	/**
	 * 
	 * @param listener
	 */
	void setJobListener(JobEntryListener listener);
}
