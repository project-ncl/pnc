package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

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

    public List<ProjectRest> getAll(String field, String sorting, String rsql) {
        Iterable<Project> project = projectRepository.findAll();
        return nullableStreamOf(project).map(productVersion -> new ProjectRest(productVersion)).collect(Collectors.toList());
    }

    public ProjectRest getSpecific(Integer id) {
        Project project = projectRepository.findOne(id);
        if (project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

    public Integer store(ProjectRest projectRest) {
        Project project = projectRest.toProject();
        project = projectRepository.save(project);
        return project.getId();
    }

    public Integer update(ProjectRest projectRest) {
        Project project = projectRepository.findOne(projectRest.getId());
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectRest.getId());
        project = projectRepository.save(projectRest.toProject());
        return project.getId();
    }

    private List<ProjectRest> mapToListOfProjectRest(Iterable<Project> entries) {
        return nullableStreamOf(entries).map(project -> new ProjectRest(project)).collect(Collectors.toList());
    }

}
