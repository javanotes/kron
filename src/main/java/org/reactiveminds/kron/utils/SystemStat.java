package org.reactiveminds.kron.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.jvnet.winp.WinProcess;
import org.springframework.boot.system.JavaVersion;

public class SystemStat implements Runnable{

	private static double roundTo2Decimal(double d) {
		return roundToDecimal(d, 2);
	}
	private static double roundToDecimal(double d, int scale) {
		return BigDecimal.valueOf(d).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}
	@Override
	public String toString() {
		return "SystemUsage [processCpuLoad=" + processCpuLoad + ", systemCpuLoad=" + systemCpuLoad
				+ ", totalPhysicalMemory=" + totalPhysicalMemory + ", pctFreeMemory=" + pctFreeMemory
				+ ", availableProcessors=" + numOfProcessors + ", lastTimestamp=" + lastTimestamp + ", platform="
				+ platform + "]";
	}

	public double getProcessCpuLoad() {
		return processCpuLoad;
	}
	public double getTotalPhysicalMemory() {
		return totalPhysicalMemory;
	}
	
	public int getAvailableProcessors() {
		return numOfProcessors;
	}
	public long getLastTimestamp() {
		return lastTimestamp;
	}
	private double processCpuLoad;
	private double systemCpuLoad;
	private double totalPhysicalMemory;
	private double pctFreeMemory;
	private long lastTimestamp;
	
	private static AtomicBoolean isSigarEnabled = new AtomicBoolean(true);
	/**
	 * Disables the use of Sigar library (even if present), and 
	 * falls back to com.sun.management.OperatingSystemMXBean probes.
	 */
	public static void useJRESupportToGather() {
		isSigarEnabled.compareAndSet(true, false);
	}
	@SuppressWarnings("restriction")
	private void useJava() {
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		totalPhysicalMemory = roundTo2Decimal( (os.getTotalPhysicalMemorySize() * 1.0) / 1024 / 1024);
		pctFreeMemory = roundTo2Decimal( ((os.getFreePhysicalMemorySize() * 1.0) / os.getTotalPhysicalMemorySize()) * 100 );
		processCpuLoad = roundTo2Decimal( os.getProcessCpuLoad()*100.0 );
		systemCpuLoad = roundTo2Decimal( os.getSystemCpuLoad()*100.0 );
		lastTimestamp = System.currentTimeMillis();
		platform = os.getArch();
	}
	private static void checkSigarSetup() {
		JavaVersion version = JavaVersion.getJavaVersion();
		if(version == JavaVersion.NINE) {
			//Sigar is crashing on java 9 (tested on Windows 10)
			/*
			 	Current thread (0x000001cc478b4000):  JavaThread "main" [_thread_in_native, id=16304, stack(0x0000007dd0d00000,0x0000007dd0e00000)]
				Stack: [0x0000007dd0d00000,0x0000007dd0e00000],  sp=0x0000007dd0dfcee0,  free space=1011k
				Native frames: (J=compiled Java code, j=interpreted, Vv=VM code, C=native code)
				C  [sigar-amd64-winnt.dll+0x14ed4]
				C  [sigar-amd64-winnt.dll+0x22078]
				C  0x000001cc51868f1c
				
				Java frames: (J=compiled Java code, j=interpreted, Vv=VM code)
				j  org.hyperic.sigar.Sigar.getCpuInfoList()[Lorg/hyperic/sigar/CpuInfo;+0
				j  org.reactiveminds.kron.utils.SystemInfo.gatherStaticInfo()V+9
				j  org.reactiveminds.kron.utils.SystemInfo.<clinit>()V+25
				v  ~StubRoutines::call_stub
				j  org.reactiveminds.kron.model.NodeInfo.useJRESupportToGather()V+0
				j  org.reactiveminds.kron.core.slave.SystemInfoDaemon.init()V+0
				v  ~StubRoutines::call_stub
			 
			 */
			isSigarEnabled.compareAndSet(true, false);
			return;
		}
		try 
		{
			Class<?> clazz = Class.forName("org.hyperic.sigar.Sigar");
			try {
				Method m = clazz.getMethod("load");
				m.invoke(null);
			} catch (NoSuchMethodException | SecurityException e) {}
			clazz.getDeclaredConstructor().newInstance();
		} 
		catch (Exception e) {
			System.err.println("WARN : Sigar not available, will use JRE support for system info * "+e.getMessage());
			isSigarEnabled.compareAndSet(true, false);
		}
	}
	private static int numOfCores = 0, numOfProcessors = 0;
	private static String platform;
	private static long totalCpuCacheSize = 0;
	
