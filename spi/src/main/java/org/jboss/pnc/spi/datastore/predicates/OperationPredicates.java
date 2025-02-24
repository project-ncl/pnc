/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.Join;

import org.jboss.pnc.api.enums.ResultStatus;
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

    public static <T extends Operation> Predicate<T> withResult(ResultStatus result) {
        return (root, query, cb) -> cb.equal(root.get(Operation_.result), result);
    }

}
