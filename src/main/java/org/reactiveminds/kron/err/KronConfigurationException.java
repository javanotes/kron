package org.reactiveminds.kron.err;

import org.springframework.beans.factory.BeanInitializationException;

public class KronConfigurationException extends BeanInitializationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KronConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public KronConfigurationException(String msg) {
		super(msg);
	}

}
