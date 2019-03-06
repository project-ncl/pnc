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
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.inject.Inject;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@ApplicationScoped
public class ArtifactRSQLMapper implements RSQLMapper<Artifact> {

    @Inject
    TargetRepositoryRSQLMapper trm;

    @Override public Path<?> toPath(From<?, Artifact> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(Artifact_.id);
            case "identifier": return from.get(Artifact_.identifier);
            case "md5": return from.get(Artifact_.md5);
            case "sha1": return from.get(Artifact_.sha1);
            case "sha256": return from.get(Artifact_.sha256);
            case "filename": return from.get(Artifact_.filename);
            case "deployPath": return from.get(Artifact_.deployPath);
            case "originUrl": return from.get(Artifact_.originUrl);
            case "size": return from.get(Artifact_.size);
            case "importDate": return from.get(Artifact_.importDate);
            case "artifactQuality": return from.get(Artifact_.artifactQuality);
            case "targetRepository":
                return trm.toPath(from.join(Artifact_.targetRepository), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());


        }
    }
}
