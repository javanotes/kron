package org.reactiveminds.kron.core;

import org.reactiveminds.kron.dto.CommandAndTarget;
import org.springframework.scheduling.SchedulingException;
/**
 * The scheduling policy available as per configuration.
 * @author Sutanu_Dalui
 *
 */
public interface SchedulingPolicy {

	String name();
	void allocate(CommandAndTarget command) throws SchedulingException;
	
}
