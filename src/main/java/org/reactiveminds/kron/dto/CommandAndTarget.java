package org.reactiveminds.kron.dto;

import java.io.IOException;

import org.reactiveminds.kron.model.ExecutionRequest;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class CommandAndTarget implements DataSerializable{
	
	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public String getTargetPattern() {
		return targetPattern;
	}
	public void setTargetPattern(String targetPattern) {
		this.targetPattern = targetPattern;
	}
	private Command command;
	private String targetPattern;
	private ExecutionRequest request;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(getCommand().name());
		out.writeUTF(getTargetPattern());
		request.writeData(out);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setCommand(Command.valueOf(in.readUTF()));
		setTargetPattern(in.readUTF());
		request = new ExecutionRequest();
		request.readData(in);
	}
	public ExecutionRequest getRequest() {
		return request;
	}
	public void setRequest(ExecutionRequest request) {
		this.request = request;
	}
}
