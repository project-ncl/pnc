package org.jboss.pnc.rest.provider;


import org.jboss.pnc.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.rest.restmodel.ArtifactRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class BuildArtifactProvider {

    private ArtifactRepository artifactRepository;

    public BuildArtifactProvider() {
    }

    @Inject
    public BuildArtifactProvider(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    public List<ArtifactRest> getAll(Integer buildRecordId) {
        return nullableStreamOf(artifactRepository.findByBuildRecord(buildRecordId))
                .map(artifact -> new ArtifactRest(artifact))
                .collect(Collectors.toList());
    }
}
