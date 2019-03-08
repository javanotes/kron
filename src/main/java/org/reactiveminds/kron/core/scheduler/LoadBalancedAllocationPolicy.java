package org.reactiveminds.kron.core.scheduler;

import java.util.NavigableSet;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.model.NodeInfo;
import org.reactiveminds.kron.core.model.NodeStat;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.NoWorkerAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
class LoadBalancedAllocationPolicy implements WorkerAllocationPolicy {

	@Autowired
	private DistributionService distService;
	@Override
	public void allocate(ExecuteCommand command) {
		NavigableSet<NodeStat> nodes = distService.getWorkerSnapshot();
		if(nodes == null || nodes.isEmpty()) {
			throw new NoWorkerAvailableException(command.getJobName());
		}
		NodeInfo node = nodes.first();
		command.setTargetPattern(node.getWorkerId());
		log.info(name()+": Allocated to node "+node.getWorkerId());
	}

	@Override
	public String name() {
		return "LOADBALANCED";
	}

}
