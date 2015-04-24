package org.jboss.pnc.rest.trigger;

import java.util.List;

import com.google.common.base.Preconditions;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildTriggerer {

    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Deprecated //not meant for usage its only to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator,
            final BuildConfigurationRepository buildConfigurationRepository,
            final BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            final BuildConfigurationSetRepository buildConfigurationSetRepository) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigurationSetRepository= buildConfigurationSetRepository;
    }

    public int triggerBuilds( final Integer configurationId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.findOne(configurationId);
        configuration.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(configurationId));

        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersion() != null) {
            buildRecordSet.setProductMilestone(configuration.getProductVersion().getCurrentProductMilestone());
        }

        return buildCoordinator.build(configuration, currentUser).getBuildConfiguration().getId();
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null, "Can't find configuration with given id=" + buildConfigurationSetId);

        for (BuildConfiguration config : buildConfigurationSet.getBuildConfigurations()) {
            config.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(config.getId()));
        }
        return buildCoordinator.build(buildConfigurationSet, currentUser).getId();
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     * 
     * @param buildConfigurationId
     * @return The latest revision of the given build configuration
     */
    private BuildConfigurationAudited getLatestAuditedBuildConfiguration(Integer buildConfigurationId) {
        List<BuildConfigurationAudited> buildConfigRevs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(buildConfigurationId);
        if ( buildConfigRevs.isEmpty() ) {
            // TODO should we throw an exception?  This should never happen.
            return null;
        }
        return buildConfigRevs.get(0);
    }
}
