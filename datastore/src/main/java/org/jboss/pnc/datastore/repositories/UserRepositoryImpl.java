package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.UserSpringRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class UserRepositoryImpl extends AbstractRepository<User, Integer> implements UserRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public UserRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public UserRepositoryImpl(UserSpringRepository springUserSpringRepository) {
        super(springUserSpringRepository, springUserSpringRepository);
    }
}
