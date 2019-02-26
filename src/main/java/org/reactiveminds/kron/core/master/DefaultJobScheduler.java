package org.reactiveminds.kron.core.master;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.vo.ScheduleCommand;
import org.reactiveminds.kron.err.AllocationPolicyException;
import org.reactiveminds.kron.err.KronRuntimeException;
import org.reactiveminds.kron.model.JobEntry;
import org.reactiveminds.kron.model.JobRunEntry;
import org.reactiveminds.kron.model.JobRunEntry.RunState;
import org.reactiveminds.kron.scheduler.vo.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Lazy
class DefaultJobScheduler implements JobScheduler {

	private static final Logger log = LoggerFactory.getLogger("JobScheduler");
	private class ScheduleRunner implements Runnable{
		private final ScheduleCommand request;
		private ScheduleRunner(ScheduleCommand request) {
			super();
			this.request = request;
		}
		@Override
		public void run() {
			WorkerAllocationPolicy policy = beans.getBean(WorkerAllocationPolicy.class);
			try 
			{
				ScheduleCommand newRequest = request.copy();
				policy.allocate(newRequest);
				
				JobRunEntry runEntry = new JobRunEntry();
				runEntry.setExecId(service.getNextSequence(request.getJobName()));
				runEntry.setJobName(request.getJobName());
				runEntry.setKey();
				runEntry.setState(RunState.SUBMIT);
				
				newRequest.setExecutionId(runEntry.getDBKey());
				
				//TODO: transactional
				service.createJobRunEntry(runEntry);
				service.submitWorkerCommand(newRequest);
				log.debug("Job ["+request.getJobName()+"] scheduled run submitted ");
			} 
			catch (AllocationPolicyException e) {
				log.error("Unable to allocate worker: "+e);
				log.debug("", e);
			}
			
		}
	}
	private class SingleScheduleRunner extends ScheduleRunner implements ScheduledDaemon{
		private SingleScheduleRunner(ScheduleCommand request, Schedule schedule) {
			super(request);
			this.schedule = schedule;
		}
		private final Schedule schedule;
		@Override
		public Schedule getSchedule() {
			return schedule;
		}
		
	}
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.master.JobScheduler#scheduleJob(org.reactiveminds.kron.dto.JobEntry)
	 */
	@Override
	public void scheduleJob(JobEntry job) {
		cancelJob(job.getJobName());
		if(StringUtils.hasText(job.getCronSchedule())) {
			scheduleRepeatable(job);
		}
		else {
			scheduleAt(job);
		}
	}
	private void scheduleRepeatable(JobEntry job) {
		ScheduleCommand request = new ScheduleCommand();
		request.setExecution(job.getJob());
		request.setJobName(job.getJobName());
		(request).setCronExpr(job.getCronSchedule());
		
		long id = scheduler.schedule(new ScheduleRunner(request), job.getCronSchedule());
		scheduled.put(job.getJobName(), id);
		log.info("Scheduled ["+request.getJobName()+"] - "+job.getCronSchedule());
	}
	private void scheduleAt(JobEntry job) {
		ScheduleCommand request = new ScheduleCommand();
		request.setExecution(job.getJob());
		request.setJobName(job.getJobName());
		try {
			Schedule sched = new Schedule(new SimpleDateFormat(job.getDateFormat()).parse(job.getStartFrom()));
			long id = scheduler.schedule(new SingleScheduleRunner(request, sched));
			scheduled.put(job.getJobName(), id);
			log.info("Scheduled ["+request.getJobName()+"] - "+sched.getStartTime());
		} catch (ParseException e) {
			throw new KronRuntimeException("Error scheduling job at specific time", e);
		}
		
	}
	private ConcurrentMap<String, Long> scheduled = new ConcurrentHashMap<>();
	@Autowired
	private BeanFactory beans;
	@Autowired
	private SchedulingSupport scheduler;
	@Autowired
	private DistributionService service;
	@Override
	public void cancelJob(String jobName) {
		if(scheduled.containsKey(jobName)) {
			Long id = scheduled.remove(jobName);
			scheduler.cancel(id, true);
		}
	}
	
}
