/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class DeliverableAnalyzerOperationRSQLMapper extends GenericOperationRSQLMapper<DeliverableAnalyzerOperation> {

    public DeliverableAnalyzerOperationRSQLMapper() {
        super(DeliverableAnalyzerOperation.class);
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerOperation, ? extends GenericEntity<?>> toEntity(
            String name) {
        switch (name) {
            case "productMilestone":
                return DeliverableAnalyzerOperation_.productMilestone;
            default:
                return super.toEntity(name);
        }
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerOperation, ?> toAttribute(String name) {
        switch (name) {
            default:
                return super.toAttribute(name);
        }
    }

}
