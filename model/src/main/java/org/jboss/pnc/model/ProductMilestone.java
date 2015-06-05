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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

/**
 * Represents a product milestone. A single product version, for example "1.0", can be associated with several product
 * milestones such as "1.0.0.build1", "1.0.0.build2", etc. A milestone represents the set of work (build records) that was
 * performed during a development cycle from the previous milestone until the end of the current milestone.
 */
@Entity
public class ProductMilestone implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    public static final String SEQUENCE_NAME = "product_milestone_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @NotNull
    private String version;

    private Date releaseDate;

    private Date startingDate;

    private Date plannedReleaseDate;

    private String downloadUrl;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @ForeignKey(name = "fk_productmilestone_productversion")
    private ProductVersion productVersion;

    @OneToOne(mappedBy = "productMilestone")
    private ProductRelease productRelease;

    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @ForeignKey(name = "fk_productmilestone_buildrecordset")
    private BuildRecordSet buildRecordSet;

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

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * The scheduled starting date of this milestone
     *
     * @return
     */
    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    /**
     * The scheduled ending date of this milestone
     *
     * @return
     */
    public Date getPlannedReleaseDate() {
        return plannedReleaseDate;
    }

    public void setPlannedReleaseDate(Date plannedReleaseDate) {
        this.plannedReleaseDate = plannedReleaseDate;
    }

    /**
     * The release (or handoff) date of this milestone
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
     * Build record set represents the set of builds which were executed during this milestone build cycle
     *
     * @return The set of build records for this release
     */
    public BuildRecordSet getBuildRecordSet() {
        return buildRecordSet;
    }

    public void setBuildRecordSet(BuildRecordSet buildRecordSet) {
        this.buildRecordSet = buildRecordSet;
    }

    /**
     * If this milestone was promoted to a release, this field will be set. Will be null if the milestone was not relesed.
     * 
     * @return the product release or null
     */
    public ProductRelease getProductRelease() {
        return productRelease;
    }

    public void setProductRelease(ProductRelease productRelease) {
        this.productRelease = productRelease;
    }

    @Override
    public String toString() {
        return "ProductMilestone [id=" + id + ", version=" + version + "]";
    }

    public static class Builder {

        private Integer id;

        private String version;

        private ProductVersion productVersion;

        private Date releaseDate;

        private Date startingDate;

        private Date plannedReleaseDate;

        private String downloadUrl;

        private BuildRecordSet buildRecordSet;

        private ProductRelease productRelease;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductMilestone build() {
            ProductMilestone productMilestone = new ProductMilestone();
            productMilestone.setId(id);
            productMilestone.setVersion(version);
            productMilestone.setReleaseDate(releaseDate);
            productMilestone.setStartingDate(startingDate);
            productMilestone.setPlannedReleaseDate(plannedReleaseDate);
            productMilestone.setDownloadUrl(downloadUrl);

            if (buildRecordSet == null) {
                buildRecordSet = new BuildRecordSet();
            }
            buildRecordSet.setProductMilestone(productMilestone);
            productMilestone.setBuildRecordSet(buildRecordSet);

            if (productVersion != null) {
                productVersion.addProductMilestone(productMilestone);
                productMilestone.setProductVersion(productVersion);
            }

            if (productRelease != null) {
                productRelease.setProductMilestone(productMilestone);
                productMilestone.setProductRelease(productRelease);
            }

            return productMilestone;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder releaseDate(Date releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder startingDate(Date startingDate) {
            this.startingDate = startingDate;
            return this;
        }

        public Builder plannedReleaseDate(Date plannedReleaseDate) {
            this.plannedReleaseDate = plannedReleaseDate;
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

        public Builder buildRecordSet(BuildRecordSet buildRecordSet) {
            this.buildRecordSet = buildRecordSet;
            return this;
        }

        public Builder productRelease(ProductRelease productRelease) {
            this.productRelease = productRelease;
            return this;
        }
    }
}
