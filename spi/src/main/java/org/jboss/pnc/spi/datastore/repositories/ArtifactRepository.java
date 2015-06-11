package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Artifact} entity.
 */
public interface ArtifactRepository extends Repository<Artifact, Integer> {
}
