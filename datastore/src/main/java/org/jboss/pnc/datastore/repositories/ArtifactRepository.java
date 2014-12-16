package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArtifactRepository extends JpaRepository<Artifact, Integer> {

    @Query("select u from Artifact u where u.projectBuildResult.id = ?1")
    List<Artifact> findByProjectBuildResult(Integer projectBuildResultId);

}
