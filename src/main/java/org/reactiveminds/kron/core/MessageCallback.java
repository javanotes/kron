package org.reactiveminds.kron.core;
/**
 * Generic message callback lambda
 * @author Sutanu_Dalui
 *
 * @param <T>
 */
@FunctionalInterface
public interface MessageCallback<T> {
	/**
	 * 
	 * @param message
	 */
	void onMessage(T message);
}
