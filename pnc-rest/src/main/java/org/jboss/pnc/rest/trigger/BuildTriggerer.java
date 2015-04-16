package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildTriggerer {

    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    //to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator,
            final BuildConfigurationRepository buildConfigurationRepository,
            final BuildConfigurationSetRepository buildConfigurationSetRepository) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationSetRepository= buildConfigurationSetRepository;
    }

    public int triggerBuilds( final Integer configurationId )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.findOne(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersion() != null) {
            buildRecordSet.setProductMilestone(configuration.getProductVersion().getCurrentProductMilestone());
        }

        return buildCoordinator.build(configuration).getBuildConfiguration().getId();
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null, "Can't find configuration with given id=" + buildConfigurationSetId);

        return buildCoordinator.build(buildConfigurationSet).getId();
    }

}
