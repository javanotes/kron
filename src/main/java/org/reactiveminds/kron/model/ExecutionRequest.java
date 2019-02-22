package org.reactiveminds.kron.model;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ExecutionRequest implements DataSerializable{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cronSchedule == null) ? 0 : cronSchedule.hashCode());
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutionRequest other = (ExecutionRequest) obj;
		if (cronSchedule == null) {
			if (other.cronSchedule != null)
				return false;
		} else if (!cronSchedule.equals(other.cronSchedule))
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		return true;
	}
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
	public String getCronSchedule() {
		return cronSchedule;
	}
	public void setCronSchedule(String cronSchedule) {
		this.cronSchedule = cronSchedule;
	}
	private int numOfCores;
	private long minMemoryMb;
	private String cronSchedule;
	private String jobName;
	private String jobCommand;
	private String workDir;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(numOfCores);
		out.writeLong(minMemoryMb);
		out.writeUTF(cronSchedule);
		out.writeUTF(jobName);
		out.writeUTF(jobCommand);
		out.writeUTF(workDir);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setNumOfCores(in.readInt());
		setMinMemoryMb(in.readLong());
		setCronSchedule(in.readUTF());
		setJobName(in.readUTF());
		setJobCommand(in.readUTF());
		setWorkDir(in.readUTF());
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
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
