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

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

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
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Contains the milestone version string.  This consists of a major, minor, and micro
     * numeric version followed by an alphanumeric qualifier.  For example "1.0.0.ER1".
     */
    @Pattern(message="The version should consist of three numeric parts and one alphanumeric qualifier each separated by a dot" , regexp="^[0-9]+\\.[0-9]+\\.[0-9]+\\.[\\w]+$")
    @NotNull
    @Size(max=50)
    private String version;

    @Enumerated(EnumType.STRING)
    private SupportLevel supportLevel;

    private Date releaseDate;

    @Size(max=255)
    private String downloadUrl;

    /**
     * Issue tracker URL containing the set of issues fixed in this release
     */
    @Size(max=255)
    private String issueTrackerUrl;

    @NotNull
    @OneToOne(cascade = { CascadeType.REFRESH })
    @ForeignKey(name = "fk_productrelease_milestone")
    @Index(name="idx_productrelease_milestone")
    private ProductMilestone productMilestone;

    public ProductRelease() {

    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
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
     * The product version entity associated with this release.  The association is via
     * the product milestone.
     * 
     * @return the product version entity associated with the linked product milestone.
     */
    public ProductVersion getProductVersion() {
        if (productMilestone != null) {
            return productMilestone.getProductVersion();
        }
        return null;
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
     * @return The url to download this release
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
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

        private ProductMilestone productMilestone;

        private SupportLevel supportLevel;

        private Date releaseDate;

        private String downloadUrl;

        private String issueTrackerUrl;

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
            productRelease.setIssueTrackerUrl(issueTrackerUrl);

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

        public Builder issueTrackerUrl(String issueTrackerUrl) {
            this.issueTrackerUrl = issueTrackerUrl;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

    }
}
