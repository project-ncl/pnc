package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ArtifactRepository extends JpaRepository<Artifact, Integer>, QueryDslPredicateExecutor<Artifact> {

}
