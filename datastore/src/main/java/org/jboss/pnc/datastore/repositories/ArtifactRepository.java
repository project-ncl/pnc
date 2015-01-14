package org.jboss.pnc.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArtifactRepository extends JpaRepository<Artifact, Integer> {

    @Query("select u from Artifact u where u.buildRecord.id = ?1")
    List<Artifact> findByBuildRecord(Integer buildRecordId);

}
