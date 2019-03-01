package org.reactiveminds.kron.core.model;

import com.hazelcast.core.PartitionAware;

public interface PartitionKeyed extends PartitionAware<String> {

	String getDBKey();
}
