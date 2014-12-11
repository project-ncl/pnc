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

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

/**
 * @author avibelli
 *
 */
public class ProductVersionBuilder {

    private Integer id;

    private String version;

    private Product product;

    private Set<BuildCollection> productBuildCollections;

    private ProductVersionBuilder() {
        productBuildCollections = new HashSet<>();
    }

    public static ProductVersionBuilder newBuilder() {
        return new ProductVersionBuilder();
    }

    public ProductVersion build() {
        ProductVersion productVersion = new ProductVersion();
        productVersion.setId(id);
        productVersion.setVersion(version);

        // Set the bi-directional mapping
        if (product != null) {
            product.addVersion(productVersion);
        }
        productVersion.setProduct(product);

        // Set the bi-directional mapping
        for (BuildCollection buildCollection : productBuildCollections) {
            buildCollection.setProductVersion(productVersion);
        }
        productVersion.setProductBuildCollection(productBuildCollections);

        return productVersion;
    }

    public ProductVersionBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProductVersionBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ProductVersionBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public ProductVersionBuilder productBuildCollection(BuildCollection productBuildCollection) {
        this.productBuildCollections.add(productBuildCollection);
        return this;
    }

    public ProductVersionBuilder productBuildCollections(Set<BuildCollection> productBuildCollections) {
        this.productBuildCollections = productBuildCollections;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public Product getProduct() {
        return product;
    }

    public Set<BuildCollection> getProductBuildCollections() {
        return productBuildCollections;
    }

}
