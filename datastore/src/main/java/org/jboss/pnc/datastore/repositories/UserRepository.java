package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author avibelli
 *
 */
public interface UserRepository extends JpaRepository<User, Integer> {

}
