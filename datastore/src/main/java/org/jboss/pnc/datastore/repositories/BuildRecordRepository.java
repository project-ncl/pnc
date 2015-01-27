package org.jboss.pnc.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.BuildRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface BuildRecordRepository extends JpaRepository<BuildRecord, Integer> {

    @Query("select br from BuildRecord br where br.buildConfiguration.id = ?1")
    List<BuildRecord> findByBuildConfigurationId(Integer buildConfigurationId);
}
