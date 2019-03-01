package org.reactiveminds.kron.core.scheduler;

import java.util.concurrent.TimeUnit;

public class MilliSchedule extends Schedule {

	public MilliSchedule(long repeatAfter) {
		super(repeatAfter, TimeUnit.MILLISECONDS);
	}
}
