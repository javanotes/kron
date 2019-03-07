package org.reactiveminds.kron.slave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.reactiveminds.kron.core.vo.ExecuteCommand;
import org.reactiveminds.kron.err.TaskExecuteException;
import org.reactiveminds.kron.utils.SystemStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

class ProcessExecutable implements Executable {
	private static final Logger log = LoggerFactory.getLogger("ProcessExecutable");
	
	private class StreamGobler extends Thread implements AutoCloseable{
		private final BufferedReader reader;
		private StreamGobler(InputStream stream) {
			super(command.getJobName());
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
	
	private String defaultWorkDir;
	public String getDefaultWorkDir() {
		return defaultWorkDir;
	}

	public void setDefaultWorkDir(String defaultWorkDir) {
		this.defaultWorkDir = defaultWorkDir;
	}

	private boolean goblerThreadEnable = false;
	public boolean isGoblerThreadEnable() {
		return goblerThreadEnable;
	}

	public void setGoblerThreadEnable(boolean goblerThreadEnable) {
		this.goblerThreadEnable = goblerThreadEnable;
	}

	ProcessExecutable() {
	}
	private int processId = 0;
	@Override
	public void execute()  {
		try {
			ProcessBuilder builder = new ProcessBuilder(command.getExecution().getJobCommand().split(" "));
			builder.directory(ResourceUtils.getFile(StringUtils.hasText(command.getExecution().getWorkDir()) ? command.getExecution().getWorkDir() : defaultWorkDir));
			builder.redirectErrorStream(true);
			process = builder.start();
			setProcessId(SystemStat.processId(process));
		} catch (IOException e) {
			throw new TaskExecuteException("Unable to spawn process", e);
		}
	}

	@Override
	public int awaitCompletion() throws InterruptedException {
		try(StreamGobler sg = new StreamGobler(process.getInputStream());){
			if (goblerThreadEnable) {
				sg.start();
			}
			else {
				sg.run();
			}
			int e = process.waitFor();
			log.info("["+command.getJobName()+"] Exit code: "+e);
			
			if (goblerThreadEnable) {
				sg.join(TimeUnit.SECONDS.toMillis(10));
			}
			return e;
		}
		
	}

	private Process process;
	public Process getProcess() {
		return process;
	}

	private ExecuteCommand command;
	@Override
	public void setCommand(ExecuteCommand command) {
		this.command = command;
	}

	@Override
	public void destroy() {
		if (process != null) {
			process.destroy();
		}
	}

	public int getProcessId() {
		return processId;
	}

	private void setProcessId(int processId) {
		this.processId = processId;
	}

}
