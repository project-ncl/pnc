package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildConfigurationAuditedRepository extends JpaRepository<BuildConfigurationAudited, IdRev>, QueryDslPredicateExecutor<BuildConfigurationAudited> {

}
