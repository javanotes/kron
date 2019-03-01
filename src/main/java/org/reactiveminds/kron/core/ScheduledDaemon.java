package org.reactiveminds.kron.core;

import org.reactiveminds.kron.core.scheduler.Schedule;
/**
 * Structure for a scheduled task
 * @author Sutanu_Dalui
 *
 */
public interface ScheduledDaemon extends Runnable {
	Schedule getSchedule();
}
