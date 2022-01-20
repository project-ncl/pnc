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

import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductMilestoneRelease_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProductMilestoneReleasePredicates {

    public static Predicate<ProductMilestoneRelease> withMilestoneId(Integer milestoneId) {
        return (root, query, cb) -> cb.equal(root.get(ProductMilestoneRelease_.milestone), milestoneId);
    }

    public static Predicate<ProductMilestoneRelease> withStatus(MilestoneCloseStatus status) {
        return (root, query, cb) -> cb.equal(root.get(ProductMilestoneRelease_.status), status);
    }

}
