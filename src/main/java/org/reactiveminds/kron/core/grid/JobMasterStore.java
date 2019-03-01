package org.reactiveminds.kron.core.grid;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.reactiveminds.kron.core.model.JobEntry;
import org.reactiveminds.kron.core.model.JobEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.MapStore;
/**
 * @deprecated
 * @author Sutanu_Dalui
 *
 */
class JobMasterStore implements MapStore<String, JobEntry> {

	@Autowired
	JobEntryRepo repository;
	@Override
	public JobEntry load(String key) {
		return repository.findById(key).get();
	}

	@Override
	public Map<String, JobEntry> loadAll(Collection<String> keys) {
		return StreamSupport.stream(repository.findAllById(keys).spliterator(), false)
		.collect(Collectors.toMap(JobEntry::getJobName, Function.identity()));
	}

	@Override
	public Iterable<String> loadAllKeys() {
		return StreamSupport.stream(repository.findAll().spliterator(), false)
				.map(JobEntry::getJobName)
				.collect(Collectors.toList());
	}

	@Override
	public void store(String key, JobEntry value) {
		repository.save(value);
	}

	@Override
	public void storeAll(Map<String, JobEntry> map) {
		repository.saveAll(map.values());
	}

	@Override
	public void delete(String key) {
		repository.deleteById(key);
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		for(String k : keys)
			delete(k);
	}

}
