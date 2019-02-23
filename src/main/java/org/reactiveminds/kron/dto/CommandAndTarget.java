package org.reactiveminds.kron.dto;

import java.io.IOException;

import org.reactiveminds.kron.model.ExecutionRequest;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public abstract class CommandAndTarget implements DataSerializable{
	public static final String TARGET_ALL = "**/*";
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
	private String jobName;
	private Command command;
	private String targetPattern;
	private ExecutionRequest execution;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(getCommand().name());
		out.writeUTF(getTargetPattern());
		out.writeUTF(getJobName());
		execution.writeData(out);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setCommand(Command.valueOf(in.readUTF()));
		setTargetPattern(in.readUTF());
		setJobName(in.readUTF());
		execution = new ExecutionRequest();
		execution.readData(in);
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public ExecutionRequest getExecution() {
		return execution;
	}
	public void setExecution(ExecutionRequest execution) {
		this.execution = execution;
	}
	
}
