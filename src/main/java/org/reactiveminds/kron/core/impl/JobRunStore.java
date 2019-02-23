package org.reactiveminds.kron.core.impl;

import java.util.Collection;
import java.util.Map;

import org.reactiveminds.kron.model.JobRunEntry;

import com.hazelcast.core.MapStore;

class JobRunStore implements MapStore<Long, JobRunEntry> {


	@Override
	public JobRunEntry load(Long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, JobRunEntry> loadAll(Collection<Long> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Long> loadAllKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void store(Long key, JobRunEntry value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeAll(Map<Long, JobRunEntry> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Long key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(Collection<Long> keys) {
		// TODO Auto-generated method stub
		
	}

}
