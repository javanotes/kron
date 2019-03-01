package org.reactiveminds.kron.slave;

import org.reactiveminds.kron.slave.ProcessMonitor.ProcessStat;

@FunctionalInterface
interface ProcessAlertListener{
	static enum ProcessAlert{CPU_YELLOW, MEM_YELLOW, CPU_RED, MEM_RED, DISK_YELLOW, DISK_RED}
	/**
	 * 
	 * @param type
	 */
	void onAlert(ProcessAlert type, ProcessStat snapshot);
}