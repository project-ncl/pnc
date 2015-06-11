package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserSpringRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

}

