/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.model.Operation_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public abstract class GenericOperationRSQLMapper<T extends Operation> extends AbstractRSQLMapper<Base32LongID, T> {

    public GenericOperationRSQLMapper(Class<T> type) {
        super(type);
    }

    @Override
    protected SingularAttribute<? super T, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "user":
                return Operation_.user;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<T, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super T, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Operation_.id;
            case "endTime":
                return Operation_.endTime;
            case "result":
                return Operation_.result;
            case "progressStatus":
                return Operation_.progressStatus;
            case "startTime":
                return Operation_.startTime;
            case "submitTime":
                return Operation_.submitTime;
            default:
                return null;
        }
    }

}
