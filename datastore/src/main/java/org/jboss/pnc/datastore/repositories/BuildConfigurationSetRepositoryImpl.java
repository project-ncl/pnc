package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationSetSpringRepository;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildConfigurationSetRepositoryImpl extends AbstractRepository<BuildConfigurationSet, Integer> implements
        BuildConfigurationSetRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigurationSetRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildConfigurationSetRepositoryImpl(BuildConfigurationSetSpringRepository buildConfigurationSetSpringRepository) {
        super(buildConfigurationSetSpringRepository, buildConfigurationSetSpringRepository);
    }
}
