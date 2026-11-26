/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ProductProvider;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.rest.api.endpoints.ProductEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
public class ProductEndpointImpl implements ProductEndpoint {

    @Inject
    private ProductProvider productProvider;

    @Inject
    private ProductVersionProvider productVersionProvider;

    private EndpointHelper<Integer, Product, ProductRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Product.class, productProvider);
    }

    @Override
    public Page<Product> getAll(PageParameters pageParameters) {

        return productProvider.getAll(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ());
    }

    @Override
    public Product createNew(Product product) {
        return endpointHelper.create(product);
    }

    @Override
    public Product getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, Product product) {
        endpointHelper.update(id, product);
    }

    @Override
    public Product patchSpecific(String id, Product product) {
        return endpointHelper.update(id, product);
    }

    @Override
    public Page<ProductVersion> getProductVersions(String id, PageParameters pageParameters) {
        return productVersionProvider.getAllForProduct(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
