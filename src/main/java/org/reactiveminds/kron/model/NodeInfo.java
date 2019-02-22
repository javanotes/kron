package org.reactiveminds.kron.model;

import java.io.IOException;

import org.reactiveminds.kron.utils.SystemInfo;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
/**
 * 
 * @author Sutanu_Dalui
 *
 */
public class NodeInfo implements DataSerializable {
	/**
	 * Disables the use of Sigar library (even if present), and 
	 * falls back to com.sun.management.OperatingSystemMXBean probes.
	 */
	public static void useJRESupportToGather() {
		SystemInfo.disableSigar();
	}
	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public SystemInfo getSysInfo() {
		return sysInfo;
	}
	/**
	 * Gather the current system statistics.
	 * Will erase previous info run and calculate fresh new.
	 */
	public void gather() {
		sysInfo = new SystemInfo();
		sysInfo.run();
	}
	public void setSysInfo(SystemInfo sysInfo) {
		this.sysInfo = sysInfo;
	}

	private int jobsRunning;
	private String workerId;
	private SystemInfo sysInfo;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(workerId);
		out.writeInt(jobsRunning);
		byte[] b = sysInfo.toBytes();
		out.writeInt(b.length);
		out.write(b);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setWorkerId(in.readUTF());
		setJobsRunning(in.readInt());
		byte[] b = new byte[in.readInt()];
		in.readFully(b);
		sysInfo = new SystemInfo();
		sysInfo.fromBytes(b);
	}

	public int getJobsRunning() {
		return jobsRunning;
	}

	public void setJobsRunning(int jobsRunning) {
		this.jobsRunning = jobsRunning;
	}

}
