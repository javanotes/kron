package org.reactiveminds.kron.core.model;

import java.io.IOException;

import org.reactiveminds.kron.utils.SystemStat;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
/**
 * 
 * @author Sutanu_Dalui
 *
 */
public class NodeStat extends NodeInfo {
	
	public SystemStat getSysInfo() {
		return sysInfo;
	}
	/**
	 * Gather the current system statistics.
	 * Will erase previous info run and calculate fresh new.
	 */
	public void gather() {
		sysInfo = new SystemStat();
		sysInfo.run();
	}
	public void setSysInfo(SystemStat sysInfo) {
		this.sysInfo = sysInfo;
	}

	private int jobsRunning;
	private SystemStat sysInfo;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(jobsRunning);
		byte[] b = sysInfo.toBytes();
		out.writeInt(b.length);
		out.write(b);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		setJobsRunning(in.readInt());
		byte[] b = new byte[in.readInt()];
		in.readFully(b);
		sysInfo = new SystemStat();
		sysInfo.fromBytes(b);
	}

	public int getJobsRunning() {
		return jobsRunning;
	}

	public void setJobsRunning(int jobsRunning) {
		this.jobsRunning = jobsRunning;
	}

}
