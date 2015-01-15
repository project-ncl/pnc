package org.jboss.pnc.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query("select u from Project u " +
            "inner join u.productVersionProjects p " +
            "where p.productVersion.product.id = ?1 " +
            "and " +
            "p.productVersion.id = ?2")
    List<Project> findByProductAndProductVerionId(Integer productId, Integer productVersionId);

    @Query("select u from Project u " +
            "inner join u.productVersionProjects p " +
            "where p.productVersion.product.id = ?1 " +
            "and " +
            "p.productVersion.id = ?2 " +
            "and " +
            "u.id = ?3")
    Project findByProductAndProductVersionIdAndProjectId(Integer productId, Integer productVersionId, Integer projectId);
}
