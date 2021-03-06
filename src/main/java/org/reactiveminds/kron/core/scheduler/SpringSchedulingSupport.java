package org.reactiveminds.kron.core.scheduler;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import org.reactiveminds.kron.core.ScheduledDaemon;
import org.reactiveminds.kron.core.SchedulingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

class SpringSchedulingSupport implements SchedulingSupport {

	public static class FutureWrapper{
		private static AtomicLong counter = new AtomicLong();
		private FutureWrapper(Future<?> f, boolean scheduled) {
			super();
			this.f = f;
			this.scheduled = scheduled;
			id = counter.incrementAndGet();
		}
		public Future<?> getF() {
			return f;
		}
		public boolean isScheduled() {
			return scheduled;
		}
		private final Future<?> f;
		private final boolean scheduled;
		private final long id;
	}
	@Autowired
	private TaskScheduler scheduler;
	@Autowired
	private AsyncTaskExecutor executor;
	
	private final Map<Long, FutureWrapper> futures = new ConcurrentHashMap<>();
	
	@Override
	public long schedule(ScheduledDaemon daemon) {
		Schedule schedule = daemon.getSchedule();
		ScheduledFuture<?> f;
		if (schedule.getRepeatAfter() > 0) {
			f = scheduler.scheduleAtFixedRate(daemon, schedule.getStartTime(),
					schedule.getUnit().toMillis(schedule.getRepeatAfter()));
		}
		else
			f = scheduler.schedule(daemon, schedule.getStartTime());
		
		FutureWrapper wrap = new FutureWrapper(f, true);
		futures.put(wrap.id, wrap);
		return wrap.id;
	}

	@Override
	public long execute(Runnable daemon) {
		Future<?> f = executor.submit(daemon);
		FutureWrapper wrap = new FutureWrapper(f, true);
		futures.put(wrap.id, wrap);
		return wrap.id;
	}
	@Override
	public int executionCapacity() {
		Assert.isInstanceOf(ThreadPoolTaskExecutor.class, executor, "UnsupportedOperation - Executor not an instance of ThreadPoolTaskExecutor");
		return ((ThreadPoolTaskExecutor) executor).getMaxPoolSize() - ((ThreadPoolTaskExecutor) executor).getActiveCount();
	}

	@Override
	public long schedule(Runnable daemon, String cronExpr, Date startFrom) {
		ScheduledFuture<?> f = scheduler.schedule(daemon, new DelayingCronTrigger(cronExpr, startFrom));
		FutureWrapper wrap = new FutureWrapper(f, true);
		futures.put(wrap.id, wrap);
		return wrap.id;
	}

	@Override
	public void cancel(long id, boolean intrIfRunning) {
		if(futures.containsKey(id)) {
			futures.remove(id).getF().cancel(intrIfRunning);
		}
	}

	@PreDestroy
	public void destroy() throws Exception {
		for(Iterator<Entry<Long, FutureWrapper>> iter = futures.entrySet().iterator();iter.hasNext();) {
			Entry<Long, FutureWrapper> e = iter.next();
			e.getValue().getF().cancel(true);
			iter.remove();
		}
	}

	@Override
	public void awaitCompletion(long id, long timeout, TimeUnit unit) throws ExecutionException, TimeoutException {
		if(futures.containsKey(id)) {
			try {
				futures.get(id).getF().get(timeout, unit);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
