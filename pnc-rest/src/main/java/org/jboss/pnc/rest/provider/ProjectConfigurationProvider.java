package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.rest.restmodel.ProjectBuildConfigurationRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class ProjectConfigurationProvider {

    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;
    private ProjectRepository projectRepository;

    @Inject
    public ProjectConfigurationProvider(ProjectBuildConfigurationRepository projectBuildConfigurationRepository, ProjectRepository projectRepository) {
        this.projectBuildConfigurationRepository = projectBuildConfigurationRepository;
        this.projectRepository = projectRepository;
    }

    //needed for EJB/CDI
    public ProjectConfigurationProvider() {
    }


    public List<ProjectBuildConfigurationRest> getAll(Integer projectId) {
        List<ProjectBuildConfiguration> product = projectBuildConfigurationRepository.findByProjectId(projectId);
        return nullableStreamOf(product)
                .map(projectConfiguration -> new ProjectBuildConfigurationRest(projectConfiguration))
                .collect(Collectors.toList());
    }

    public ProjectBuildConfigurationRest getSpecific(Integer projectId, Integer id) {
        ProjectBuildConfiguration projectConfiguration = projectBuildConfigurationRepository.findByProjectIdAndConfigurationId(projectId, id);
        if(projectConfiguration != null) {
            return new ProjectBuildConfigurationRest(projectConfiguration);
        }
        return null;
    }

    public List<ProjectBuildConfigurationRest> getAll() {
        List<ProjectBuildConfiguration> product = projectBuildConfigurationRepository.findAll();
        return nullableStreamOf(product)
                .map(projectConfiguration -> new ProjectBuildConfigurationRest(projectConfiguration))
                .collect(Collectors.toList());
    }

    public Integer store(Integer projectId, ProjectBuildConfigurationRest projectBuildConfigurationRest) {
        Project project = projectRepository.findOne(projectId);
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectId);
        ProjectBuildConfiguration projectBuildConfiguration = projectBuildConfigurationRest.getProjectBuildConfiguration(project);
        projectBuildConfiguration = projectBuildConfigurationRepository.save(projectBuildConfiguration);
        return projectBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        projectBuildConfigurationRepository.delete(configurationId);
    }
}
