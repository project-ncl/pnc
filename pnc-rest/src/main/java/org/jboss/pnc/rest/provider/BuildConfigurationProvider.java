package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;
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
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository, ProjectRepository projectRepository) {
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.projectRepository = projectRepository;
    }

    //needed for EJB/CDI
    public BuildConfigurationProvider() {
    }


    public List<BuildConfigurationRest> getAll(Integer projectId) {
        List<BuildConfiguration> product = buildConfigurationRepository.findByProjectId(projectId);
        return nullableStreamOf(product)
                .map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration))
                .collect(Collectors.toList());
    }

    public BuildConfigurationRest getSpecific(Integer projectId, Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findByProjectIdAndConfigurationId(projectId, id);
        if(projectConfiguration != null) {
            return new BuildConfigurationRest(projectConfiguration);
        }
        return null;
    }

    public List<BuildConfigurationRest> getAll() {
        List<BuildConfiguration> product = buildConfigurationRepository.findAll();
        return nullableStreamOf(product)
                .map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration))
                .collect(Collectors.toList());
    }

    public Integer store(Integer projectId, BuildConfigurationRest buildConfigurationRest) {
        Project project = projectRepository.findOne(projectId);
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectId);
        BuildConfiguration buildConfiguration = buildConfigurationRest.getBuildConfiguration(project);
        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);
        return buildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }
}
