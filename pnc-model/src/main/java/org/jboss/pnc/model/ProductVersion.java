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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains all the versions for a Product
 *
 * @author avibelli
 */
@Entity
public class ProductVersion implements Serializable {

    private static final long serialVersionUID = 6314079319551264379L;

    @Id
    @SequenceGenerator(name="product_version_id_seq", sequenceName="product_version_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="product_version_id_seq")
    private Integer id;

    private String version;

    private boolean released;

    private boolean supported;

    private String internalDownloadUrl;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Product product;

    @OneToMany(mappedBy = "productVersion")
    private Set<BuildConfigurationSet> buildConfigurationSets;

    @OneToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "buildrecordset_id")
    private BuildRecordSet buildRecordSet;

    public ProductVersion() {
        buildConfigurationSets = new HashSet<>();
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
     * Build record set represents the set of completed builds which produced the artifacts included in the product release
     *
     * @return The set of build records for this release
     */
    public BuildRecordSet getBuildRecordSet() {
        return buildRecordSet;
    }

    public void setBuildRecordSet(BuildRecordSet buildRecordSet) {
        this.buildRecordSet = buildRecordSet;
    }

    public Set<BuildConfigurationSet> getBuildConfigurationSets() {
        return buildConfigurationSets;
    }

    public void setBuildConfigurationSets(Set<BuildConfigurationSet> buildConfigurationSets) {
        this.buildConfigurationSets = buildConfigurationSets;
    }

    @Override
    public String toString() {
        return "ProductVersion [id=" + id + ", version=" + version + "]";
    }

    public static class Builder {

        private Integer id;

        private String version;

        private Product product;

        private boolean released;

        private boolean supported;

        private String internalDownloadUrl;

        private BuildRecordSet buildRecordSet;

        private Set<BuildConfigurationSet> buildConfigurationSets = new HashSet<>();

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductVersion build() {
            ProductVersion productVersion = new ProductVersion();
            productVersion.setId(id);
            productVersion.setVersion(version);
            productVersion.setReleased(released);
            productVersion.setSupported(supported);
            productVersion.setInternalDownloadUrl(internalDownloadUrl);
            productVersion.setBuildRecordSet(buildRecordSet);

            // Set the bi-directional mapping
            for (BuildConfigurationSet buildConfigurationSet : buildConfigurationSets) {
                buildConfigurationSet.setProductVersion(productVersion);
            }
            productVersion.setBuildConfigurationSets(buildConfigurationSets);

            if (product != null) {
                product.addVersion(productVersion);
                productVersion.setProduct(product);
            }

            return productVersion;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder released(boolean released) {
            this.released = released;
            return this;
        }

        public Builder supported(boolean supported) {
            this.supported = supported;
            return this;
        }

        public Builder internalDownloadUrl(String internalDownloadUrl) {
            this.internalDownloadUrl = internalDownloadUrl;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder buildRecordSet(BuildRecordSet buildRecordSet) {
            this.buildRecordSet = buildRecordSet;
            return this;
        }

        public Builder product(Product.Builder productBuilder) {
            this.product = productBuilder.build();
            return this;
        }
    }
}
