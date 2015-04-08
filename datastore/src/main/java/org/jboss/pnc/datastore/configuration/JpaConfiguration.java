package org.jboss.pnc.datastore.configuration;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
