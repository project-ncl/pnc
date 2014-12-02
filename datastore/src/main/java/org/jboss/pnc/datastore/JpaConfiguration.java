package org.jboss.pnc.datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class JpaConfiguration {

    @Produces
    @PersistenceContext(unitName = "primary")
    private EntityManager entityManager;

}
