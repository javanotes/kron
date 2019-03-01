package org.reactiveminds.kron.core.grid;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.reactiveminds.kron.core.JobScheduler;
import org.reactiveminds.kron.core.JobScheduler.HKey;
import org.reactiveminds.kron.core.model.JobRunEntry;
import org.reactiveminds.kron.core.model.JobRunEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.MapStore;
/**
 * @author Sutanu_Dalui
 *
 */
class JobRunStore implements MapStore<String, JobRunEntry> {

	@Autowired
	JobRunEntryRepo repo;
	@Override
	public JobRunEntry load(String key) {
		HKey k = JobScheduler.extractExecutionKey(key);
		return repo.findByJobNameAndExecId(k.jobName, k.seq);
	}

	@Override
	public Map<String, JobRunEntry> loadAll(Collection<String> keys) {
		return StreamSupport.stream(keys.spliterator(), false)
		.map(s -> load(s))
		.collect(Collectors.toMap(j -> JobScheduler.makeExecutionKey(j.getJobName(), j.getExecId()), Function.identity()));
	}

	@Override
	public Iterable<String> loadAllKeys() {
		return StreamSupport.stream(repo.findAll().spliterator(), false)
		.map(j -> JobScheduler.makeExecutionKey(j.getJobName(), j.getExecId()))
		.collect(Collectors.toList());
	}

	@Override
	public void store(String key, JobRunEntry value) {
		repo.save(value);
	}

	@Override
	public void storeAll(Map<String, JobRunEntry> map) {
		repo.saveAll(map.values());
	}

	@Override
	public void delete(String key) {
		HKey k = JobScheduler.extractExecutionKey(key);
		repo.deleteByJobNameAndExecId(k.jobName, k.seq);
	}

	@Override
	public void deleteAll(Collection<String> keys) {
		keys.forEach(k -> delete(k));
	}

}
