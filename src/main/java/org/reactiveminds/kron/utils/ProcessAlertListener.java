package org.reactiveminds.kron.utils;

@FunctionalInterface
public interface ProcessAlertListener{
	public static enum ProcessAlert{CPU_YELLOW, MEM_YELLOW, CPU_RED, MEM_RED, DISK_YELLOW, DISK_RED}
	/**
	 * 
	 * @param type
	 */
	void onAlert(ProcessAlert type);
}