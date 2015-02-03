package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Project;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {

    @Query("select u from Project u " +
            "inner join u.productVersionProjects p " +
            "where p.productVersion.product.id = ?1 " +
            "and " +
            "p.productVersion.id = ?2")
    List<Project> findByProductAndProductVersionId(Integer productId, Integer productVersionId);

    @Query("select u from Project u " +
            "inner join u.productVersionProjects p " +
            "where p.productVersion.product.id = ?1 " +
            "and " +
            "p.productVersion.id = ?2 " +
            "and " +
            "u.id = ?3")
    Project findByProductAndProductVersionIdAndProjectId(Integer productVersionId, Integer projectId);
}
