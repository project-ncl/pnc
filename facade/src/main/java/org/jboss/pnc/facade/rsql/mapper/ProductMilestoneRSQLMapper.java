/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductMilestoneRSQLMapper extends AbstractRSQLMapper<Integer, ProductMilestone> {

    public ProductMilestoneRSQLMapper() {
        super(ProductMilestone.class);
    }

    @Override
    protected SingularAttribute<ProductMilestone, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "productVersion":
                return ProductMilestone_.productVersion;
            case "productRelease":
                return ProductMilestone_.productRelease;
            default:
                return null;
        }
    }

    @Override
    protected SingularAttribute<ProductMilestone, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return ProductMilestone_.id;
            case "version":
                return ProductMilestone_.version;
            case "endDate":
                return ProductMilestone_.endDate;
            case "startingDate":
                return ProductMilestone_.startingDate;
            case "plannedEndDate":
                return ProductMilestone_.plannedEndDate;
            default:
                return null;
        }
    }

}
