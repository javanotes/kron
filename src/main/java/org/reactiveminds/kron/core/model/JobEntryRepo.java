package org.reactiveminds.kron.core.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface JobEntryRepo extends CrudRepository<JobEntry, String> {

}
