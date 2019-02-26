package org.reactiveminds.kron.core;

import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.AllocationPolicyException;
/**
 * The worker node selection policy available as per configuration.
 * @author Sutanu_Dalui
 *
 */
public interface WorkerAllocationPolicy {
	/**
	 * Name of the policy, meaningful and unique.
	 * @return
	 */
	String name();
	/**
	 * Allocate task to the best available worker
	 * @param command
	 * @throws AllocationPolicyException
	 */
	void allocate(ExecuteCommand command) throws AllocationPolicyException;
}
