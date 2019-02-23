package org.reactiveminds.kron.dto;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ScheduleCommand extends CommandAndTarget {

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
