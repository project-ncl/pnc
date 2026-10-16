/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;

@Stateless
public class ProductRepositoryImpl extends AbstractRepository<Product, Integer> implements ProductRepository {

    public ProductRepositoryImpl() {
        super(Product.class, Integer.class);
    }
}
