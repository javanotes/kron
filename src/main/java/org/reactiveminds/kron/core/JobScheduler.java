package org.reactiveminds.kron.core;

import org.reactiveminds.kron.model.JobEntry;

public interface JobScheduler {

	/**
	 * Schedule the run of the job. The actual execution will happen
	 * on any one of the worker nodes.
	 * @param job
	 */
	void scheduleJob(JobEntry job);

	/**
	 * Submit a new job. This is an alternative API to posting a {@linkplain JobEntry} via Hz Rest
	 * @param request
	 * @deprecated Using Hz rest api
	 */
	//void registerJob(ExecutionRequest request);

}