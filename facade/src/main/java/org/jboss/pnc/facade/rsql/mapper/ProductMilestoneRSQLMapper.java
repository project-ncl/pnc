/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductMilestoneRSQLMapper implements RSQLMapper<ProductMilestone>{
    @Inject
    private ProductVersionRSQLMapper pvm;

    @Inject
    private ProductReleaseRSQLMapper prm;

    @Override
    public Path<?> toPath(From<?, ProductMilestone> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": return from.get(ProductMilestone_.id);
            case "version": return from.get(ProductMilestone_.version);
            case "endDate": return from.get(ProductMilestone_.endDate);
            case "startingDate": return from.get(ProductMilestone_.startingDate);
            case "plannedEndDate": return from.get(ProductMilestone_.plannedEndDate);
            case "downloadUrl": return from.get(ProductMilestone_.downloadUrl);
            case "issueTrackerUrl": return from.get(ProductMilestone_.issueTrackerUrl);
            case "productVersion":
                return pvm.toPath(from.join(ProductMilestone_.productVersion), selector.next());
            case "productRelease":
                return prm.toPath(from.join(ProductMilestone_.productRelease), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

}
