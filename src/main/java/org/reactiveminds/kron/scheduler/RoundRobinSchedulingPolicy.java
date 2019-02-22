package org.reactiveminds.kron.scheduler;

import org.reactiveminds.kron.core.SchedulingPolicy;
import org.reactiveminds.kron.dto.CommandAndTarget;

class RoundRobinSchedulingPolicy implements SchedulingPolicy {

	@Override
	public void allocate(CommandAndTarget command) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		return "ROUNDROBIN";
	}

}
