package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import java.util.List;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfigurationAudited} entity.
 */
public interface BuildConfigurationAuditedRepository extends Repository<BuildConfigurationAudited, IdRev> {
    List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id);
}
