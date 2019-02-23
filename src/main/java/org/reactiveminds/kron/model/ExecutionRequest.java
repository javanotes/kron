package org.reactiveminds.kron.model;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ExecutionRequest implements DataSerializable{

	public int getNumOfCores() {
		return numOfCores;
	}
	public void setNumOfCores(int numOfCores) {
		this.numOfCores = numOfCores;
	}
	public long getMinMemoryMb() {
		return minMemoryMb;
	}
	public void setMinMemoryMb(long minMemoryMb) {
		this.minMemoryMb = minMemoryMb;
	}
	
	private int numOfCores;
	private long minMemoryMb;
	private String jobCommand;
	private String workDir;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(numOfCores);
		out.writeLong(minMemoryMb);
		out.writeUTF(jobCommand);
		out.writeUTF(workDir);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setNumOfCores(in.readInt());
		setMinMemoryMb(in.readLong());
		setJobCommand(in.readUTF());
		setWorkDir(in.readUTF());
	}
	
	public String getJobCommand() {
		return jobCommand;
	}
	public void setJobCommand(String jobCommand) {
		this.jobCommand = jobCommand;
	}
	public String getWorkDir() {
		return workDir;
	}
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
}
