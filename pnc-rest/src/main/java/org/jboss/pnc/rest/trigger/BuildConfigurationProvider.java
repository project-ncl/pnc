package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.rest.mapping.Mapper;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BuildConfigurationProvider {

    private ProjectRepository projectRepository;
    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;
    private Mapper mapper;

    //to make CDI happy
    public BuildConfigurationProvider() {
    }

    @Inject
    public BuildConfigurationProvider(ProjectBuildConfigurationRepository repository, ProjectRepository projectRepository, Mapper mapper) {
        this.projectBuildConfigurationRepository = repository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    public ProjectBuildConfigurationRest getSpecificConfiguration(Integer configurationId) {
        Preconditions.checkArgument(configurationId != null, "Configuration can't be null");
        return mapper.mapTo(projectBuildConfigurationRepository.findOne(configurationId), ProjectBuildConfigurationRest.class);
    }

    public List<ProjectBuildConfigurationRest> getAvailableBuildConfigurations() {
        return projectBuildConfigurationRepository.findAll().stream()
                .map(projectBuildConfiguration -> mapper.mapTo(projectBuildConfiguration, ProjectBuildConfigurationRest.class))
                .collect(Collectors.toList());
    }

    public Integer storeConfiguration(ProjectBuildConfigurationRest configuration) {
        Preconditions.checkArgument(configuration.getProjectId() != null, "Parent Project Id must not be null");
        ProjectBuildConfiguration mappedConfiguration = mapper.mapTo(configuration, ProjectBuildConfiguration.class);

        Project project = projectRepository.findOne(configuration.getProjectId());
        mappedConfiguration.setProject(project);

        projectBuildConfigurationRepository.save(mappedConfiguration);

        return mappedConfiguration.getId();
    }

    public void updateConfiguration(ProjectBuildConfigurationRest configuration) {
        Preconditions.checkArgument(configuration.getId() != null, "Configuration Id must not be null");
        ProjectBuildConfiguration mappedConfiguration = mapper.mapTo(configuration, ProjectBuildConfiguration.class);

        ProjectBuildConfiguration configurationToBeUpdated = projectBuildConfigurationRepository.findOne(configuration.getId());
        Preconditions.checkArgument(configurationToBeUpdated != null, "Can't find project configuration");

        mapper.mapTo(mappedConfiguration, configurationToBeUpdated);

        projectBuildConfigurationRepository.save(mappedConfiguration);
    }

}
