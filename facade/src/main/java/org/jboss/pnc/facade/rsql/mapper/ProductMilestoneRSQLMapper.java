/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;

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
    protected SetAttribute<ProductMilestone, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
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
