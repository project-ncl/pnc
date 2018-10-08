/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProjectPredicates.withProjectName;

@Stateless
public class ProjectProvider extends AbstractProvider<Project, ProjectRest> {

    @Inject
    public ProjectProvider(ProjectRepository projectRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(projectRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    // needed for EJB/CDI
    public ProjectProvider() {
    }

    @Override
    protected void validateBeforeSaving(ProjectRest projectRest) throws RestValidationException {
        Project project = repository.queryByPredicates(withProjectName(projectRest.getName()));
        //don't validate against myself
        if(project != null && !project.getId().equals(projectRest.getId())) {
            throw new ConflictedEntryException("Project of that name already exists", Project.class, project.getId());
        }
    }

    @Override
    protected Function<? super Project, ? extends ProjectRest> toRESTModel() {
        return project -> new ProjectRest(project);
    }

    @Override
    protected Function<? super ProjectRest, ? extends Project> toDBModel() {
        return project -> project.toDBEntityBuilder().build();
    }
}
