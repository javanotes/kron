package org.reactiveminds.kron.model;

import com.hazelcast.core.PartitionAware;

public interface PartitionKeyed extends PartitionAware<String> {

	String getDBKey();
}
