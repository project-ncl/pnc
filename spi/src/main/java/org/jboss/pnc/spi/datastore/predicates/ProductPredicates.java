/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.Product} entity.
 */
public class ProductPredicates {

    public static Predicate<Product> withName(String name) {
        return (root, query, cb) -> cb.equal(root.get(Product_.name), name);
    }

    public static Predicate<Product> withAbbrev(String abbrev) {
        return (root, query, cb) -> cb.equal(root.get(Product_.abbreviation), abbrev);
    }

}
