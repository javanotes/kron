package org.reactiveminds.kron.scheduler;

import java.util.NavigableSet;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingPolicy;
import org.reactiveminds.kron.dto.CommandAndTarget;
import org.reactiveminds.kron.err.NoWorkerAvailableException;
import org.reactiveminds.kron.model.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
class LoadBalancedSchedulingPolicy implements SchedulingPolicy {

	private static final Logger log = LoggerFactory.getLogger("LoadBalancedSchedulingPolicy");
	@Autowired
	private DistributionService distService;
	@Override
	public void allocate(CommandAndTarget command) {
		NavigableSet<NodeInfo> nodes = distService.getWorkerSnapshot();
		if(nodes == null || nodes.isEmpty()) {
			throw new NoWorkerAvailableException(command.getJobName());
		}
		NodeInfo node = nodes.first();
		command.setTargetPattern(node.getWorkerId());
		distService.submitWorkerCommand(command);
		log.info("Submitted to node "+node.getWorkerId());
	}

	@Override
	public String name() {
		return "LOADBALANCED";
	}

}
