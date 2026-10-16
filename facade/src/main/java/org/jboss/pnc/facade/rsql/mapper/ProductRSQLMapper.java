/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.Product_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductRSQLMapper extends AbstractRSQLMapper<Integer, Product> {

    public ProductRSQLMapper() {
        super(Product.class);
    }

    @Override
    protected SingularAttribute<Product, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<Product, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<Product, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Product_.id;
            case "name":
                return Product_.name;
            case "description":
                return Product_.description;
            case "abbreviation":
                return Product_.abbreviation;
            default:
                return null;
        }
    }

}
