package org.reactiveminds.kron.core.scheduler;

import java.util.List;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.model.NodeInfo;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.NoWorkerAvailableException;
import org.springframework.beans.factory.annotation.Autowired;

class RoundRobinAllocationPolicy implements WorkerAllocationPolicy {

	@Autowired
	private DistributionService distService;
	@Override
	public void allocate(ExecuteCommand command) {
		List<NodeInfo> nodes = distService.getWorkers();
		if(nodes == null || nodes.isEmpty()) {
			throw new NoWorkerAvailableException(command.getJobName());
		}
		long incr = distService.getNextIncrement(name());
		NodeInfo node = nodes.get((int) (incr % nodes.size()));
		command.setTargetPattern(node.getWorkerId());
		log.info(name()+": Allocated to node "+node.getWorkerId());
	}

	@Override
	public String name() {
		return "ROUNDROBIN";
	}

}
