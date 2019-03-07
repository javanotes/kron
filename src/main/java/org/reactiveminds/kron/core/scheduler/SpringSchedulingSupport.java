package org.reactiveminds.kron.core.scheduler;

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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.concurrent.ListenableFuture;

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
	private ThreadPoolTaskExecutor executor;
	
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
		ListenableFuture<?> f = executor.submitListenable(daemon);
		FutureWrapper wrap = new FutureWrapper(f, true);
		futures.put(wrap.id, wrap);
		return wrap.id;
	}
	@Override
	public int executionCapacity() {
		return executor.getMaxPoolSize() - executor.getActiveCount();
	}

	@Override
	public long schedule(Runnable daemon, String cronExpr) {
		ScheduledFuture<?> f = scheduler.schedule(daemon, new CronTrigger(cronExpr));
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
