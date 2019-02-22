package org.reactiveminds.kron.core.master;

import java.util.List;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobEntryFilter;
import org.reactiveminds.kron.core.JobEntryListener;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.model.JobEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Lazy
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class LeaderTaskRunner implements Runnable, JobEntryListener {
	@Autowired
	DistributionService distService;
	@Autowired
	BeanFactory beans;
	@Autowired
	JobScheduler jobRegistry;
	
	private static Logger log = LoggerFactory.getLogger(LeaderTaskRunner.class);
	@Override
	public void run() {
		List<JobEntry> jobs = distService.getJobEntries(JobEntryFilter.ActiveJobFilter);
		for(JobEntry job : jobs) {
			jobRegistry.scheduleJob(job);
		}
		distService.setJobListener(this);
		log.debug("Yay! I am the leader now ");
		
		/*ExecutionRequest req = new ExecutionRequest();
		req.setJobCommand("dir/p/o");
		req.setJobName("List Dir");
		req.setMinMemoryMb(64);
		req.setNumOfCores(1);
		
		JobEntry j = new JobEntry();
		j.setEnabled(true);
		j.setJob(req);
		
		System.out.println(JsonMapper.toString(j));*/
	}
	@Override
	public void onEntryAdded(JobEntry job) {
		jobRegistry.scheduleJob(job);
	}
	
}
