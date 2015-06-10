package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ArtifactSpringRepository;
import org.jboss.pnc.datastore.repositories.internal.UserSpringRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ArtifactRepositoryImpl extends AbstractRepository<Artifact, Integer> implements ArtifactRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ArtifactRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ArtifactRepositoryImpl(ArtifactSpringRepository springArtifactRepository) {
        super(springArtifactRepository, springArtifactRepository);
    }
}
