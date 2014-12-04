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

package org.jboss.pnc.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains all the versions for a Product
 * 
 * @author avibelli
 *
 */
@Entity
public class ProductVersion implements Serializable {

    private static final long serialVersionUID = 6314079319551264379L;

    @Id
    @GeneratedValue
    private Integer id;

    private String version;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    @OneToMany(mappedBy = "productVersion", cascade = CascadeType.ALL)
    private Set<BuildCollection> productBuildCollections;

    public ProductVersion() {
        productBuildCollections = new HashSet<>();
    }

    /**
     * @param version
     * @param product
     */
    public ProductVersion(String version, Product product) {
        this();
        this.version = version;
        this.product = product;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * @return the productBuildCollections
     */
    public Set<BuildCollection> getProductBuildCollections() {
        return productBuildCollections;
    }

    /**
     * @param productBuildCollections the productBuildCollections to set
     */
    public void setProductBuildCollections(Set<BuildCollection> productBuildCollections) {
        this.productBuildCollections = productBuildCollections;
    }

}
