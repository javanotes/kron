package org.reactiveminds.kron.slave;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.TaskProxy;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class TaskRunner implements Runnable, TaskProxy {
	
	private static final Logger log = LoggerFactory.getLogger("TaskRunner");
	
	@Value("${kron.worker.goblerThreadEnable:false}")
	private boolean goblerThreadEnable;
	public TaskRunner(ExecuteCommand taskCommand) {
		super();
		this.taskCommand = taskCommand;
	}
	private ExecuteCommand taskCommand;
	private String defaultWorkDir;
	@Autowired
	private DistributionService service;
	@Autowired
	private SchedulingSupport scheduler;
	@Autowired
	private BeanFactory beans;
	
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.core.slave.TaskProxy#setDefaultWorkDir(java.lang.String)
	 */
	@Override
	public void setDefaultWorkDir(String workDir) {
		this.defaultWorkDir = workDir;
	}
	
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.core.slave.TaskProxy#isTaskActive()
	 */
	@Override
	public boolean isTaskActive() {
		return taskActive;
	}
	private volatile boolean taskActive;
	
	private Executable executable;
	
	private void spawnProcess() {
		executable = new ProcessExecutable();
		executable.setCommand(taskCommand);
		((ProcessExecutable)executable).setDefaultWorkDir(defaultWorkDir);
		executable.execute();
		taskActive = true;
	}
	private int awaitCompletion() throws InterruptedException, IOException {
		long schedId = -1;
		try(ProcessMonitorDaemon pmon = beans.getBean(ProcessMonitorDaemon.class, ((ProcessExecutable)executable).getProcess(), 1, TimeUnit.SECONDS)){
			if (((ProcessExecutable)executable).getProcessId() != 0) {
				schedId = scheduler.schedule(pmon);
			}
			return executable.awaitCompletion();
		}
		finally {
			scheduler.cancel(schedId, true);
		}
	}
	private int runProcessTask() throws InterruptedException, IOException {
		spawnProcess();
		log.info("["+taskCommand.getJobName()+"] pid: "+((ProcessExecutable)executable).getProcessId());
		return awaitCompletion();
	}
	private int runEmbeddedTask() throws InterruptedException {
		executable = new EmbeddedExecutable();
		executable.setCommand(taskCommand);
		taskActive = true;
		executable.execute();
		return executable.awaitCompletion();
	}
	
	@Override
	public void run() {
		log.info("["+taskCommand.getJobName()+"] Executing "+taskCommand.getExecution().getJobCommand());
		try 
		{
			service.updateJobRunStart(taskCommand.getExecutionId(), System.currentTimeMillis());
			int exitCode = 0;
			if (taskCommand.isEmbeddedExec()) {
				exitCode = runEmbeddedTask();
			}
			else {
				exitCode = runProcessTask();
			}
			taskActive = false;
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), exitCode, null);
		} 
		catch (InterruptedException e) {
			taskActive = false;
			Thread.currentThread().interrupt();
			executable.destroy();
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), 1, e);
		}
		catch (Exception e) {
			taskActive = false;
			log.error("Error while executing job - "+taskCommand.getJobName(), e);
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), 1, e);
		} 
		
	}
}
