package org.reactiveminds.kron.slave;

import org.reactiveminds.kron.KronDaemon;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.TaskExecuteException;
import org.reactiveminds.kron.spi.EmbeddedTask;
import org.springframework.beans.BeansException;

//this is going to be executed in the taskrunner thread
class EmbeddedExecutable implements Executable {

	protected EmbeddedExecutable() {
	}

	private ExecuteCommand command;
	
	@Override
	public void setCommand(ExecuteCommand command) {
		this.command = command;
	}
	private EmbeddedTask task;
	@Override
	public void execute() throws TaskExecuteException {
		try {
			task = KronDaemon.getBean(command.getExecution().getJobCommand(), EmbeddedTask.class);
			task.run();
		} catch (BeansException e) {
			throw new TaskExecuteException("No matching spring bean found for embedded execution : "+e.getMessage());
		}
		catch (Exception e) {
			throw new TaskExecuteException("Embedded execution failed for task - "+command.getExecution().getJobCommand(), e);
		}
	}

	@Override
	public int awaitCompletion() throws InterruptedException {
		//this is noop
		return 0;
	}

	@Override
	public void destroy() {
		//noop
	}

	

}
