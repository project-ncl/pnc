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
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * Represents a released version of a product.  For example, a Beta, GA, or SP release.
 * Each release is associated with a product version (many releases for one version),
 * and each release is associated with a single milestone (one to one).
 */
@Entity
public class ProductRelease implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    @Id
    @SequenceGenerator(name="product_release_id_seq", sequenceName="product_release_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="product_release_id_seq")
    private Integer id;

    @NotNull
    private String version;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    private ProductVersion productVersion;

    @Enumerated(EnumType.STRING)
    private SupportLevel supportLevel;

    private Date releaseDate;

    private String downloadUrl;

    @NotNull
    @OneToOne(cascade = { CascadeType.REFRESH })
    private ProductMilestone productMilestone;

    @NotNull
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "build_record_set_id")
    private BuildRecordSet buildRecordSet;

    public ProductRelease() {

    }

    /**
     * @param version
     * @param product
     */
    public ProductRelease(ProductVersion productVersion, String version) {
        this.productVersion = productVersion;
        this.version = version;
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
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param product the product to set
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * The current support level of this product release.
     *
     * @return
     */
    public SupportLevel getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(SupportLevel supportLevel) {
        this.supportLevel = supportLevel;
    }

    /**
     * The date of this release
     *
     * @return
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * URL which can be used to download the product distribution
     *
     * @return
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
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

    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
    }

    @Override
    public String toString() {
        return "ProductRelease [id=" + id + ", version=" + version + "]";
    }

    /**
     * Contains the various possible support levels, such as UNRELEASED, SUPPORTED, EOL, etc..
     *
     */
    public enum SupportLevel {
        UNRELEASED,
        EARLYACCESS,
        SUPPORTED,
        EXTENDED_SUPPORT,
        EOL
    }

    public static class Builder {

        private Integer id;

        private String version;

        private ProductVersion productVersion;

        private ProductMilestone productMilestone;

        private SupportLevel supportLevel;

        private Date releaseDate;

        private String downloadUrl;

        private BuildRecordSet buildRecordSet;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductRelease build() {
            ProductRelease productRelease = new ProductRelease();
            productRelease.setId(id);
            productRelease.setVersion(version);
            productRelease.setSupportLevel(supportLevel);
            productRelease.setReleaseDate(releaseDate);
            productRelease.setDownloadUrl(downloadUrl);

            if (buildRecordSet == null) {
                buildRecordSet = new BuildRecordSet();
            }
            buildRecordSet.setProductRelease(productRelease);
            productRelease.setBuildRecordSet(buildRecordSet);

            if (productVersion != null) {
                productVersion.addProductRelease(productRelease);
            }
            productRelease.setProductVersion(productVersion);

            if (productMilestone != null) {
                productMilestone.setProductRelease(productRelease);
            }
            productRelease.setProductMilestone(productMilestone);

            return productRelease;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder supportLevel(SupportLevel supportLevel) {
            this.supportLevel = supportLevel;
            return this;
        }

        public Builder releaseDate(Date releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Builder buildRecordSet(BuildRecordSet buildRecordSet) {
            this.buildRecordSet = buildRecordSet;
            return this;
        }

    }
}
