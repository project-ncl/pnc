/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigSetRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

@Stateless
public class BuildConfigSetRecordRepositoryImpl extends AbstractRepository<BuildConfigSetRecord, Base32LongID>
        implements BuildConfigSetRecordRepository {

    private static final EnumSet<BuildStatus> IN_PROGRESS_STATES = BuildStatus.unfinished();
    EntityManager manager;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigSetRecordRepositoryImpl() {
        super(BuildConfigSetRecord.class, Base32LongID.class);
    }

    @Inject
    public BuildConfigSetRecordRepositoryImpl(EntityManager manager) {
        super(BuildConfigSetRecord.class, Base32LongID.class);
        this.manager = manager;
    }

    @Override
    public BuildConfigSetRecord save(BuildConfigSetRecord entity) {
        if (entity.getId() == null) {
            Base32LongID id = new Base32LongID(Sequence.nextId());
            entity.setId(id);
        }
        return super.save(entity);
    }

    @Override
    public List<BuildConfigSetRecord> findTemporaryBuildConfigSetRecordsOlderThan(Date date) {
        return queryWithPredicates(
                BuildConfigSetRecordPredicates.temporaryBuild(),
                BuildConfigSetRecordPredicates.buildFinishedBefore(date));
    }

    @Override
    public List<BuildConfigSetRecord> findBuildConfigSetRecordsInProgress() {
        return queryWithPredicates(BuildConfigSetRecordPredicates.inStates(IN_PROGRESS_STATES));
    }
}
