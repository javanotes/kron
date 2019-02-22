package org.reactiveminds.kron.scheduler.vo;

import java.util.concurrent.TimeUnit;

public class HourSchedule extends Schedule {

	public HourSchedule(long repeatAfter) {
		super(repeatAfter, TimeUnit.HOURS);
	}
}
