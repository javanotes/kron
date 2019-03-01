package org.reactiveminds.kron.slave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.TaskProxy;
import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.utils.SystemStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

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
	private class StreamGobler extends Thread implements AutoCloseable{
		private final BufferedReader reader;
		private StreamGobler(InputStream stream) {
			super(taskCommand.getJobName());
			this.reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		}
		@Override
		public void run() {
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					log.info("> " + line);
				}
			} catch (IOException e) {
				log.error("Error while reading execution output, "+ e.getMessage());
				log.debug("", e);
			}
		}
		@Override
		public void close() {
			try {
				reader.close();
			} catch (IOException e) {
				log.warn("Error while closing output stream, "+ e.getMessage());
				log.debug("", e);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.core.slave.TaskProxy#isTaskActive()
	 */
	@Override
	public boolean isTaskActive() {
		return taskActive;
	}
	private volatile boolean taskActive;
	private Process process = null;
	
	private int spawnProcess() throws IOException {
		ProcessBuilder builder = new ProcessBuilder(taskCommand.getExecution().getJobCommand().split(" "));
		builder.directory(ResourceUtils.getFile(StringUtils.hasText(taskCommand.getExecution().getWorkDir()) ? taskCommand.getExecution().getWorkDir() : defaultWorkDir));
		builder.redirectErrorStream(true);
		process = builder.start();
		taskActive = true;
		return SystemStat.processId(process);
	}
	private void awaitCompletion(int pid) throws InterruptedException, IOException {
		long schedId = -1;
		try(StreamGobler sg = new StreamGobler(process.getInputStream()); ProcessMonitorDaemon pmon = beans.getBean(ProcessMonitorDaemon.class, process, 1, TimeUnit.SECONDS)){
			if (pid != 0) {
				schedId = scheduler.schedule(pmon);
			}
			if (goblerThreadEnable) {
				sg.start();
			}
			else {
				sg.run();
			}
			int e = process.waitFor();
			taskActive = false;
			log.info("["+taskCommand.getJobName()+"] Exit code: "+e);
			
			if (goblerThreadEnable) {
				sg.join(TimeUnit.SECONDS.toMillis(10));
			}
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), e, null);
		}
		finally {
			scheduler.cancel(schedId, true);
		}
	}
	//9800841527
	@Override
	public void run() {
		log.info("["+taskCommand.getJobName()+"] Executing "+taskCommand.getExecution().getJobCommand());
		try 
		{
			service.updateJobRunStart(taskCommand.getExecutionId(), System.currentTimeMillis());
			int pid = spawnProcess();
			log.info("["+taskCommand.getJobName()+"] pid: "+pid);
			awaitCompletion(pid);
			
		} 
		catch (IOException e) {
			log.error("Error while executing job - "+taskCommand.getJobName(), e);
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), 1, e);
		} 
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (process != null) {
				process.destroy();
			}
			service.updateJobRunEnd(taskCommand.getExecutionId(), System.currentTimeMillis(), 1, e);
		}
	}
}
