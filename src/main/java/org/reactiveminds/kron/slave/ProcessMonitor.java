package org.reactiveminds.kron.slave;

import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.reactiveminds.kron.err.KronRuntimeException;
import org.reactiveminds.kron.slave.ProcessAlertListener.ProcessAlert;
import org.reactiveminds.kron.utils.SystemStat;

class ProcessMonitor implements Closeable {

	private double cpuThreshold, cpuHigh;
	private long memThreshold, memHigh;
	private final Sigar sigar;
	private final Process process;
	private final int pid;
	public long getPid() {
		return pid;
	}

	private final Observable notifManager = new Observable() {
		@Override
		public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}
	};
	public ProcessMonitor(Process process) {
		this.process = process;
		this.pid = SystemStat.processId(process);
		if(pid == 0)
			throw new KronRuntimeException("Invalid process id for given process");
		
		sigar = new Sigar();
		sigarClosed = false;
	}
	/**
	 * 
	 * @param force
	 */
	public void killProcess(boolean force) {
		try {
			if (force) {
				sigar.kill(pid, "-9");
			}
			else
				sigar.kill(pid, "-15");
		} catch (SigarException e) {
			throw new KronRuntimeException("kill process failed", e);
		}
	}
	/**
	 * 
	 * @param listener
	 */
	public void addProcessAlertListener(ProcessAlertListener listener) {
		notifManager.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				listener.onAlert(((AlertAndStat) arg).alert, ((AlertAndStat) arg).stat);
			}
		});
	}
	static final String NO_PROCESS_ERR_MSG = "No such process";
	static class ProcessStat extends SystemStat{
		final double cpuPerc;
		final double memPerc;
		/**
		 * 
		 * @param cpuPerc
		 * @param memPerc
		 * @param run
		 */
		ProcessStat(double cpuPerc, long memPerc, boolean run) {
			super();
			if (run) {
				super.run();
			}
			this.cpuPerc = cpuPerc;
			this.memPerc = memPerc/getTotalPhysicalMemory();
		}

		ProcessStat(double percent, long size) {
			this(percent, size, true);
		}
	}
	private static class AlertAndStat{
		private AlertAndStat(ProcessStat stat, ProcessAlert alert) {
			super();
			this.stat = stat;
			this.alert = alert;
		}
		final ProcessStat stat;
		final ProcessAlert alert;
	}
	private synchronized ProcessStat gather() throws SigarException {
		if (!sigarClosed && process.isAlive()) 
		{
			ProcessStat procStat = new ProcessStat(sigar.getProcCpu(pid).getPercent(), sigar.getProcMem(pid).getSize());
			
			if (procStat.getProcessCpuLoad() >= cpuHigh) {
				notifManager.notifyObservers(new AlertAndStat(procStat, ProcessAlert.CPU_RED));
			} else if (procStat.cpuPerc >= cpuThreshold) {
				notifManager.notifyObservers(new AlertAndStat(procStat, ProcessAlert.CPU_YELLOW));
			}
			if ((100 - procStat.getPctFreeMemory()) >= memHigh) {
				notifManager.notifyObservers(new AlertAndStat(procStat, ProcessAlert.MEM_RED));
			} else if (procStat.memPerc >= memThreshold) {
				notifManager.notifyObservers(new AlertAndStat(procStat, ProcessAlert.MEM_YELLOW));
			} 
			
			return procStat;
		}
		throw new SigarException(NO_PROCESS_ERR_MSG);
	}
	void run() {
		try {
			gather();
		} catch (SigarException e) {
			if(e.getMessage().toUpperCase().contains(NO_PROCESS_ERR_MSG.toUpperCase())) {
				close();
			}
			else
				throw new KronRuntimeException("process stat fetch error", e);
		}
	}

	private boolean sigarClosed;
	@Override
	public synchronized void close() {
		if (!sigarClosed) {
			sigar.close();
			sigarClosed = true;
		}
	}

	public double getCpuThreshold() {
		return cpuThreshold;
	}

	public void setCpuThreshold(double cpuThreshold) {
		this.cpuThreshold = cpuThreshold;
	}

	public long getMemThreshold() {
		return memThreshold;
	}

	public void setMemThreshold(long memThreshold) {
		this.memThreshold = memThreshold;
	}
	public double getCpuHigh() {
		return cpuHigh;
	}
	public void setCpuHigh(double cpuHigh) {
		this.cpuHigh = cpuHigh;
	}
	public long getMemHigh() {
		return memHigh;
	}
	public void setMemHigh(long memHigh) {
		this.memHigh = memHigh;
	}

}
