package org.reactiveminds.kron.core.slave;

import javax.annotation.PostConstruct;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.model.NodeInfo;
import org.reactiveminds.kron.scheduler.vo.Schedule;
import org.reactiveminds.kron.scheduler.vo.SecSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
class SystemInfoDaemon implements ScheduledDaemon {

	@Autowired
	DistributionService distService;
	@Value("${kron.worker.sysinfoScheduleSecs:10}")
	private int scheduleSecs;
	@Autowired
	SchedulingSupport executors;
	@PostConstruct
	private void init() {
		NodeInfo.useJRESupportToGather();
	}
	@Override
	public void run() {
		NodeInfo n = new NodeInfo();
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
