package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationProvider {

    private BuildConfigurationRepository buildConfigurationRepository;
    private ProjectRepository projectRepository;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository,
            ProjectRepository projectRepository) {
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.projectRepository = projectRepository;
    }

    // needed for EJB/CDI
    public BuildConfigurationProvider() {
    }

    public List<BuildConfigurationRest> getAll(Integer projectId) {
        List<BuildConfiguration> product = buildConfigurationRepository.findByProjectId(projectId);
        return nullableStreamOf(product).map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration)).collect(
                Collectors.toList());
    }

    public BuildConfigurationRest getSpecific(Integer projectId, Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findByProjectIdAndConfigurationId(projectId, id);
        if (projectConfiguration != null) {
            return new BuildConfigurationRest(projectConfiguration);
        }
        return null;
    }

    public List<BuildConfigurationRest> getAll() {
        List<BuildConfiguration> product = buildConfigurationRepository.findAll();
        return nullableStreamOf(product).map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration)).collect(
                Collectors.toList());
    }

    public Integer store(Integer projectId, BuildConfigurationRest buildConfigurationRest) {
        Project project = projectRepository.findOne(projectId);
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectId);
        BuildConfiguration buildConfiguration = buildConfigurationRest.getBuildConfiguration(project);
        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);
        return buildConfiguration.getId();
    }

    public Integer update(BuildConfigurationRest buildConfigurationRest) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationRest.getId());
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationRest.getId());

        // Applying the changes
        buildConfiguration.setName(buildConfigurationRest.getName());
        buildConfiguration.setBuildScript(buildConfigurationRest.getBuildScript());
        buildConfiguration.setScmUrl(buildConfigurationRest.getScmUrl());
        buildConfiguration.setCreationTime(buildConfigurationRest.getCreationTime());
        buildConfiguration.setLastModificationTime(buildConfigurationRest.getLastModificationTime());
        buildConfiguration.setRepositories(buildConfigurationRest.getRepositories());

        buildConfiguration = buildConfigurationRepository.saveAndFlush(buildConfiguration);
        return buildConfiguration.getId();
    }

    public Integer clone(Integer projectId, Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = BuildConfigurationBuilder.newBuilder().name("_" + buildConfiguration.getName())
                .buildScript(buildConfiguration.getBuildScript()).scmUrl(buildConfiguration.getScmUrl())
                .description(buildConfiguration.getDescription()).productVersion(buildConfiguration.getProductVersion())
                .project(buildConfiguration.getProject()).environment(buildConfiguration.getEnvironment())
                .creationTime(buildConfiguration.getCreationTime())
                .lastModificationTime(buildConfiguration.getLastModificationTime())
                .repositories(buildConfiguration.getRepositories()).dependencies(buildConfiguration.getDependencies()).build();

        clonedBuildConfiguration = buildConfigurationRepository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }
}
