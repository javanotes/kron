package org.reactiveminds.kron.err;

import org.springframework.scheduling.SchedulingException;

public class NoWorkerAvailableException extends SchedulingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoWorkerAvailableException(String msg) {
		super(msg);
	}
}
