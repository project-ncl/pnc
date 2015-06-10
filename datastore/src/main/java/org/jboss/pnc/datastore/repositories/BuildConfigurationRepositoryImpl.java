package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationSpringRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildConfigurationRepositoryImpl extends AbstractRepository<BuildConfiguration, Integer> implements
        BuildConfigurationRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigurationRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildConfigurationRepositoryImpl(BuildConfigurationSpringRepository buildConfigurationSpringRepository) {
        super(buildConfigurationSpringRepository, buildConfigurationSpringRepository);
    }
}
