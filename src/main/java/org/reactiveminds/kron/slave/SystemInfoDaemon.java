package org.reactiveminds.kron.slave;

import javax.annotation.PostConstruct;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.model.NodeStat;
import org.reactiveminds.kron.core.scheduler.Schedule;
import org.reactiveminds.kron.core.scheduler.SecSchedule;
import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.utils.SystemStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
class SystemInfoDaemon implements ScheduledDaemon {

	@Autowired
	DistributionService distService;
	@Value("${kron.worker.sysinfo.scheduleSecs:10}")
	private int scheduleSecs;
	@Value("${kron.worker.sysinfo.useJavaRuntime:false}")
	private boolean disableSigar;
	@Autowired
	SchedulingSupport executors;
	@PostConstruct
	private void init() {
		if (disableSigar) {
			SystemStat.useJRESupportToGather();
		}
	}
	@Override
	public void run() {
		NodeStat n = new NodeStat();
		n.setWorkerId(distService.getSelfId());
		n.setJobsRunning(executors.executionCapacity());
		n.gather();
		distService.updateWorkerSystemInfo(n);
	}

	@Override
	public Schedule getSchedule() {
		return new SecSchedule(scheduleSecs);
	}

}
