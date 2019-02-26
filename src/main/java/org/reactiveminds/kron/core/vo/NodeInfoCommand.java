package org.reactiveminds.kron.core.vo;

import org.reactiveminds.kron.core.Command;

public class NodeInfoCommand extends CommandTarget {

	public NodeInfoCommand() {
		setCommand(Command.SYSTEMSTAT);
		setTargetPattern(TARGET_ALL);
	}

}
