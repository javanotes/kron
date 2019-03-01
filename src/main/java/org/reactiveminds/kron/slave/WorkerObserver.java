package org.reactiveminds.kron.slave;

import org.reactiveminds.kron.core.Command;
import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.vo.CommandTarget;
import org.reactiveminds.kron.err.KronRuntimeException;
import org.reactiveminds.kron.core.MessageCallback;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class WorkerObserver implements CommandLineRunner, MessageCallback<CommandTarget> {

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
		try {
			if(distService.isWorkerNode()) {
				executor.schedule(beanFactory.getBean(SystemInfoDaemon.class));
				distService.registerWorkerChannel(this);
			}
		} catch (Exception e) {
			throw new KronRuntimeException("Exception encountered on starting worker", e);
		}
	}

	private boolean isMatchedTargetFilter(CommandTarget message) {
		return distService.isWorkerNode() && (distService.getSelfId().equals(message.getTargetPattern())
				|| message.getTargetPattern().equals(CommandTarget.TARGET_ALL));
	}
	@Override
	public void onMessage(CommandTarget message) {
		if(isMatchedTargetFilter(message)) {
			Command cmd = message.getCommand();
			switch(cmd) {
				case UNDEF:
					break;
				case SYSTEMSTAT:
					beanFactory.getBean(SystemInfoDaemon.class).run();
					distService.countdownLatch(cmd.name());
					break;
				default:
					TaskRunner runner = beanFactory.getBean(TaskRunner.class, message);
					runner.setDefaultWorkDir(workDir);
					executor.execute(runner);
					break;
			}
		}
		
	}

}
