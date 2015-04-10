package org.jboss.pnc.datastore.repositories.audited;

import org.hibernate.envers.AuditReader;
import org.jboss.pnc.datastore.audit.impl.AbstractAuditRepository;
import org.jboss.pnc.model.BuildConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AuditedBuildConfigurationRepository extends AbstractAuditRepository<BuildConfiguration, Integer> {

    /**
     * @deprecated This constructor is provided only for CDI. Please don't use it.
     */
    @Deprecated
    public AuditedBuildConfigurationRepository() {
        super(null, BuildConfiguration.class);
    }

    @Inject
    public AuditedBuildConfigurationRepository(AuditReader auditReader) {
        super(auditReader, BuildConfiguration.class);
    }
}
