package org.reactiveminds.kron.core.vo;

import java.io.IOException;

import org.reactiveminds.kron.core.Command;
import org.reactiveminds.kron.model.ExecutionRequest;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ExecuteCommand extends CommandTarget {

	protected ExecuteCommand(String executionId, String jobName, ExecutionRequest execution, Command command, String targetPattern) {
		super();
		this.executionId = executionId;
		this.jobName = jobName;
		this.execution = execution;
		setCommand(command);
		setTargetPattern(targetPattern);
	}
	public ExecuteCommand() {
		setCommand(Command.EXECUTE);
	}
	public String getExecutionId() {
		return executionId;
	}
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
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
	protected String executionId;
	protected String jobName;
	protected ExecutionRequest execution;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(getJobName());
		execution.writeData(out);
		out.writeUTF(executionId);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		setJobName(in.readUTF());
		execution = new ExecutionRequest();
		execution.readData(in);
		setExecutionId(in.readUTF());
	}
}
