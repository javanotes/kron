package org.reactiveminds.kron.master;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.LeaderElectNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class LeaderObserver implements CommandLineRunner, LeaderElectNotifier {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LeaderObserver.class);
	@Autowired
	DistributionService distService;
	@Autowired
	JobScheduler execService;
	@Autowired
	BeanFactory beans;
	@Override
	public void run(String... args) throws Exception {
		if(!distService.isWorkerNode()) {
			distService.registerLeaderCallback(this);
			distService.tryElectAsLeader();
		}
	}
	/**
	 * will be triggered whenever this node is selected as a leader
	 */
	@Override
	public void onElect(String message) {
		LeaderTaskRunner worker = beans.getBean(LeaderTaskRunner.class);
		worker.run();
	}
}
