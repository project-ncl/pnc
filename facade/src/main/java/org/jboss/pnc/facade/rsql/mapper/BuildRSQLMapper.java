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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;

import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BuildRSQLMapper implements RSQLMapper<BuildRecord>{

    @Inject
    private EnvironmentRSQLMapper em;
    @Inject
    private UserRSQLMapper um;
    
    @Override
    public Path<?> toPath(From<?, BuildRecord> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(BuildRecord_.id);
            case "submitTime": return from.get(BuildRecord_.submitTime);
            case "startTime": return from.get(BuildRecord_.startTime);
            case "endTime": return from.get(BuildRecord_.endTime);
            case "status": return from.get(BuildRecord_.status);
            case "buildContentId": return from.get(BuildRecord_.buildContentId);
            case "temporaryBuild": return from.get(BuildRecord_.temporaryBuild);
            case "environment":
                return em.toPath(from.join(BuildRecord_.buildEnvironment), selector.next());
            case "user":
                return um.toPath(from.join(BuildRecord_.user), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

}
