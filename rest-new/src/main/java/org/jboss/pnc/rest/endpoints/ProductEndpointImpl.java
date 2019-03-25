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
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ProductProvider;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.rest.api.endpoints.ProductEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductEndpointImpl extends AbstractEndpoint<Product, ProductRef> implements ProductEndpoint {

    @Inject
    private ProductProvider productProvider;

    @Inject
    private ProductVersionProvider productVersionProvider;

    public ProductEndpointImpl() {
        super(Product.class);
    }

    @Override
    protected ProductProvider provider() {
        return productProvider;
    }

    @Override
    public Page<Product> getAll(PageParameters pageParameters) {

        return productProvider.getAll(pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ());

    }

    @Override
    public Product createNew(Product product) {
        return super.create(product);
    }

    @Override
    public Product getSpecific(int id) {
        return super.getSpecific(id);
    }

    @Override
    public void update(int id, Product product) {
        super.update(id, product);
    }

    @Override
    public Page<ProductVersion> getProductVersions(int id, PageParameters pageParameters) {
        return productVersionProvider.getAllForProduct(pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
