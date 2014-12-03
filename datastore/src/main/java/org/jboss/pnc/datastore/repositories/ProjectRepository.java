package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
