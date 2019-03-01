package org.reactiveminds.kron.core;

import org.reactiveminds.kron.core.model.JobEntry;
/**
 * A callback interface whenever a new job is submitted over REST
 * @author Sutanu_Dalui
 *
 */
@FunctionalInterface
public interface JobEntryListener {
	/**
	 * 
	 * @param key
	 * @param value
	 */
	void onEntryAdded(JobEntry value);
}
