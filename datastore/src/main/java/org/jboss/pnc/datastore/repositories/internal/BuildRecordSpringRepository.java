/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import javax.enterprise.context.Dependent;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Dependent
public interface BuildRecordSpringRepository
        extends JpaRepository<BuildRecord, Base32LongID>, JpaSpecificationExecutor<BuildRecord> {

    @Query("SELECT br FROM BuildRecord br WHERE br.submitTime = (SELECT max(brr.submitTime) FROM BuildRecord brr"
            + " WHERE br.buildConfigurationId = brr.buildConfigurationId) AND br.buildConfigurationId IN ?1")
    List<BuildRecord> getLatestBuildsByBuildConfigIds(List<Integer> configIds);

    @Query("select br from BuildRecord br fetch all properties where br.id = ?1")
    BuildRecord findByIdFetchAllProperties(Base32LongID id);

    @Query("select br from BuildRecord br " + "left join fetch br.productMilestone "
            + "left join fetch br.buildConfigSetRecord " + "left join fetch br.user " + "where br.id = ?1")
    BuildRecord findByIdFetchProperties(Base32LongID id);

    @Query("SELECT DISTINCT br FROM BuildRecord br " + "JOIN br.builtArtifacts builtArtifacts "
            + "WHERE builtArtifacts.id IN (?1)")
    Set<BuildRecord> findByBuiltArtifacts(Set<Integer> dependenciesIds);

    @Query(
            value = "SELECT buildrecord_id, buildcontentid, submittime, starttime, endtime, lastupdatetime,"
                    + " submit_year, submit_month, submit_quarter,"
                    + " status, temporarybuild, autoalign, brewpullactive, buildtype,"
                    + " executionrootname, executionrootversion, user_id, username,"
                    + " buildconfiguration_id, buildconfiguration_rev, buildconfiguration_name,"
                    + " buildconfigsetrecord_id, productmilestone_id, productmilestone_version,"
                    + " project_id, project_name, productversion_id, product_version, product_id, product_name"
                    + " FROM _archived_buildrecords WHERE lastupdatetime > ?1 "
                    + " ORDER BY lastupdatetime ASC LIMIT ?2 OFFSET ?3",
            nativeQuery = true)
    List<Object[]> getAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime, int pageSize, int offset);

    @Query(
            value = "SELECT COUNT(DISTINCT buildrecord_id) "
                    + " FROM _archived_buildrecords WHERE lastupdatetime > ?1 ",
            nativeQuery = true)
    int countAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime);
}
