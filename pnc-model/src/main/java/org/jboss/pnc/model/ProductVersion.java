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

    private boolean released;

    private boolean supported;

    private String internalDownloadUrl;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

    @OneToMany(mappedBy = "productVersion", cascade = CascadeType.ALL)
    private Set<ProductVersionProject> productVersionProjects;

    @OneToMany(mappedBy = "productVersion", cascade = CascadeType.ALL)
    private Set<BuildRecordSet> productBuildRecordSets;

    public ProductVersion() {
        productBuildRecordSets = new HashSet<>();
        productVersionProjects = new HashSet<>();
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
     * @param version
     * @param product
     * @param released
     * @param supported
     * @param internalDownloadUrl
     */
    public ProductVersion(String version, Product product, boolean released, boolean supported, String internalDownloadUrl) {
        this();
        this.version = version;
        this.product = product;
        this.released = released;
        this.supported = supported;
        this.internalDownloadUrl = internalDownloadUrl;
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
     * Flag to show whether this product version has been released
     * 
     * @return
     */
    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    /**
     * Flag showing whether this product version is currently supported
     * 
     * @return
     */
    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    /**
     * URL which can be used to download the product distribution
     * 
     * @return
     */
    public String getInternalDownloadUrl() {
        return internalDownloadUrl;
    }

    public void setInternalDownloadUrl(String internalDownloadUrl) {
        this.internalDownloadUrl = internalDownloadUrl;
    }

    /**
     * @return the productBuildRecordSets
     */
    public Set<BuildRecordSet> getProductBuildRecordSets() {
        return productBuildRecordSets;
    }

    /**
     * @param productBuildRecordSets the productBuildRecordSets to set
     */
    public void setProductBuildRecordSets(Set<BuildRecordSet> productBuildRecordSets) {
        this.productBuildRecordSets = productBuildRecordSets;
    }

    /**
     * Add a productBuildRecordSet to the set of productBuildRecordSets
     *
     * @param productBuildRecordSet
     * @return
     */
    public Set<BuildRecordSet> addProductBuildRecordSet(BuildRecordSet productBuildRecordSet) {
        productBuildRecordSets.add(productBuildRecordSet);

        return productBuildRecordSets;
    }

    /**
     * Remove a productBuildRecordSet from the set of productBuildRecordSets
     *
     * @param productBuildRecordSet
     * @return
     */
    public Set<BuildRecordSet> removeProductBuildRecordSet(BuildRecordSet productBuildRecordSet) {
        productBuildRecordSets.remove(productBuildRecordSet);

        return productBuildRecordSets;
    }

    public Set<ProductVersionProject> getProductVersionProjects() {
        return productVersionProjects;
    }

    public void setProductVersionProjects(Set<ProductVersionProject> productVersionProjects) {
        this.productVersionProjects = productVersionProjects;
    }
}
