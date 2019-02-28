package org.reactiveminds.kron.utils;

import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.reactiveminds.kron.err.KronRuntimeException;
import org.reactiveminds.kron.utils.ProcessAlertListener.ProcessAlert;

public class ProcessMonitor implements Runnable,Closeable {

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
				listener.onAlert((ProcessAlert) arg);
			}
		});
	}
	static final String NO_PROCESS_ERR_MSG = "No such process";
	private synchronized void gather() throws SigarException {
		if (!sigarClosed && process.isAlive()) 
		{
			double totalCpu = sigar.getCpuPerc().getCombined();
			double totalMem = sigar.getMem().getUsed();
			ProcCpu cpu = sigar.getProcCpu(pid);
			ProcMem mem = sigar.getProcMem(pid);
			if (totalCpu >= cpuHigh) {
				notifManager.notifyObservers(ProcessAlert.CPU_RED);
			} else if (cpu.getPercent() >= cpuThreshold) {
				notifManager.notifyObservers(ProcessAlert.CPU_YELLOW);
			}
			if (totalMem >= memHigh) {
				notifManager.notifyObservers(ProcessAlert.MEM_RED);
			} else if (mem.getSize() >= memThreshold) {
				notifManager.notifyObservers(ProcessAlert.MEM_YELLOW);
			} 
		}
	}
	@Override
	public void run() {
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
