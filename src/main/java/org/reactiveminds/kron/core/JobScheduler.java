package org.reactiveminds.kron.core;

import org.reactiveminds.kron.core.model.JobEntry;
import org.reactiveminds.kron.err.KronRuntimeException;
import org.springframework.util.Assert;

public interface JobScheduler {
	String TODAY = "TODAY";
	String TOMORROW = "TOMORROW";
	String NOW = "NOW";
	
	String KEY_SEP = "#";
	public static String makeExecutionKey(String jobName, long seq) {
		return jobName + KEY_SEP + seq;
	}
	public static class HKey{
		private HKey(String jobName, long seq) {
			super();
			this.jobName = jobName;
			this.seq = seq;
		}
		public final String jobName;
		public final long seq;
	}
	public static HKey extractExecutionKey(String key) {
		try {
			Assert.isTrue(key.contains(JobScheduler.KEY_SEP), "Invalid execution key - "+key);
			String[] keys = key.split(JobScheduler.KEY_SEP, 2);
			return new HKey(keys[0], Long.parseLong(keys[1]));
		} catch (IllegalArgumentException e) {
			throw new KronRuntimeException(e.getMessage());
		}
	}
	/**
	 * Schedule the run of the job. The actual execution will happen
	 * on any one of the worker nodes.
	 * @param job
	 */
	void scheduleJob(JobEntry job);
	/**
	 * Cancels the schedule
	 * @param jobName
	 */
	void cancelJob(String jobName);
}