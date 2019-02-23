package org.reactiveminds.kron.dto;

public class NodeInfoCommand extends CommandAndTarget {

	public NodeInfoCommand() {
		setCommand(Command.SYSTEMSTAT);
		setTargetPattern(TARGET_ALL);
	}

}
