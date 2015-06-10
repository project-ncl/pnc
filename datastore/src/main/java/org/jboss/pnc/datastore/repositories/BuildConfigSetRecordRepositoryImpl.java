package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigSetRecordSpringRepository;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildConfigSetRecordRepositoryImpl extends AbstractRepository<BuildConfigSetRecord, Integer> implements
        BuildConfigSetRecordRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigSetRecordRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildConfigSetRecordRepositoryImpl(BuildConfigSetRecordSpringRepository buildConfigSetRecordSpringRepository) {
        super(buildConfigSetRecordSpringRepository, buildConfigSetRecordSpringRepository);
    }
}
