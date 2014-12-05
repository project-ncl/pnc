/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model.builder;

import java.util.HashSet;
import java.util.Set;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

/**
 * @author avibelli
 *
 */
public class ProductBuilder {

    private Integer id;

    private String name;

    private String description;

    private Set<ProductVersion> productVersions;

    private ProductBuilder() {
        productVersions = new HashSet<>();
    }

    public static ProductBuilder newBuilder() {
        return new ProductBuilder();
    }

    public Product build() {

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);

        // Set the bi-directional mapping
        for (ProductVersion productVersion : productVersions) {
            productVersion.setProduct(product);
        }
        product.setProductVersions(productVersions);

        return product;
    }

    public ProductBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProductBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder productVersion(ProductVersion productVersion) {
        this.productVersions.add(productVersion);
        return this;
    }

    public ProductBuilder productVersions(Set<ProductVersion> productVersions) {
        this.productVersions = productVersions;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<ProductVersion> getProductVersions() {
        return productVersions;
    }

}
