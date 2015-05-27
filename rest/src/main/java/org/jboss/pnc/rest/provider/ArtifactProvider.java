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


import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.ArtifactPredicates.*;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ArtifactProvider {

    private ArtifactRepository artifactRepository;

    public ArtifactProvider() {
    }

    @Inject
    public ArtifactProvider(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    public List<ArtifactRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildRecordId) {
        return performQuery(pageIndex, pageSize, sortingRsql, query, withBuildRecordId(buildRecordId));
    }

    private List<ArtifactRest> performQuery(int pageIndex, int pageSize, String sortingRsql, String query, BooleanExpression predicates) {
        BooleanExpression filteringCriteria = RSQLPredicateProducer.fromRSQL(Artifact.class, query).get();
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(artifactRepository.findAll(
                BooleanExpression.allOf(filteringCriteria, predicates), paging))
                .map(toRestModel()).collect(Collectors.toList());
    }

    private Function<Artifact, ArtifactRest> toRestModel() {
        return artifact -> new ArtifactRest(artifact);
    }
}
