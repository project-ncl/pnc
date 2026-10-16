/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;

@Stateless
public class ProductReleaseRepositoryImpl extends AbstractRepository<ProductRelease, Integer>
        implements ProductReleaseRepository {

    public ProductReleaseRepositoryImpl() {
        super(ProductRelease.class, Integer.class);
    }
}
