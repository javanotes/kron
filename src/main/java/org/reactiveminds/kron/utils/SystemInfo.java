package org.reactiveminds.kron.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public final class SystemInfo implements Runnable{

	private static double roundTo2Decimal(double d) {
		return roundToDecimal(d, 2);
	}
	private static double roundToDecimal(double d, int scale) {
		return BigDecimal.valueOf(d).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}
	@Override
	public String toString() {
		return "SystemInfo [processCpuLoad=" + processCpuLoad + ", systemCpuLoad=" + systemCpuLoad
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
	
	private static boolean isSigarEnabled = true;
	public static void enableSigar() {
		isSigarEnabled = true;
	}
	public static void disableSigar() {
		isSigarEnabled = false;
	}
	private void useJava() {
		com.sun.management.OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		totalPhysicalMemory = roundTo2Decimal( (os.getTotalPhysicalMemorySize() * 1.0) / 1024 / 1024);
		pctFreeMemory = roundTo2Decimal( ((os.getFreePhysicalMemorySize() * 1.0) / os.getTotalPhysicalMemorySize()) * 100 );
		processCpuLoad = roundTo2Decimal( os.getProcessCpuLoad()*100.0 );
		systemCpuLoad = roundTo2Decimal( os.getSystemCpuLoad()*100.0 );
		lastTimestamp = System.currentTimeMillis();
		platform = os.getArch();
	}
	private static void checkSigarSetup() {
		try {
			Class<?> clazz = Class.forName("org.hyperic.sigar.Sigar");
			try {
				Method m = clazz.getMethod("load");
				m.invoke(null);
			} catch (NoSuchMethodException | SecurityException e) {}
			clazz.newInstance();
		} catch (Exception e) {
			System.err.println("WARN : Sigar not available, will use JRE support for system info * "+e.getMessage());
			isSigarEnabled = false;
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
				setTotalCpuCacheSize(getTotalCpuCacheSize() + cpu.getCacheSize());
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}
	static {
		checkSigarSetup();
		if(isSigarEnabled)
			try {
				gatherStaticInfo();
			} catch (InterruptedException e) {Thread.currentThread().interrupt();}
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
	}
	@Override
	public void run() {
		if(isSigarEnabled)
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
		SystemInfo sys = new SystemInfo();
		sys.run();
		System.out.println(sys);
		sys.useJava();
		System.out.println(sys);
	}
	public static long getTotalCpuCacheSize() {
		return totalCpuCacheSize;
	}
	public static void setTotalCpuCacheSize(long totalCpuCacheSize) {
		SystemInfo.totalCpuCacheSize = totalCpuCacheSize;
	}
	
}
