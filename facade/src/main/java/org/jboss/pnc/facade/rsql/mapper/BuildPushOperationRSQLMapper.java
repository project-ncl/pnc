/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushOperation_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildPushOperationRSQLMapper extends GenericOperationRSQLMapper<BuildPushOperation> {

    public BuildPushOperationRSQLMapper() {
        super(BuildPushOperation.class);
    }

    @Override
    protected SingularAttribute<? super BuildPushOperation, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "build":
                return BuildPushOperation_.build;
            default:
                return super.toEntity(name);
        }
    }

    @Override
    protected SingularAttribute<? super BuildPushOperation, ?> toAttribute(String name) {
        switch (name) {
            default:
                return super.toAttribute(name);
        }
    }

}
