package org.reactiveminds.kron.core;

import org.reactiveminds.kron.scheduler.vo.Schedule;

/**
 * The core interface to define underlying scheduling (and asynchronous execution) mechanism. By default
 * uses Spring task scheduling. May be implemented with a different provider, say Quartz
 * @author Sutanu_Dalui
 *
 */
public interface SchedulingSupport{
	/**
	 * Cancel task execution future
	 * @param id
	 * @param intrIfRunning
	 */
	void cancel(long id, boolean intrIfRunning);
	/**
	 * Schedule based on the cron expression
	 * @param daemon
	 * @param cronSequence
	 */
	long schedule(Runnable daemon, String cronSequence);
	/**
	 * Schedule this task at a fixed rate, or at a specified instant when
	 * {@link Schedule#getRepeatAfter()} is 0
	 * @param daemon
	 */
	long schedule(ScheduledDaemon daemon);
	/**
	 * Execute a one time task asynchronously
	 * @param r
	 * @return
	 */
	long execute(Runnable daemon);
	/**
	 * The current execution capacity. Basically it should translate 
	 * to the number of free worker threads at any moment.
	 * @return
	 */
	int executionCapacity();
}
