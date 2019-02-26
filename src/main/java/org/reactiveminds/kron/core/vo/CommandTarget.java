package org.reactiveminds.kron.core.vo;

import java.io.IOException;

import org.reactiveminds.kron.core.Command;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
/**
 * 
 * An abstract command class 
 */
public abstract class CommandTarget implements DataSerializable{
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
	private Command command;
	private String targetPattern;
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(getCommand().name());
		out.writeUTF(getTargetPattern());
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		setCommand(Command.valueOf(in.readUTF()));
		setTargetPattern(in.readUTF());
	}
}
