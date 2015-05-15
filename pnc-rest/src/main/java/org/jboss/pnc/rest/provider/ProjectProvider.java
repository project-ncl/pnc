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
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ProjectProvider {

    private ProjectRepository projectRepository;

    @Inject
    public ProjectProvider(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // needed for EJB/CDI
    public ProjectProvider() {
    }

    public String getDefaultSortingField() {
        return Project.DEFAULT_SORTING_FIELD;
    }

    public List<ProjectRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(Project.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);
        return nullableStreamOf(projectRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProjectRest getSpecific(Integer id) {
        Project project = projectRepository.findOne(id);
        if (project != null) {
            return new ProjectRest(project);
        }
        return null;
    }

    public Integer store(ProjectRest projectRest) {
        Preconditions.checkArgument(projectRest.getId() == null, "Id must be null");
        Project project = projectRest.toProject();
        project = projectRepository.save(project);
        return project.getId();
    }

    public Integer update(Integer id, ProjectRest projectRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(projectRest.getId() == null || projectRest.getId().equals(id),
                "Entity id does not match the id to update");
        projectRest.setId(id);
        Project project = projectRepository.findOne(projectRest.getId());
        Preconditions.checkArgument(project != null, "Couldn't find project with id " + projectRest.getId());
        project = projectRepository.save(projectRest.toProject());
        return project.getId();
    }

    public void delete(Integer projectId) {
        projectRepository.delete(projectId);
    }

    public Function<? super Project, ? extends ProjectRest> toRestModel() {
        return project -> new ProjectRest(project);
    }

}
