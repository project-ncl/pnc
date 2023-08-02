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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@Slf4j
public class ArtifactRepositoryImpl extends AbstractRepository<Artifact, Integer> implements ArtifactRepository {

    /**
     * [NCLSUP-912] Partition the search for existing artifacts by this size to avoid a StackOverflow error in Hibernate
     */
    public static final int QUERY_ARTIFACT_PARITION_SIZE = 1000;

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

    @Override
    public Set<Artifact> withIdentifierAndSha256(Collection<Artifact.IdentifierSha256> identifierSha256Set) {
        // [NCLSUP-912] partition the constraints in maximum size to avoid a stackoverflow in Hibernate
        // We use the Guava Lists.partition, which requires a List. Hence we have to convert it also
        List<List<Artifact.IdentifierSha256>> partitionedList = Lists
                .partition(new ArrayList<>(identifierSha256Set), QUERY_ARTIFACT_PARITION_SIZE);
        HashSet<Artifact> artifacts = new HashSet<>();
        for (List<Artifact.IdentifierSha256> partition : partitionedList) {
            List<Artifact> artifactsInDb = queryWithPredicates(ArtifactPredicates.withIdentifierAndSha256(partition));
            artifacts.addAll(artifactsInDb);
        }
        return artifacts;
    }

}
