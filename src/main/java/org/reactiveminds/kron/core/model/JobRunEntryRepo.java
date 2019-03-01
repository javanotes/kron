package org.reactiveminds.kron.core.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface JobRunEntryRepo extends CrudRepository<JobRunEntry, Long> {

	JobRunEntry findByJobNameAndExecId(String name, long id);
	long deleteByJobNameAndExecId(String name, long id);
}
