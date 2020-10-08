/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class ArtifactRSQLMapper extends AbstractRSQLMapper<Integer, Artifact> {

    public ArtifactRSQLMapper() {
        super(Artifact.class);
    }

    @Override
    protected SingularAttribute<Artifact, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "targetRepository":
                return Artifact_.targetRepository;
            case "build":
                return Artifact_.buildRecord;
            default:
                return null;
        }
    }

    @Override
    protected SingularAttribute<Artifact, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Artifact_.id;
            case "identifier":
                return Artifact_.identifier;
            case "md5":
                return Artifact_.md5;
            case "sha1":
                return Artifact_.sha1;
            case "sha256":
                return Artifact_.sha256;
            case "filename":
                return Artifact_.filename;
            case "deployPath":
                return Artifact_.deployPath;
            case "originUrl":
                return Artifact_.originUrl;
            case "size":
                return Artifact_.size;
            case "importDate":
                return Artifact_.importDate;
            case "artifactQuality":
                return Artifact_.artifactQuality;
            default:
                return null;
        }
    }
}
