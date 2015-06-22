package org.jboss.pnc.datastore.repositories;

import java.util.List;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationAuditedSpringRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildRecordSpringRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Integer> implements BuildRecordRepository {

    private BuildRecordSpringRepository repository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildRecordRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildRecordRepositoryImpl(BuildRecordSpringRepository buildRecordSpringRepository) {
        super(buildRecordSpringRepository, buildRecordSpringRepository);
        repository = buildRecordSpringRepository;
    }

    @Override
    public List<BuildRecord> findAllByLatestBuildConfigurationOrderByEndTimeDesc(BuildConfiguration buildConfiguration) {
        return repository.findAllByLatestBuildConfigurationOrderByEndTimeDesc(buildConfiguration);
    }
}
