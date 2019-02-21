/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProjectRSQLMapper implements RSQLMapper<Project> {

    @Inject
    private BuildConfigurationRSQLMapper bcm;

    @Override
    public Path<?> toPath(From<?, Project> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(Project_.id);
            case "name": return from.get(Project_.name);
            case "description": return from.get(Project_.description);
            case "issueTrackerUrl": return from.get(Project_.issueTrackerUrl);
            case "projectUrl": return from.get(Project_.projectUrl);
            default: throw new IllegalArgumentException();
        }
    }

}
