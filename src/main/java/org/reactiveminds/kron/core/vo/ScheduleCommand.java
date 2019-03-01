package org.reactiveminds.kron.core.vo;

import java.io.IOException;

import org.reactiveminds.kron.core.Command;
import org.reactiveminds.kron.core.model.ExecutionRequest;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ScheduleCommand extends ExecuteCommand {

	public ScheduleCommand copy() {
		return new ScheduleCommand(getCronExpr(), getExecutionId(), getJobName(), getExecution(), getCommand(), getTargetPattern());
	}
	private ScheduleCommand(String cronExpr, String executionId, String jobName, ExecutionRequest execution, Command command, String targetPattern) {
		super(executionId, jobName, execution, command, targetPattern);
		this.cronExpr = cronExpr;
	}
	public ScheduleCommand() {
		setCommand(Command.SCHEDULE);
	}
	public String getCronExpr() {
		return cronExpr;
	}
	public void setCronExpr(String cronExpr) {
		this.cronExpr = cronExpr;
	}
	private String cronExpr;
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(cronExpr);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		setCronExpr(in.readUTF());
	}
}
