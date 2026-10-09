/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductVersionRSQLMapper extends AbstractRSQLMapper<Integer, ProductVersion> {

    public ProductVersionRSQLMapper() {
        super(ProductVersion.class);
    }

    @Override
    protected SingularAttribute<ProductVersion, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "product":
                return ProductVersion_.product;
            case "currentProductMilestone":
                return ProductVersion_.currentProductMilestone;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<ProductVersion, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<ProductVersion, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return ProductVersion_.id;
            case "version":
                return ProductVersion_.version;
            default:
                return null;
        }
    }

}
