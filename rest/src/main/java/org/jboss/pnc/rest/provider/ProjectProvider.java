/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.ProjectPredicates.withProjectName;

@Stateless
public class ProjectProvider {

    private ProjectRepository projectRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public ProjectProvider(ProjectRepository projectRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.projectRepository = projectRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    // needed for EJB/CDI
    public ProjectProvider() {
    }

    public String getDefaultSortingField() {
        return Project.DEFAULT_SORTING_FIELD;
    }

    public List<ProjectRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<Project> rsqlPredicate = rsqlPredicateProducer.getPredicate(Project.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(projectRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProjectRest getSpecific(Integer id) {
        Project project = projectRepository.queryById(id);
        if (project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

    public Integer store(ProjectRest projectRest) throws ConflictedEntryException {
        Preconditions.checkArgument(projectRest.getId() == null, "Id must be null");
        validateBeforeSaving(projectRest);
        Project project = projectRest.toProject();
        project = projectRepository.save(project);
        return project.getId();
    }

    public Integer update(Integer id, ProjectRest projectRest) throws ConflictedEntryException {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(projectRest.getId() == null || projectRest.getId().equals(id),
                "Entity id does not match the id to update");
        validateBeforeSaving(projectRest);
        projectRest.setId(id);
        Project project = projectRepository.queryById(id);
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectRest.getId());
        project = projectRepository.save(projectRest.toProject());
        return project.getId();
    }

    private void validateBeforeSaving(ProjectRest projectRest) throws ConflictedEntryException {
        Project project = projectRepository.queryByPredicates(withProjectName(projectRest.getName()));
        if(project != null) {
            throw new ConflictedEntryException("Project of that name already exists", Project.class, project.getId());
        }
    }

    public void delete(Integer projectId) {
        projectRepository.delete(projectId);
    }

    public Function<? super Project, ? extends ProjectRest> toRestModel() {
        return project -> new ProjectRest(project);
    }

}
