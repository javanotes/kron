package org.reactiveminds.kron.scheduler.vo;

import java.util.concurrent.TimeUnit;

public class MilliSchedule extends Schedule {

	public MilliSchedule(long repeatAfter) {
		super(repeatAfter, TimeUnit.MILLISECONDS);
	}
}
