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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.ForeignKey;

/**
 * Represents a set of related build records. For example, this could be the set of builds that were executed during a specific
 * product milestone cycle.
 */
@Entity
public class BuildRecordSet implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1633628406382742445L;

    public static final String DEFAULT_SORTING_FIELD = "id";
    public static final String SEQUENCE_NAME = "build_record_set_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    private String buildSetContentId;

    @OneToOne(mappedBy = "buildRecordSet")
    private ProductMilestone productMilestone;

    @OneToOne(mappedBy = "buildRecordSet")
    private ProductRelease productRelease;

    @ManyToMany
    @JoinTable(name = "build_record_set_map", joinColumns = { @JoinColumn(name = "build_record_set_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "build_record_id", referencedColumnName = "id") })
    @ForeignKey(name = "fk_build_record_set_map_buildrecordset", inverseName = "fk_build_record_set_map_buildrecord")
    private List<BuildRecord> buildRecords;

    /**
     * Instantiates a new builds the collection.
     */
    public BuildRecordSet() {

    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the milestone
     */
    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    /**
     * @param productMilestone the milestone to set
     */
    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
    }

    public ProductRelease getProductRelease() {
        return productRelease;
    }

    public void setProductRelease(ProductRelease productRelease) {
        this.productRelease = productRelease;
    }

    /**
     * @return the buildRecord
     */
    public List<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    /**
     * @param record the BuildRecord(s) to set
     */
    public void setBuildRecords(List<BuildRecord> records) {
        this.buildRecords = records;
    }

    public String getBuildSetContentId() {
        return this.buildSetContentId;
    }

    /**
     * @param buildSetContentId The identifier to use when aggregating and retrieving content related to this record set which
     *        is stored via external services.
     */
    public void setBuildSetContentId(String buildSetContentId) {
        this.buildSetContentId = buildSetContentId;
    }

    @Override
    public String toString() {
        String version = "none";
        if (productRelease != null) {
            version = productRelease.getVersion();
        } else if (productMilestone != null) {
            version = productMilestone.getVersion();
        }
        return "BuildRecordSet [id=" + getId() + ", version=" + version + "]";
    }

    public static class Builder {

        private Integer id;

        private String buildSetContentId;

        private ProductMilestone productMilestone;

        private ProductRelease productRelease;

        private List<BuildRecord> buildRecords;

        private Builder() {
            buildRecords = new ArrayList<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecordSet build() {
            BuildRecordSet buildRecordSet = new BuildRecordSet();
            buildRecordSet.setId(id);
            buildRecordSet.setBuildSetContentId(buildSetContentId);

            // Set the bi-directional mappings
            if (productMilestone != null) {
                productMilestone.setBuildRecordSet(buildRecordSet);
            }
            buildRecordSet.setProductMilestone(productMilestone);

            if (productRelease != null) {
                productRelease.setBuildRecordSet(buildRecordSet);
            }
            buildRecordSet.setProductRelease(productRelease);

            for (BuildRecord buildRecord : buildRecords) {
                buildRecord.getBuildRecordSets().add(buildRecordSet);
            }
            buildRecordSet.setBuildRecords(buildRecords);

            return buildRecordSet;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder buildSetContentId(String buildSetContentId) {
            this.buildSetContentId = buildSetContentId;
            return this;
        }

        public Builder productRelease(ProductRelease productRelease) {
            this.productRelease = productRelease;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecords.add(buildRecord);
            return this;
        }

        public Builder buildRecords(List<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

    }
}
