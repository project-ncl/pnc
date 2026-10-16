/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.Join;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.model.Operation_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

public class OperationPredicates {

    public static Predicate<DeliverableAnalyzerOperation> withMilestoneId(Integer milestoneId) {
        return (root, query, cb) -> {
            Join<DeliverableAnalyzerOperation, ProductMilestone> milestone = root
                    .join(DeliverableAnalyzerOperation_.productMilestone);
            return cb.equal(milestone.get(ProductMilestone_.id), milestoneId);
        };
    }

    public static <T extends Operation> Predicate<T> inProgress() {
        return (root, query, cb) -> cb.isNull(root.get(Operation_.result));
    }

    public static <T extends Operation> Predicate<T> withResult(OperationResult result) {
        return (root, query, cb) -> cb.equal(root.get(Operation_.result), result);
    }

}
