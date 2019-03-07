package org.reactiveminds.kron.slave;

import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.TaskExecuteException;

/**
 * Abstraction for different type of executions.
 * @author Sutanu_Dalui
 *
 */
interface Executable {
	/**
	 * 
	 * @param command
	 */
	void setCommand(ExecuteCommand command);
	/**
	 * 
	 * @throws TaskExecuteException
	 */
	void execute() throws TaskExecuteException;
	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	int awaitCompletion() throws InterruptedException;
	/**
	 * 
	 */
	void destroy();
	
}
