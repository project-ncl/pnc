package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildTriggerer {

    private ProjectBuilder projectBuilder;
    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    //to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(ProjectBuilder projectBuilder, ProjectBuildConfigurationRepository projectBuildConfigurationRepository) {
        this.projectBuilder = projectBuilder;
        this.projectBuildConfigurationRepository = projectBuildConfigurationRepository;
    }

    public void triggerBuilds(Integer configurationId) throws InterruptedException, CoreException, BuildDriverException {
        ProjectBuildConfiguration configuration = projectBuildConfigurationRepository.findOne(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        BuildCollection buildCollection = new BuildCollection();
        buildCollection.setProductVersion(configuration.getProductVersion());

        projectBuilder.buildProject(configuration, buildCollection);
    }

}
