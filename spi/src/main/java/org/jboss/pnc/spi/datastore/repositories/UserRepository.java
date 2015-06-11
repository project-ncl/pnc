package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.User} entity.
 */
public interface UserRepository extends Repository<User, Integer> {
}
