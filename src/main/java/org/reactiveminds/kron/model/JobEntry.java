package org.reactiveminds.kron.model;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class JobEntry implements DataSerializable {

	public JobEntry() {
	}
	public ExecutionRequest getJob() {
		return job;
	}

	public void setJob(ExecutionRequest job) {
		this.job = job;
	}
	private boolean isEnabled = true;
	private ExecutionRequest job;
	//..other properties
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		job.writeData(out);
		out.writeBoolean(isEnabled);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		job = new ExecutionRequest();
		job.readData(in);
		setEnabled(in.readBoolean());
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
}
