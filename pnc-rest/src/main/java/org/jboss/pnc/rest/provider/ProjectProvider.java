package org.jboss.pnc.rest.provider;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class ProjectProvider extends BasePaginationProvider<ProjectRest, Project> {

    private ProjectRepository projectRepository;

    @Inject
    public ProjectProvider(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // needed for EJB/CDI
    public ProjectProvider() {
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super Project, ? extends ProjectRest> toRestModel() {
        return project -> new ProjectRest(project);
    }

    @Override
    public String getDefaultSortingField() {
        return Project.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting) {

        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return projectRepository.findAll().stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(projectRepository.findAll(buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }
    
    public List<ProjectRest> getAll(Integer productId, Integer productVersionId) {
        List<Project> project = projectRepository.findByProductAndProductVersionId(productId, productVersionId);
        return nullableStreamOf(project).map(productVersion -> new ProjectRest(productVersion)).collect(Collectors.toList());
    }

    public ProjectRest getSpecific(Integer productId, Integer productVersionId, Integer projectId) {
        Project project = projectRepository
                .findByProductAndProductVersionIdAndProjectId(productId, productVersionId, projectId);
        if (project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

    public ProjectRest getSpecific(Integer id) {
        Project project = projectRepository.findOne(id);
        if (project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

}
