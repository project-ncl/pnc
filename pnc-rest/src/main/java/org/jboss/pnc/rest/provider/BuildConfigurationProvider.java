package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigurationPredicates.*;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationProvider extends BasePaginationProvider<BuildConfigurationRest, BuildConfiguration> {

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

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRestModel() {
        return projectConfiguration -> new BuildConfigurationRest(projectConfiguration);
    }

    @Override
    public String getDefaultSortingField() {
        return BuildConfiguration.DEFAULT_SORTING_FIELD;
    }

    @Deprecated
    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting) {
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return buildConfigurationRepository.findAll().stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(buildConfigurationRepository.findAll(buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public List<BuildConfigurationRest> getAll() {
        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll());
    }

    public List<BuildConfigurationRest> getAllForProject(Integer projectId) {
        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll(withProjectId(projectId)));
    }

    public List<BuildConfigurationRest> getAllForProduct(Integer productId) {
        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll(withProductId(productId)));
    }

    public List<BuildConfigurationRest> getAllForProductAndProductVersion(Integer productId, Integer versionId) {
        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll(withProductId(productId).and(withProductVersionId(versionId))));
    }

    @Deprecated
    public BuildConfigurationRest getSpecific(Integer projectId, Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findOne(withProjectId(projectId).and(withConfigurationId(id)));
        return mapToBuildConfigurationRest(projectConfiguration);
    }

    public BuildConfigurationRest getSpecific(Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findOne(withConfigurationId(id));
        return mapToBuildConfigurationRest(projectConfiguration);
    }

    @Deprecated
    public Integer store(Integer projectId, BuildConfigurationRest buildConfigurationRest) {
        Project project = projectRepository.findOne(projectId);
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectId);
        BuildConfiguration buildConfiguration = buildConfigurationRest.toBuildConfiguration(project);
        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);
        return buildConfiguration.getId();
    }

    public Integer store(BuildConfigurationRest buildConfigurationRest) {
        BuildConfiguration buildConfiguration = buildConfigurationRest.toBuildConfiguration();
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
        buildConfiguration.setPatchesUrl(buildConfigurationRest.getPatchesUrl());
        buildConfiguration.setCreationTime(buildConfigurationRest.getCreationTime());
        buildConfiguration.setLastModificationTime(buildConfigurationRest.getLastModificationTime());
        buildConfiguration.setRepositories(buildConfigurationRest.getRepositories());

        buildConfiguration = buildConfigurationRepository.saveAndFlush(buildConfiguration);
        return buildConfiguration.getId();
    }

    @Deprecated
    public Integer clone(Integer projectId, Integer buildConfigurationId) {
        return clone(buildConfigurationId);
    }

    public Integer clone(Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = BuildConfigurationBuilder.newBuilder()
                .name("_" + buildConfiguration.getName()).buildScript(buildConfiguration.getBuildScript())
                .scmUrl(buildConfiguration.getScmUrl()).scmBranch(buildConfiguration.getScmBranch())
                .patchesUrl(buildConfiguration.getPatchesUrl()).description(buildConfiguration.getDescription())
                .productVersion(buildConfiguration.getProductVersion()).project(buildConfiguration.getProject())
                .environment(buildConfiguration.getEnvironment()).creationTime(buildConfiguration.getCreationTime())
                .lastModificationTime(buildConfiguration.getLastModificationTime())
                .repositories(buildConfiguration.getRepositories()).dependencies(buildConfiguration.getDependencies()).build();

        clonedBuildConfiguration = buildConfigurationRepository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }

    private BuildConfigurationRest mapToBuildConfigurationRest(BuildConfiguration projectConfiguration) {
        if (projectConfiguration != null) {
            return new BuildConfigurationRest(projectConfiguration);
        }
        return null;
    }

    private List<BuildConfigurationRest> mapToListOfBuildConfigurationRest(Iterable<BuildConfiguration> entries) {
        return nullableStreamOf(entries).map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration)).collect(
                Collectors.toList());
    }
}
