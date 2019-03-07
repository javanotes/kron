package org.reactiveminds.kron.err;

public class TaskExecuteException extends KronRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TaskExecuteException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public TaskExecuteException(String msg) {
		super(msg);
	}

}
