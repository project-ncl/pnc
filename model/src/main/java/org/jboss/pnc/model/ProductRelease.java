/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * Represents a released version of a product. For example, a Beta, GA, or SP release. Each release is associated with a product
 * version (many releases for one version), and each release is associated with a single milestone (one to one). For example,
 * product version 1.0 could have three milestones (1.0.0.Build1, 1.0.0.Build2, and 1.0.0.Build3) and two releases (1.0.0.Beta1
 * which was promoted from 1.0.0.Build1 and 1.0.0.GA which was promoted from 1.0.0.Build3).
 */
@Entity
public class ProductRelease implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    public static final String SEQUENCE_NAME = "product_release_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
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

    public ProductRelease(ProductVersion productVersion, String version) {
        this.productVersion = productVersion;
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The product version entity associated with this release
     * 
     * @return the product version entity
     */
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * The current support level of this product release.
     *
     * @return The support level enum
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
     * @return The date representing the release date
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
        UNRELEASED, EARLYACCESS, SUPPORTED, EXTENDED_SUPPORT, EOL
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
