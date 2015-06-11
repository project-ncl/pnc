package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.EnvironmentSpringRepository;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.datastore.repositories.EnvironmentRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class EnvironmentImpl extends AbstractRepository<Environment, Integer> implements EnvironmentRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public EnvironmentImpl() {
        super(null, null);
    }

    @Inject
    public EnvironmentImpl(EnvironmentSpringRepository environmentSpringRepository) {
        super(environmentSpringRepository, environmentSpringRepository);
    }
}
