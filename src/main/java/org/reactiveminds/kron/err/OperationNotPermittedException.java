package org.reactiveminds.kron.err;

public class OperationNotPermittedException extends KronRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OperationNotPermittedException(String msg) {
		super(msg);
	}

}
