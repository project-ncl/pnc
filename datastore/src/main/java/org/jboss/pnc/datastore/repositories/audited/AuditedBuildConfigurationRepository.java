package org.jboss.pnc.datastore.repositories.audited;

import org.hibernate.envers.AuditReader;
import org.jboss.pnc.datastore.audit.impl.AbstractAuditRepository;
import org.jboss.pnc.model.BuildConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AuditedBuildConfigurationRepository extends AbstractAuditRepository<BuildConfiguration, Integer> {

    @Deprecated
    public AuditedBuildConfigurationRepository() {
        super(null, BuildConfiguration.class);
        //for CDI
    }

    @Inject
    public AuditedBuildConfigurationRepository(AuditReader auditReader) {
        super(auditReader, BuildConfiguration.class);
    }
}
