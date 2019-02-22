package org.reactiveminds.kron.err;

import org.springframework.core.NestedRuntimeException;

public class KronRuntimeException extends NestedRuntimeException {

	private int errorCode;
	public KronRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public KronRuntimeException(String msg) {
		super(msg);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
