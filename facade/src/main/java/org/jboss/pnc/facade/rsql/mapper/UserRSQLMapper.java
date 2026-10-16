/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.User_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class UserRSQLMapper extends AbstractRSQLMapper<Integer, User> {

    public UserRSQLMapper() {
        super(User.class);
    }

    @Override
    protected SingularAttribute<User, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<User, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<User, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return User_.id;
            case "username":
                return User_.username;
            default:
                return null;
        }
    }

}
