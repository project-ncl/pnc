package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildTriggerer {

    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;

    //to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator, final BuildConfigurationRepository buildConfigurationRepository) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
    }

    public int triggerBuilds( final Integer configurationId )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.findOne(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        buildRecordSet.setProductVersion(configuration.getProductVersion());

        return buildCoordinator.build(configuration).getBuildConfiguration().getId();
    }

}
