package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.BuildConfiguration;
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
        return projectConfiguration -> {
            if (projectConfiguration != null) {
                return new BuildConfigurationRest(projectConfiguration);
            }
            return null;
        };
    }

    @Override
    public String getDefaultSortingField() {
        return BuildConfiguration.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String rsqls) {
        RSQLPredicate rsqlPredicate = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, rsqls);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return nullableStreamOf(buildConfigurationRepository.findAll(rsqlPredicate.get())).map(toRestModel()).collect(
                    Collectors.toList());
        } else {
            return transform(buildConfigurationRepository.findAll(rsqlPredicate.get(),
                    buildPageRequest(pageIndex, pageSize, field, sorting)));
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
        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll(withProductId(productId).and(
                withProductVersionId(versionId))));
    }

    public BuildConfigurationRest getSpecific(Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findOne(withConfigurationId(id));
        return toRestModel().apply(projectConfiguration);
    }

    public Integer store(BuildConfigurationRest buildConfigurationRest) {
        BuildConfiguration buildConfiguration = buildConfigurationRest.toBuildConfiguration(null);
        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);
        return buildConfiguration.getId();
    }

    public Integer update(BuildConfigurationRest buildConfigurationRest) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationRest.getId());
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationRest.getId());
        buildConfiguration = buildConfigurationRepository.saveAndFlush(buildConfigurationRest.toBuildConfiguration(buildConfiguration));
        return buildConfiguration.getId();
    }

    public Integer clone(Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();

        clonedBuildConfiguration = buildConfigurationRepository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }

    private List<BuildConfigurationRest> mapToListOfBuildConfigurationRest(Iterable<BuildConfiguration> entries) {
        return nullableStreamOf(entries).map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration)).collect(
                Collectors.toList());
    }
}