	private static void gatherStaticInfo() throws InterruptedException {
		Sigar sigar = new Sigar();
		try {
			CpuInfo [] info = sigar.getCpuInfoList();
			for(CpuInfo cpu : info) {
				numOfCores =  cpu.getTotalCores();
				numOfProcessors = cpu.getTotalSockets();
				platform = cpu.getVendor() + " " + cpu.getModel();
				totalCpuCacheSize = (getTotalCpuCacheSize() + cpu.getCacheSize());
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}
		finally {
			sigar.close();
		}
	}
	static {
		checkSigarSetup();
		if(isSigarEnabled.get()) {
			try {
				gatherStaticInfo();
			} catch (InterruptedException e) {Thread.currentThread().interrupt();}
		}
	}
	public static void getProcessCpu(long pid) {
		if(isSigarEnabled.get()) {
			
		}
	}
	private void useSigar() {
		Sigar sigar = new Sigar();
		Mem mem = null;
		CpuPerc cpu = null;
        try {
            mem = sigar.getMem();
            cpu = sigar.getCpuPerc();
            totalPhysicalMemory = roundTo2Decimal( (mem.getTotal() * 1.0) / 1024 / 1024);
    		pctFreeMemory = roundTo2Decimal( ((mem.getFree() * 1.0) / mem.getTotal()) * 100 );
    		processCpuLoad = roundTo2Decimal( cpu.getCombined()*100.0 );
    		systemCpuLoad = roundTo2Decimal( cpu.getSys()*100.0 );
    		lastTimestamp = System.currentTimeMillis();
    		
        } catch (SigarException se) {
            throw new RuntimeException(se);
        }
        finally {
        	sigar.close();
        }
	}
	@Override
	public void run() {
		if(isSigarEnabled.get())
			useSigar();
		else
			useJava();
	}
	public double getPctFreeMemory() {
		return pctFreeMemory;
	}
	public void fromBytes(byte[] bytes) {
		ByteBuffer buff = ByteBuffer.wrap(bytes);
		processCpuLoad = buff.getDouble();
		systemCpuLoad = buff.getDouble();
		totalPhysicalMemory = buff.getDouble();
		pctFreeMemory = buff.getDouble();
		lastTimestamp = buff.getLong();
		numOfProcessors = buff.getInt();
		byte [] b = new byte[buff.remaining()];
		buff.get(b);
		platform = new String(b, StandardCharsets.UTF_8);
	}
	public byte[] toBytes() {
		ByteBuffer buff = ByteBuffer.allocate(44 + platform.length());
		buff.putDouble(processCpuLoad);
		buff.putDouble(systemCpuLoad);
		buff.putDouble(totalPhysicalMemory);
		buff.putDouble(pctFreeMemory);
		buff.putLong(lastTimestamp);
		buff.putInt(numOfProcessors);
		buff.put(platform.getBytes(StandardCharsets.UTF_8));
		
		return buff.array();
	}
	public int getNumOfCores() {
		return numOfCores;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Thread.sleep(2000);
		SystemStat sys = new SystemStat();
		sys.run();
		System.out.println(sys);
		sys.useJava();
		System.out.println(sys);
	}
	public static long getTotalCpuCacheSize() {
		return totalCpuCacheSize;
	}
	
	public static boolean isSigarSupported() {
		return isSigarEnabled.get();
		
	}
	/**
	 * Get process id for a given process.
	 * @param process
	 * @return
	 */
	public static int processId(Process process)
	{
	    if (process.getClass().getName().equals("java.lang.UNIXProcess"))
	    {
	        try
	        {
	            Field f = process.getClass().getDeclaredField("pid");
	            f.setAccessible(true);
	            return f.getInt(process);
	        }
	        catch (Exception e)
	        {
	        }
	    }
	    else if (process.getClass().getName().equals("java.lang.Win32Process") ||
	    		process.getClass().getName().equals("java.lang.ProcessImpl")) 
         {
             WinProcess wp = new WinProcess(process);
             return wp.getPid();
         }

	    return 0;
	}
}
