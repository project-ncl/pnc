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
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    /**
     * If this field is non-null it means that the current build record set represents
     * the builds performed for the linked product milestone cycle.
     */
    @OneToOne(mappedBy = "performedBuildRecordSet")
    private ProductMilestone performedInProductMilestone;

    @ManyToMany
    @JoinTable(name = "build_record_set_map", joinColumns = { @JoinColumn(name = "build_record_set_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "build_record_id", referencedColumnName = "id") })
    @ForeignKey(name = "fk_build_record_set_map_buildrecordset", inverseName = "fk_build_record_set_map_buildrecord")
    @Index(name="idx_build_record_set_map_buildrecordset", columnNames={"build_record_set_id", "build_record_id"} )
    private Set<BuildRecord> buildRecords;

    /**
     * Instantiates a new builds the collection.
     */
    public BuildRecordSet() {
        buildRecords = new HashSet<BuildRecord>();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductMilestone getPerformedInProductMilestone() {
        return performedInProductMilestone;
    }

    public void setPerformedInProductMilestone(ProductMilestone performedInProductMilestone) {
        this.performedInProductMilestone = performedInProductMilestone;
    }

    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    public void setBuildRecords(Set<BuildRecord> records) {
        if (records == null) {
            this.buildRecords = new HashSet<>();
        } else {
            this.buildRecords = records;
        }
    }

    public boolean addBuildRecord(BuildRecord buildRecord) {
        if (!buildRecord.getBuildRecordSets().contains(this)) {
            buildRecord.addBuildRecordSet(this);
        }
        return this.buildRecords.add(buildRecord);
    }

    public static class Builder {

        private Integer id;

        private String description;

        private ProductMilestone performedInProductMilestone;

        private Set<BuildRecord> buildRecords;

        private Builder() {
            buildRecords = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecordSet build() {
            BuildRecordSet buildRecordSet = new BuildRecordSet();
            buildRecordSet.setId(id);
            buildRecordSet.setDescription(description);

            // Set the bi-directional mappings
            if (performedInProductMilestone != null) {
                performedInProductMilestone.setPerformedBuildRecordSet(buildRecordSet);
            }
            buildRecordSet.setPerformedInProductMilestone(performedInProductMilestone);

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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder performedInProductMilestone(ProductMilestone performedInProductMilestone) {
            this.performedInProductMilestone = performedInProductMilestone;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecords.add(buildRecord);
            return this;
        }

        public Builder buildRecords(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

    }
}
