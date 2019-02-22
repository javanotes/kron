package org.reactiveminds.kron.core.slave;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.MessageCallback;
import org.reactiveminds.kron.dto.Command;
import org.reactiveminds.kron.dto.CommandAndTarget;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class WorkerObserver implements CommandLineRunner, MessageCallback<CommandAndTarget> {

	@Autowired
	DistributionService distService;
	@Autowired
	BeanFactory beanFactory;
	@Autowired
	SchedulingSupport executor;
	@Value("${kron.worker.workDirectory:/usr/home}")
	private String workDir;
		
	@Override
	public void run(String... args) throws Exception {
		if(distService.isWorkerNode()) {
			executor.schedule(beanFactory.getBean(SystemInfoDaemon.class));
			distService.registerWorkerChannel(this);
		}
	}
	private boolean isMatchedTargetFilter(CommandAndTarget message) {
		return distService.isWorkerNode() && distService.getSelfId().equals(message.getTargetPattern());
	}
	@Override
	public void onMessage(CommandAndTarget message) {
		if(isMatchedTargetFilter(message)) {
			Command cmd = message.getCommand();
			switch(cmd) {
				case NEWJOB:
					TaskRunner runner = beanFactory.getBean(TaskRunner.class, message.getRequest());
					runner.setDefaultWorkDir(workDir);
					executor.execute(runner);
					break;
				case SYSTEMSTAT:
					beanFactory.getBean(SystemInfoDaemon.class).run();
					distService.countdownLatch(cmd.name());
					break;
				default:
					break;
			
			}
		}
		
	}

}
