package org.reactiveminds.kron.scheduler;

import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.vo.ExecuteCommand;

class RoundRobinSchedulingPolicy implements WorkerAllocationPolicy {

	@Override
	public void allocate(ExecuteCommand command) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		return "ROUNDROBIN";
	}

}
