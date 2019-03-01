package org.reactiveminds.kron.slave;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.core.scheduler.MilliSchedule;
import org.reactiveminds.kron.core.scheduler.Schedule;
import org.reactiveminds.kron.slave.ProcessMonitor.ProcessStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
class ProcessMonitorDaemon implements ScheduledDaemon, Closeable, ProcessAlertListener {
	private static final Logger log = LoggerFactory.getLogger("ProcessMonitor");
	/**
	 * 
	 * @param pid
	 */
	public ProcessMonitorDaemon(Process pid) {
		pmon = new ProcessMonitor(pid);
		pmon.addProcessAlertListener(this);
	}
	/**
	 * 
	 * @param pid
	 * @param period
	 * @param unit
	 */
	public ProcessMonitorDaemon(Process pid, long period, TimeUnit unit) {
		this(pid);
		scheduleMillis = unit.toMillis(period);
	}
	
	private final ProcessMonitor pmon;
	@Override
	public void run() {
		pmon.run();
		log.debug("[pid: "+pmon.getPid()+"] monitor ran");
	}
	private long scheduleMillis = 5000;
	@Override
	public Schedule getSchedule() {
		return new MilliSchedule(scheduleMillis);
	}

	@Override
	public void close() throws IOException {
		pmon.close();
	}

	@Override
	public void onAlert(ProcessAlert type, ProcessStat stat) {
		log.warn("[pid: "+pmon.getPid()+"] " + type);
		//TODO : take some action
	}

}
