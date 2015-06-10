package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationAuditedSpringRepository;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class BuildConfigurationAuditedRepositoryImpl extends AbstractRepository<BuildConfigurationAudited, IdRev> implements
        BuildConfigurationAuditedRepository {

    private BuildConfigurationAuditedSpringRepository repository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigurationAuditedRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildConfigurationAuditedRepositoryImpl(BuildConfigurationAuditedSpringRepository buildConfigurationAuditedSpringRepository) {
        super(buildConfigurationAuditedSpringRepository, buildConfigurationAuditedSpringRepository);
        repository = buildConfigurationAuditedSpringRepository;
    }

    @Override
    public List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id) {
        return repository.findAllByIdOrderByRevDesc(id);
    }
}
