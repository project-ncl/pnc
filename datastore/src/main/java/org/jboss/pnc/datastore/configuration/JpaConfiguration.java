/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

@ApplicationScoped
public class JpaConfiguration {

    @Produces
    @PersistenceContext(unitName = "primary")
    private EntityManager entityManager;

    @Produces
    public AuditReader auditReader(EntityManager entityManager) {
        return AuditReaderFactory.get(entityManager);
    }

}
