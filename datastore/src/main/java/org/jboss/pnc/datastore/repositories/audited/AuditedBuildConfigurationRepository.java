/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories.audited;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.envers.AuditReader;
import org.jboss.pnc.datastore.audit.AbstractAuditRepository;
import org.jboss.pnc.model.BuildConfiguration;

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
