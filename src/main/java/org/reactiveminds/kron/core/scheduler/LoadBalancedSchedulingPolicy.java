package org.reactiveminds.kron.core.scheduler;

import java.util.NavigableSet;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.model.NodeInfo;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.NoWorkerAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
class LoadBalancedSchedulingPolicy implements WorkerAllocationPolicy {

	private static final Logger log = LoggerFactory.getLogger("LoadBalancedSchedulingPolicy");
	@Autowired
	private DistributionService distService;
	@Override
	public void allocate(ExecuteCommand command) {
		NavigableSet<NodeInfo> nodes = distService.getWorkerSnapshot();
		if(nodes == null || nodes.isEmpty()) {
			throw new NoWorkerAvailableException(command.getJobName());
		}
		NodeInfo node = nodes.first();
		command.setTargetPattern(node.getWorkerId());
		log.info("Allocated to node "+node.getWorkerId());
	}

	@Override
	public String name() {
		return "LOADBALANCED";
	}

}
