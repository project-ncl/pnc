package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class ProjectProvider {

    private ProjectRepository projectRepository;

    @Inject
    public ProjectProvider(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    //needed for EJB/CDI
    public ProjectProvider() {
    }

    public List<ProjectRest> getAll(Integer productId, Integer productVersionId) {
        List<Project> product = projectRepository.findByProductAndProductVersionId(productId, productVersionId);
        return nullableStreamOf(product)
                .map(productVersion -> new ProjectRest(productVersion))
                .collect(Collectors.toList());
    }

    public ProjectRest getSpecific(Integer productId, Integer productVersionId, Integer projectId) {
        Project project = projectRepository.findByProductAndProductVersionIdAndProjectId(productId, productVersionId, projectId);
        if(project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

}
