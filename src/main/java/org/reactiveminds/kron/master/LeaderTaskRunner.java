package org.reactiveminds.kron.master;

import java.util.List;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobEntryFilter;
import org.reactiveminds.kron.core.JobEntryListener;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.model.JobEntry;
import org.reactiveminds.kron.core.model.JobEntryRepo;
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
	@Autowired
	JobEntryRepo jobRepo;
	private static Logger log = LoggerFactory.getLogger("LeaderTaskRunner");
	@Override
	public void run() {
		try {
			List<JobEntry> jobs = distService.getJobEntries(JobEntryFilter.ActiveJobFilter);
			for(JobEntry job : jobs) {
				try {
					jobRegistry.scheduleJob(job);
				} catch (Exception e) {
					log.error("Cannot schedule saved task - "+job.getJobName()+": "+e.getMessage());
					log.debug("",e);
				}
			}
			distService.setJobListener(this);
			log.debug("Yay! I am the leader now ");
		} catch (Exception e) {
			log.error("Exception encountered on starting leader", e);
		}
		
	}
	@Override
	public void onEntryAdded(JobEntry job) {
		//TODO transactional
		jobRepo.save(job);
		jobRegistry.scheduleJob(job);
	}
	
}
