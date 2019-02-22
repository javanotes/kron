package org.reactiveminds.kron.core.slave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.model.ExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class TaskRunner implements Runnable {
	private static final Logger log = LoggerFactory.getLogger("TaskRunner");
	@Value("${kron.worker.goblerThreadEnable:false}")
	private boolean goblerThreadEnable;
	public TaskRunner(ExecutionRequest taskCommand) {
		super();
		this.taskCommand = taskCommand;
	}
	private ExecutionRequest taskCommand;
	private String defaultWorkDir;
	
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
	@Override
	public void run() {
		log.info("["+taskCommand.getJobName()+"] Executing "+taskCommand.getJobCommand());
		Process proc = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(taskCommand.getJobCommand().split(" "));
			builder.directory(ResourceUtils.getFile(StringUtils.hasText(taskCommand.getWorkDir()) ? taskCommand.getWorkDir() : defaultWorkDir));
			builder.redirectErrorStream(true);
			
			proc = builder.start();
			try(StreamGobler sg = new StreamGobler(proc.getInputStream())){
				if (goblerThreadEnable) {
					sg.start();
				}
				else {
					sg.run();
				}
				int e = proc.waitFor();
				log.info("["+taskCommand.getJobName()+"] Exit code: "+e);
				if (goblerThreadEnable) {
					sg.join(TimeUnit.SECONDS.toMillis(10));
				}
			}
		} 
		catch (IOException e) {
			log.error("Error while executing job - "+taskCommand.getJobName(), e);
		} 
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (proc != null) {
				proc.destroy();
			}
		}
	}
}