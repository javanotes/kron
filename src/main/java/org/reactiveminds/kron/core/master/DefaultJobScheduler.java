package org.reactiveminds.kron.core.master;

import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.SchedulingPolicy;
import org.reactiveminds.kron.dto.Command;
import org.reactiveminds.kron.dto.CommandAndTarget;
import org.reactiveminds.kron.model.ExecutionRequest;
import org.reactiveminds.kron.model.JobEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Service;

@Service
@Lazy
class DefaultJobScheduler implements JobScheduler {

	private static final Logger log = LoggerFactory.getLogger("JobScheduler");
	private class ScheduleRunner implements Runnable{
		private final ExecutionRequest request;
		private ScheduleRunner(ExecutionRequest request) {
			super();
			this.request = request;
		}
		@Override
		public void run() {
			CommandAndTarget cmd = new CommandAndTarget();
			cmd.setCommand(Command.NEWJOB);
			cmd.setRequest(request);
			SchedulingPolicy policy = beans.getBean(SchedulingPolicy.class);
			try {
				policy.allocate(cmd);
				log.info("Job ["+request.getJobName()+"] scheduled run submitted ");
			} catch (SchedulingException e) {
				log.error("Scheduling exception: "+e);
				log.debug("", e);
			}
			//TODO: update status
		}
		
	}
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.master.JobScheduler#scheduleJob(org.reactiveminds.kron.dto.JobEntry)
	 */
	@Override
	public void scheduleJob(JobEntry job) {
		scheduler.schedule(new ScheduleRunner(job.getJob()), job.getJob().getCronSchedule());
		log.info("Scheduled ["+job.getJob().getJobName()+"] - "+job.getJob().getCronSchedule());
	}
	@Autowired
	private BeanFactory beans;
	@Autowired
	private SchedulingSupport scheduler;
	
}
