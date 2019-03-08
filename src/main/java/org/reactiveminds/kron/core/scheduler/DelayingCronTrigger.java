package org.reactiveminds.kron.core.scheduler;

import java.util.Date;
import java.util.TimeZone;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronSequenceGenerator;

class DelayingCronTrigger implements Trigger {
	//copied from CronTrigger
	
	private final CronSequenceGenerator sequenceGenerator;
	private final String expr;

	private Date initiateFrom;
	/**
	 * Build a {@link CronTrigger} from the pattern provided in the default time zone.
	 * @param expression a space-separated list of time fields, following cron
	 * expression conventions
	 */
	public DelayingCronTrigger(String expression) {
		this.expr = expression;
		this.sequenceGenerator = new CronSequenceGenerator(expression);
	}

	/**
	 * Build a {@link CronTrigger} from the pattern provided in the given time zone.
	 * @param expression a space-separated list of time fields, following cron
	 * expression conventions
	 * @param timeZone a time zone in which the trigger times will be generated
	 */
	public DelayingCronTrigger(String expression, TimeZone timeZone) {
		this.expr = expression;
		this.sequenceGenerator = new CronSequenceGenerator(expression, timeZone);
	}


	public DelayingCronTrigger(String cronExpr, Date startFrom) {
		this(cronExpr);
		this.initiateFrom = startFrom;
	}

	/**
	 * Return the cron pattern that this trigger has been built with.
	 */
	public String getExpression() {
		return expr;
	}


	/**
	 * Determine the next execution time according to the given trigger context.
	 * <p>Next execution times are calculated based on the
	 * {@linkplain TriggerContext#lastCompletionTime completion time} of the
	 * previous execution; therefore, overlapping executions won't occur.
	 */
	@Override
	public Date nextExecutionTime(TriggerContext triggerContext) {
		Date date = triggerContext.lastCompletionTime();
		if (date != null) {
			Date scheduled = triggerContext.lastScheduledExecutionTime();
			if (scheduled != null && date.before(scheduled)) {
				// Previous task apparently executed too early...
				// Let's simply use the last calculated execution time then,
				// in order to prevent accidental re-fires in the same second.
				date = scheduled;
			}
		}
		else {
			date = initiateFrom == null ? new Date() : initiateFrom;
		}
		return this.sequenceGenerator.next(date);
	}


	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof DelayingCronTrigger &&
				this.sequenceGenerator.equals(((DelayingCronTrigger) other).sequenceGenerator)));
	}

	@Override
	public int hashCode() {
		return this.sequenceGenerator.hashCode();
	}

	@Override
	public String toString() {
		return this.sequenceGenerator.toString();
	}

	public Date getInitiateFrom() {
		return initiateFrom;
	}
}
