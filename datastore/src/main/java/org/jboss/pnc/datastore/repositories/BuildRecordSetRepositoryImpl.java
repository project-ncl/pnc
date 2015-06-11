package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildRecordSetSpringRepository;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordSetRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildRecordSetRepositoryImpl extends AbstractRepository<BuildRecordSet, Integer> implements
        BuildRecordSetRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildRecordSetRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildRecordSetRepositoryImpl(BuildRecordSetSpringRepository buildRecordSetSpringRepository) {
        super(buildRecordSetSpringRepository, buildRecordSetSpringRepository);
    }
}
