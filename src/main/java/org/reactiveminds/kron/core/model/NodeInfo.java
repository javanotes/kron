package org.reactiveminds.kron.core.model;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class NodeInfo implements DataSerializable{
	
	private String workerId;

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public NodeInfo(String workerId) {
		super();
		this.workerId = workerId;
	}

	public NodeInfo() {
		super();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(workerId);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setWorkerId(in.readUTF());
	}
}