package org.jboss.pnc.rest.provider;


import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
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

import static org.jboss.pnc.datastore.predicates.ArtifactPredicates.withBuildRecordId;
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
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(Artifact.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(artifactRepository.findAll(withBuildRecordId(buildRecordId).and(filteringCriteria.get()), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    private Function<Artifact, ArtifactRest> toRestModel() {
        return artifact -> new ArtifactRest(artifact);
    }
}
