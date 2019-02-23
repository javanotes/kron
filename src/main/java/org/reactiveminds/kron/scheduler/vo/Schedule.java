package org.reactiveminds.kron.scheduler.vo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

public class Schedule{
	/**
	 * Constructor for a repeatable schedule
	 * @param startTime
	 * @param repeatAfter
	 * @param unit
	 */
	public Schedule(Date startTime, long repeatAfter, TimeUnit unit) {
		super();
		this.startTime = startTime == null ? new Date() : startTime;
		this.repeatAfter = repeatAfter;
		this.unit = unit;
	}
	/**
	 * Constructor for a one shot scheduling
	 * @param startTime
	 */
	public Schedule(Date startTime) {
		Assert.notNull(startTime, "Scheduled time reqd");
		this.startTime = startTime;
		this.repeatAfter = -1;
	}
	Schedule(long repeatAfter, TimeUnit unit) {
		this(null, repeatAfter, unit);
	}
	public Date getStartTime() {
		return startTime;
	}
	
	public long getRepeatAfter() {
		return repeatAfter;
	}
	
	public TimeUnit getUnit() {
		return unit;
	}
	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}
	private final Date startTime;
	private final long repeatAfter;
	private TimeUnit unit = TimeUnit.MILLISECONDS;
}