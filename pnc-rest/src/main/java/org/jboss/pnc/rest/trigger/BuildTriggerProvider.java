package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.rest.mapping.Mapper;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BuildTriggerProvider {

    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    //to make CDI happy
    public BuildTriggerProvider() {
    }

    @Inject
    public BuildTriggerProvider(ProjectBuildConfigurationRepository repository) {
        this.projectBuildConfigurationRepository = repository;
    }

    public ProjectBuildConfigurationRest getSpecificConfiguration(Integer configurationId) {
        Preconditions.checkArgument(configurationId != null, "Configuration can't be null");
        return Mapper.mapToProjectBuildConfigurationRest(projectBuildConfigurationRepository.findOne(configurationId));
    }

    public List<ProjectBuildConfigurationRest> getAvailableBuildConfigurations() {
        return projectBuildConfigurationRepository.findAll().stream()
                .map(Mapper::mapToProjectBuildConfigurationRest)
                .collect(Collectors.toList());
    }

}
