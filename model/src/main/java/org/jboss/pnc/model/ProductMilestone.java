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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a product milestone. A single product version, for example "1.0", can be associated with several product
 * milestones such as "1.0.0.build1", "1.0.0.build2", etc. A milestone represents the set of work (build records) that was
 * performed during a development cycle from the previous milestone until the end of the current milestone.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"version", "productVersion_id"})
})
public class ProductMilestone implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    public static final String SEQUENCE_NAME = "product_milestone_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
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

    /**
     * The release (or handoff) date of this milestone
     */
    private Date endDate;

    /**
     * The scheduled starting date of this milestone
     */
    private Date startingDate;

    /**
     * The scheduled ending date of this milestone
     */
    private Date plannedEndDate;

    /**
     * URL which can be used to download the product distribution
     */
    @Size(max=255)
    private String downloadUrl;

    /**
     * Issue tracker URL containing the set of issues fixed in this milestone
     */
    @Size(max=255)
    private String issueTrackerUrl;

    /**
     * The product major.minor version associated with this milestone.  After
     * initial creation of the milestone, the product version should never change.
     */
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @ForeignKey(name = "fk_productmilestone_productversion")
    @Index(name="idx_productmilestone_productversion")
    @JoinColumn(updatable = false)
    private ProductVersion productVersion;

    @OneToOne(mappedBy = "productMilestone")
    private ProductRelease productRelease;

    /**
     * The builds which were executed/performed during this milestone build cycle. This includes 
     * failed builds and builds which produced artifacts which were later replaced by subsequent 
     * builds. The intent of this field is to track total effort of a milestone, so for example, 
     * failed builds consumed machine and human resources even though they were not delivered with
     * the product distribution.
     */
    @OneToMany(mappedBy = "productMilestone", fetch = FetchType.EAGER)
    private Set<BuildRecord> performedBuilds;

    /**
     * Set of artifacts which were distributed in this product milestone.  At a minimum, this includes
     * the runtime artifacts of a product.  Some additional artifacts could be included if they 
     * are supported  and could include some
     * 
     * The BuildRecordSets associated with a milestone should be created when the milestone
     * is first created, and never updated after that.
     */
    @ManyToMany(fetch = FetchType.EAGER) //TODO remove eager fetch
    @JoinTable(name = "product_milestone_distributed_artifacts_map", joinColumns = {
            @JoinColumn(name = "product_milestone_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "artifact_id", referencedColumnName = "id") })
    @ForeignKey(name = "fk_product_milestone_distributed_artifacts_map")
    @Index(name = "idx_product_milestone_distributed_artifacts_map", columnNames = { "product_milestone_id",
            "artifact_id" })
    private Set<Artifact> distributedArtifacts;

    public ProductMilestone() {
        performedBuilds = new HashSet<>();
        distributedArtifacts = new HashSet<>();
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

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * The scheduled starting date of this milestone
     *
     * @return a Date
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
     * @return a Date
     */
    public Date getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(Date plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    /**
     * The release (or handoff) date of this milestone
     *
     * @return a Date
     */
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * URL which can be used to download the product distribution
     * 
     * @return The url where this milestone distribution can be downloaded
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

    public Set<BuildRecord> getPerformedBuilds() {
        return performedBuilds;
    }

    public void setPerformedBuilds(Set<BuildRecord> performedBuilds) {
        this.performedBuilds = performedBuilds;
    }

    public Set<Artifact> getDistributedArtifacts() {
        return distributedArtifacts;
    }

    public void setDistributedArtifacts(Set<Artifact> distributedArtifacts) {
        this.distributedArtifacts = distributedArtifacts;
    }

    public boolean addDistributedArtifact(Artifact distributedArtifact) {
        return distributedArtifacts.add(distributedArtifact);
    }

    public boolean removeDistributedArtifact(Artifact distributedArtifact) {
        return this.distributedArtifacts.remove(distributedArtifact);
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

        private Date endDate;

        private Date startingDate;

        private Date plannedEndDate;

        private String downloadUrl;

        private String issueTrackerUrl;

        private Set<BuildRecord> performedBuilds;

        private Set<Artifact> distributedArtifacts;

        private ProductRelease productRelease;

        private Builder() {
            performedBuilds = new HashSet<>();
            distributedArtifacts = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductMilestone build() {
            ProductMilestone productMilestone = new ProductMilestone();
            productMilestone.setId(id);
            productMilestone.setVersion(version);
            productMilestone.setEndDate(endDate);
            productMilestone.setStartingDate(startingDate);
            productMilestone.setPlannedEndDate(plannedEndDate);
            productMilestone.setDownloadUrl(downloadUrl);
            productMilestone.setIssueTrackerUrl(issueTrackerUrl);

            if (productVersion != null) {
                productVersion.addProductMilestone(productMilestone);
                productMilestone.setProductVersion(productVersion);
            }

            productMilestone.setPerformedBuilds(performedBuilds);
            productMilestone.setDistributedArtifacts(distributedArtifacts);

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

        public Builder endDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder startingDate(Date startingDate) {
            this.startingDate = startingDate;
            return this;
        }

        public Builder plannedEndDate(Date plannedEndDate) {
            this.plannedEndDate = plannedEndDate;
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

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder performedBuilds(Set<BuildRecord> performedBuilds) {
            this.performedBuilds = performedBuilds;
            return this;
        }

        public Builder performedBuild(BuildRecord performedBuild) {
            this.performedBuilds.add(performedBuild);
            return this;
        }

        public Builder distributedArtifacts(Set<Artifact> distributedArtifacts) {
            this.distributedArtifacts = distributedArtifacts;
            return this;
        }

        public Builder distributedArtifact(Artifact distributedArtifact) {
            this.distributedArtifacts.add(distributedArtifact);
            return this;
        }

        public Builder productRelease(ProductRelease productRelease) {
            this.productRelease = productRelease;
            return this;
        }

        /**
         * Safe way to try to get the associated product name without worrying about null pointers
         * 
         * @return The associated product name, or an empty string
         */
        public String getProductName() {
            if (productVersion != null && productVersion.getProduct() != null) {
                return productVersion.getProduct().getName();
            }
            return "";
        }
    }
}
