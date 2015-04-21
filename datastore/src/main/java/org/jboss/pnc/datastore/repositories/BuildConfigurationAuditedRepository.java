package org.jboss.pnc.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildConfigurationAuditedRepository extends JpaRepository<BuildConfigurationAudited, IdRev>, QueryDslPredicateExecutor<BuildConfigurationAudited> {

    /**
     * Get all the revisions of a specific build configuration in order of newest to oldest.
     * 
     * @param id of the build configuration
     * @return The list of revisions of this build config in order of newest to oldest.
     */
    public List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id);

}
