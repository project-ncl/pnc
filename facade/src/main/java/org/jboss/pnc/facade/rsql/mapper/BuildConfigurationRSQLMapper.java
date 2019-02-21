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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfiguration_;

import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BuildConfigurationRSQLMapper implements RSQLMapper<BuildConfiguration>{

    @Inject
    private ProjectRSQLMapper pjm;

    @Inject
    private RepositoryConfigurationRSQLMapper rcm;

    @Inject
    private EnvironmentRSQLMapper em;

    @Inject
    private ProductVersionRSQLMapper pvm;
    
    @Override
    public Path<?> toPath(From<?, BuildConfiguration> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(BuildConfiguration_.id);
            case "name": return from.get(BuildConfiguration_.name);
            case "description": return from.get(BuildConfiguration_.description);
            case "buildScript": return from.get(BuildConfiguration_.buildScript);
            case "scmRevision": return from.get(BuildConfiguration_.scmRevision);
            case "creationTime": return from.get(BuildConfiguration_.creationTime);
            case "modificationTime": return from.get(BuildConfiguration_.lastModificationTime);
            case "archived": return from.get(BuildConfiguration_.active); // TODO: wierd entity behaviour for archived BCs
            case "buildType": return from.get(BuildConfiguration_.buildType);
            case "project":
                return pjm.toPath(from.join(BuildConfiguration_.project), selector.next());
            case "repositoryConfiguration":
                return rcm.toPath(from.join(BuildConfiguration_.repositoryConfiguration), selector.next());
            case "environment":
                return em.toPath(from.join(BuildConfiguration_.buildEnvironment), selector.next());
            case "productVersion":
                return pvm.toPath(from.join(BuildConfiguration_.productVersion), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

}
