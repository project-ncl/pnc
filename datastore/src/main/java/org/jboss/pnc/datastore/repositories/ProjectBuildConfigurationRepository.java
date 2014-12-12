package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProjectBuildConfigurationRepository extends JpaRepository<ProjectBuildConfiguration, Integer> {

    @Query("select u from ProjectBuildConfiguration u where u.project.id = ?1")
    List<ProjectBuildConfiguration> findByProjectId(Integer projectId);

    @Query("select u from ProjectBuildConfiguration u where u.project.id = ?1 and u.id = ?2")
    ProjectBuildConfiguration findByProjectIdAndConfigurationId(Integer projectId, Integer id);
}
