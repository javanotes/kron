package org.reactiveminds.kron.core;

import org.reactiveminds.kron.scheduler.vo.Schedule;
/**
 * Structure for a scheduled task
 * @author Sutanu_Dalui
 *
 */
public interface ScheduledDaemon extends Runnable {
	Schedule getSchedule();
}
