package org.jboss.pnc.datastore;

import org.jboss.pnc.model.ProjectBuildResult;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProjectBuildResultRepository extends JpaRepository<ProjectBuildResult, Integer> {
}
