package org.reactiveminds.kron.scheduler.vo;

import java.util.concurrent.TimeUnit;

public class SecSchedule extends Schedule {

	public SecSchedule(long repeatAfter) {
		super(repeatAfter, TimeUnit.SECONDS);
	}
}
