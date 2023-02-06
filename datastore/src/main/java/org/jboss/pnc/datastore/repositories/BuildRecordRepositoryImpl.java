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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.buildFinishedBefore;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.includeTemporary;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.temporaryBuild;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdRev;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withCausingBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withSuccess;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutImplicitDependants;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutLinkedNRRRecordOlderThanTimestamp;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Base32LongID>
        implements BuildRecordRepository {

    private static final Logger logger = LoggerFactory.getLogger(BuildRecordRepositoryImpl.class);

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildRecordRepositoryImpl() {
        super(BuildRecord.class, Base32LongID.class);
    }

    @Inject
    public BuildRecordRepositoryImpl(BuildConfigurationAuditedRepository buildConfigurationAuditedRepository) {
        super(BuildRecord.class, Base32LongID.class);
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
    }

    @Override
    public BuildRecord findByIdFetchAllProperties(Base32LongID id) {
        TypedQuery<BuildRecord> query = entityManager
                .createQuery("select br from BuildRecord br fetch all properties where br.id = :id", BuildRecord.class);
        query.setParameter("id", id);
        try {
            BuildRecord buildRecord = query.getSingleResult();
            fetchBuildConfigurationAudited(buildRecord);
            return buildRecord;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public BuildRecord findByIdFetchProperties(Base32LongID id) {
        TypedQuery<BuildRecord> query = entityManager.createQuery(
                "select br from BuildRecord br "
                        + "left join fetch br.productMilestone left join fetch br.buildConfigSetRecord left join fetch br.user "
                        + "where br.id = :id",
                BuildRecord.class);
        query.setParameter("id", id);
        try {
            BuildRecord buildRecord = query.getSingleResult();
            fetchBuildConfigurationAudited(buildRecord);
            return buildRecord;
        } catch (NoResultException ex) {
            return null;
        }
    }

    private void fetchBuildConfigurationAudited(BuildRecord buildRecord) {
        Integer revision = buildRecord.getBuildConfigurationRev();
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .queryById(new IdRev(buildRecord.getBuildConfigurationId(), revision));
        buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId, boolean temporaryBuild) {
        List<BuildRecord> buildRecords = queryWithBuildConfigurationId(configurationId);
        return getLatestSuccessfulBuildRecord(buildRecords, temporaryBuild);
    }

    @Override
    public BuildRecord getAnyLatestSuccessfulBuildRecordWithBuildConfig(
            Integer configurationId,
            boolean temporaryBuild) {

        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.submitTime.getName());
        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationId(configurationId),
                withSuccess(),
                includeTemporary(temporaryBuild));
        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId) {
        return queryWithPredicates(withBuildConfigurationId(configurationId));
    }

    @Override
    public List<BuildRecord> findIndependentTemporaryBuildsOlderThan(Date date) {
        return queryWithPredicates(
                temporaryBuild(),
                buildFinishedBefore(date),
                withoutImplicitDependants(),
                withoutLinkedNRRRecordOlderThanTimestamp(date));
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(IdRev idRev, boolean temporaryBuild) {
        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.id.getName());

        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationIdRev(idRev),
                withSuccess(),
                includeTemporary(idRev, temporaryBuild));

        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public BuildRecord getAnyLatestSuccessfulBuildRecordWithRevision(IdRev idRev, boolean temporaryBuild) {
        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.submitTime.getName());

        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationIdRev(idRev),
                withSuccess(),
                includeTemporary(temporaryBuild));

        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public BuildRecord getPreferredLatestSuccessfulBuildRecordWithRevision(
            IdRev idRev,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.submitTime.getName());

        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationIdRev(idRev),
                withSuccess(),
                includeTemporary(idRev, temporaryBuild, alignmentPreference));

        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public BuildRecord getPreferredLatestSuccessfulBuildRecordWithBuildConfig(
            Integer configurationId,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.submitTime.getName());
        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationId(configurationId),
                withSuccess(),
                includeTemporary(configurationId, temporaryBuild, alignmentPreference));
        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public List<BuildRecord> getLatestBuildsForBuildConfigs(List<Integer> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            return Collections.emptyList();
        }
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM buildrecord br INNER JOIN ("
                        + "   SELECT buildconfiguration_id, max(submittime) AS max_submit FROM buildrecord"
                        + "   GROUP BY buildconfiguration_id ) brr"
                        + " ON  br.buildconfiguration_id = brr.buildconfiguration_id"
                        + " AND br.submittime = brr.max_submit AND br.buildconfiguration_id IN (:ids)",
                BuildRecord.class);
        query.setParameter("ids", configIds);
        return query.getResultList();
    }

    @Override
    public Set<BuildRecord> findByBuiltArtifacts(Set<Integer> artifactsIds) {
        if (artifactsIds == null || artifactsIds.isEmpty()) {
            return Collections.emptySet();
        }
        TypedQuery<BuildRecord> query = entityManager.createQuery(
                "SELECT DISTINCT br FROM BuildRecord br JOIN br.builtArtifacts builtArtifacts "
                        + "WHERE builtArtifacts.id IN (:ids)",
                BuildRecord.class);
        query.setParameter("ids", artifactsIds);
        return query.getResultStream().collect(Collectors.toSet());
    }

    @Override
    public List<BuildRecord> getBuildByCausingRecord(Base32LongID causingRecordId) {
        return queryWithPredicates(withCausingBuildRecordId(causingRecordId));
    }

    @Override
    public List<Object[]> getAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime, int pageSize, int offset) {
        Query query = entityManager.createNativeQuery(
                "SELECT buildrecord_id, buildcontentid, submittime, starttime, endtime, lastupdatetime,"
                        + " submit_year, submit_month, submit_quarter,"
                        + " status, temporarybuild, autoalign, brewpullactive, buildtype,"
                        + " executionrootname, executionrootversion, user_id, username,"
                        + " buildconfiguration_id, buildconfiguration_rev, buildconfiguration_name,"
                        + " buildconfigsetrecord_id, productmilestone_id, productmilestone_version,"
                        + " project_id, project_name, productversion_id, product_version, product_id, product_name"
                        + " FROM _archived_buildrecords WHERE lastupdatetime > :lastupdatetime "
                        + " ORDER BY lastupdatetime ASC LIMIT :pageSize OFFSET :offset");
        query.setParameter("lastupdatetime", lastupdatetime);
        query.setParameter("pageSize", pageSize);
        query.setParameter("offset", offset);
        return query.getResultList();
    }

    @Override
    public int countAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT buildrecord_id) "
                        + " FROM _archived_buildrecords WHERE lastupdatetime > :lastupdatetime ");
        query.setParameter("lastupdatetime", lastupdatetime);
        return (Integer) query.getSingleResult();
    }
}
