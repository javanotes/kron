package org.reactiveminds.kron.model;

import java.io.IOException;
import java.util.Date;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
/**
 * @deprecated
 * @author Sutanu_Dalui
 *
 */
public class ExecutionEntry implements DataSerializable {

	public Date getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public ExecutionRequest getJob() {
		return job;
	}

	public void setJob(ExecutionRequest job) {
		this.job = job;
	}

	private Date scheduledTime;
	private ExecutionRequest job;
	public ExecutionEntry() {
	}
	
	public ExecutionEntry(Date scheduledTime, ExecutionRequest job) {
		super();
		this.scheduledTime = scheduledTime;
		this.job = job;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(scheduledTime != null ? scheduledTime.getTime() : -1L);
		job.writeData(out);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		long t = in.readLong();
		if(t != -1)
			scheduledTime = new Date(t);
		job = new ExecutionRequest();
		job.readData(in);
	}

}
