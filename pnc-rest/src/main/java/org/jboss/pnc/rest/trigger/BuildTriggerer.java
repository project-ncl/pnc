package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Consumer;

@Stateless
public class BuildTriggerer {

    private ProjectBuilder projectBuilder;
    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    //to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final ProjectBuilder projectBuilder, final ProjectBuildConfigurationRepository projectBuildConfigurationRepository) {
        this.projectBuilder = projectBuilder;
        this.projectBuildConfigurationRepository = projectBuildConfigurationRepository;
    }

    public void triggerBuilds( final Integer configurationId )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final ProjectBuildConfiguration configuration = projectBuildConfigurationRepository.findOne(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildCollection buildCollection = new BuildCollection();
        buildCollection.setProductVersion(configuration.getProductVersion());

        Consumer<TaskStatus> onStatusUpdate = (newStatus) -> {

        };
        Consumer<Exception> onError = (e) -> {
            e.printStackTrace(); //TODO
        };

        projectBuilder.buildProject(configuration, buildCollection, onStatusUpdate, onError);
    }

}
