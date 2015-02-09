package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ProjectRepository extends JpaRepository<Project, Integer>, QueryDslPredicateExecutor<Project> {

}
