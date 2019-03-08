package org.reactiveminds.kron.master;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.reactiveminds.kron.core.WorkerAllocationPolicy;
import org.reactiveminds.kron.core.model.JobEntry;
import org.reactiveminds.kron.core.model.JobRunEntry;
import org.reactiveminds.kron.core.model.RunState;
import org.reactiveminds.kron.core.scheduler.Schedule;
import org.reactiveminds.kron.core.vo.ScheduleCommand;
import org.reactiveminds.kron.err.AllocationPolicyException;
import org.reactiveminds.kron.err.KronRuntimeException;
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
	/**
	 * 
	 * @author Sutanu_Dalui
	 *
	 */
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
				newRequest.setEmbeddedExec(request.isEmbeddedExec());
				policy.allocate(newRequest);
				
				JobRunEntry runEntry = new JobRunEntry();
				runEntry.setExecId(service.getNextSequence(request.getJobName()));
				runEntry.setJobName(request.getJobName());
				runEntry.setKey();
				runEntry.setState(RunState.SUBMIT);
				runEntry.setInstanceId(newRequest.getTargetPattern());
				newRequest.setExecutionId(runEntry.getDBKey());
				enrich(runEntry);
				
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
		protected void enrich(JobRunEntry runEntry) {
			// do nothing
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
		@Override
		protected void enrich(JobRunEntry runEntry) {
			runEntry.setState(RunState.SCHEDULED);
		}
	}
	/* (non-Javadoc)
	 * @see org.reactiveminds.kron.master.JobScheduler#scheduleJob(org.reactiveminds.kron.dto.JobEntry)
	 */
	@Override
	public void scheduleJob(JobEntry job) {
		cancelJob(job.getJobName());
		
		ScheduleCommand request = new ScheduleCommand();
		request.setExecution(job.getJob());
		request.setJobName(job.getJobName());
		request.setEmbeddedExec(job.isEmbeddedExec());
		
		if(StringUtils.hasText(job.getCronSchedule())) {
			scheduleRepeatable(request, job);
		}
		else {
			scheduleAt(request, job);
		}
	}
	private void scheduleRepeatable(ScheduleCommand request, JobEntry job) {
		request.setCronExpr(job.getCronSchedule());
		Date schedAt = null;
		try {
			schedAt = parseScheduleAtDate(job);
		} catch (Exception e) {
			// ignore
		}
		long id = scheduler.schedule(new ScheduleRunner(request), job.getCronSchedule(), schedAt);
		scheduled.put(job.getJobName(), id);
		log.info("Scheduled for ["+request.getJobName()+"] - "+job.getCronSchedule());
	}
	private static Date parseScheduleAtDate(JobEntry job) throws ParseException {
		if(job.getDateFormat().startsWith(TODAY)) {
			String timeFormat = job.getDateFormat().substring(TODAY.length()).trim();
			Calendar at = GregorianCalendar.getInstance();
			at.setTime(new SimpleDateFormat(timeFormat).parse(job.getStartFrom()));
			Calendar now = GregorianCalendar.getInstance();
			at.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
			return at.getTime();
		}
		else if(job.getDateFormat().startsWith(NOW)) {
			return new Date();
		}
		return new SimpleDateFormat(job.getDateFormat()).parse(job.getStartFrom());
	}
	private void scheduleAt(ScheduleCommand request, JobEntry job) {
		try {
			Schedule sched = new Schedule(parseScheduleAtDate(job));
			long id = scheduler.schedule(new SingleScheduleRunner(request, sched));
			scheduled.put(job.getJobName(), id);
			log.info("Scheduled at ["+request.getJobName()+"] - "+sched.getStartTime());
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
