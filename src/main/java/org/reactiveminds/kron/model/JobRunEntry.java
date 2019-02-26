package org.reactiveminds.kron.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.reactiveminds.kron.core.JobScheduler;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
@Entity
public class JobRunEntry implements DataSerializable, Serializable, PartitionKeyed{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JobRunEntry copy() {
		return new JobRunEntry(jobName, execId, startTime, endTime, state, error);
	}
	/*@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long dbId;*/
	private JobRunEntry(String jobName, long execId, Date startTime, Date endTime, RunState state, String error) {
		super();
		this.jobName = jobName;
		this.execId = execId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.state = state;
		this.error = error;
	}
	public JobRunEntry() {
	}
	public static enum RunState{
		SUBMIT,RUNNING,FINISH,ERROR,UNDEF;
	}

	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public long getExecId() {
		return execId;
	}
	public void setExecId(long execId) {
		this.execId = execId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	@Enumerated(EnumType.STRING)
	public RunState getState() {
		return state;
	}
	public void setState(RunState state) {
		this.state = state;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	private int exitCode = 1;
	private String jobName;
	private long execId;
	private Date startTime;
	private Date endTime;
	private RunState state = RunState.UNDEF; 
	
	private String error;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(jobName);
		out.writeLong(execId);
		out.writeUTF(state.name());
		out.writeUTF(error);
		out.writeLong(startTime != null ? startTime.getTime() : -1);
		out.writeLong(endTime != null ? endTime.getTime() : -1);
		out.writeUTF(key);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setJobName(in.readUTF());
		setExecId(in.readLong());
		setState(RunState.valueOf(in.readUTF()));
		setError(in.readUTF());
		long l = in.readLong();
		setStartTime(l == -1 ? null : new Date(l));
		l = in.readLong();
		setEndTime(l == -1 ? null : new Date(l));
		setDBKey(in.readUTF());
	}
	@Transient
	@Override
	public String getPartitionKey() {
		return jobName;
	}
	private String key;
	public void setDBKey(String key) {
		this.key = key;
	}
	@Id
	@Override
	public String getDBKey() {
		return key;
	}
	public void setKey() {
		setDBKey(JobScheduler.makeExecutionKey(jobName, execId));
	}
	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
}
