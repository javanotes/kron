package org.reactiveminds.kron.core.impl;

import java.util.Collection;
import java.util.Map;

import org.reactiveminds.kron.model.JobEntry;

import com.hazelcast.core.MapStore;

class JobMasterStore implements MapStore<String, JobEntry> {

	@Override
	public JobEntry load(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, JobEntry> loadAll(Collection<String> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<String> loadAllKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void store(String key, JobEntry value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeAll(Map<String, JobEntry> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		// TODO Auto-generated method stub
		
	}

}
