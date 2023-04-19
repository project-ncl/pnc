/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class ArtifactRepositoryImpl extends AbstractRepository<Artifact, Integer> implements ArtifactRepository {

    public ArtifactRepositoryImpl() {
        super(Artifact.class, Integer.class);
    }

    @Override
    public Artifact withPurl(String purl) {

        List<Artifact> artifacts = queryWithPredicates(ArtifactPredicates.withPurl(purl));
        if (artifacts != null && !artifacts.isEmpty()) {
            return artifacts.get(0);
        }

        return null;
    }

}
