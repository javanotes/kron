package org.reactiveminds.kron.core;

import java.util.function.Predicate;

import org.reactiveminds.kron.core.model.JobEntry;
/**
 * 
 * @author Sutanu_Dalui
 *
 */
public interface JobEntryFilter extends Predicate<JobEntry> {

	JobEntryFilter ActiveJobFilter = e -> e.isEnabled();
	
	public static class JobNameLikeFilter implements JobEntryFilter{

		public JobNameLikeFilter(String pattern) {
			super();
			this.pattern = pattern;
		}
		private final String pattern;
		@Override
		public boolean test(JobEntry t) {
			return t.isEnabled() && t.getJobName().matches(pattern);
		}
		
	}
	public static class JobCommandContainsFilter implements JobEntryFilter{

		public JobCommandContainsFilter(String pattern) {
			super();
			this.pattern = pattern;
		}
		private final String pattern;
		@Override
		public boolean test(JobEntry t) {
			return t.isEnabled() && t.getJob().getJobCommand().contains(pattern);
		}
		
	}
	public static class JobCommandContainsIgnoreCaseFilter implements JobEntryFilter{

		public JobCommandContainsIgnoreCaseFilter(String pattern) {
			super();
			this.pattern = pattern.toUpperCase();
		}
		private final String pattern;
		@Override
		public boolean test(JobEntry t) {
			return t.isEnabled() && t.getJob().getJobCommand().toUpperCase().contains(pattern);
		}
		
	}
}
