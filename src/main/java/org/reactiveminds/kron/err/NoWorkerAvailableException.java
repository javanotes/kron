package org.reactiveminds.kron.err;

public class NoWorkerAvailableException extends AllocationPolicyException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoWorkerAvailableException(String msg) {
		super(msg);
	}
}
