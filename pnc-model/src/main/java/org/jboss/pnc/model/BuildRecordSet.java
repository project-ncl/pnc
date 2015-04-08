package org.jboss.pnc.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class BuildRecordSet, that encapsulates the set of buildRecords that compose a specific version of a Product.
 *
 * @author avibelli
 */
@Entity
public class BuildRecordSet implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1633628406382742445L;

    public static final String DEFAULT_SORTING_FIELD = "id";

    @Id
    @SequenceGenerator(name="build_record_set_id_seq", sequenceName="build_record_set_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="build_record_set_id_seq")
    private Integer id;

    private String buildSetContentId;

    @Enumerated(EnumType.STRING)
    private ProductMilestone milestone;

    @OneToOne(mappedBy = "buildRecordSet")
    private ProductVersion productVersion;

    @ManyToMany
    @JoinTable(name = "build_record_set_map", joinColumns = {
            @JoinColumn(name = "build_record_set_id", referencedColumnName = "id") }, inverseJoinColumns = {
            @JoinColumn(name = "build_record_id", referencedColumnName = "id") })
    private List<BuildRecord> buildRecord;

    /**
     * Instantiates a new builds the collection.
     */
    public BuildRecordSet() {

    }

    @PreRemove
    private void removeFromProductVersion() {
        if (productVersion != null) {
            productVersion.setBuildRecordSet(null);
        }
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
    public ProductMilestone getMilestone() {
        return milestone;
    }

    /**
     * @param milestone the milestone to set
     */
    public void setMilestone(ProductMilestone milestone) {
        this.milestone = milestone;
    }

    /**
     * @return the productVersion
     */
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion the productVersion to set
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * @return the buildRecord
     */
    public List<BuildRecord> getBuildRecord() {
        return buildRecord;
    }

    /**
     * @param buildRecord the buildRecord to set
     */
    public void setBuildRecord(List<BuildRecord> record) {
        this.buildRecord = record;
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
        return "BuildRecordSet [productName=" + productVersion.getProduct().getName() + ", productVersion=" + productVersion
                .getVersion() + "]";
    }

    public static class Builder {

        private Integer id;

        private String buildSetContentId;

        private ProductMilestone milestone;

        private ProductVersion productVersion;

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
            buildRecordSet.setMilestone(milestone);

            if (productVersion != null) {
                productVersion.setBuildRecordSet(buildRecordSet);
            }
            buildRecordSet.setProductVersion(productVersion);

            // Set the bi-directional mapping
            for (BuildRecord buildRecord : buildRecords) {
                buildRecord.getBuildRecordSets().add(buildRecordSet);
            }

            buildRecordSet.setBuildRecord(buildRecords);

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

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder milestone(ProductMilestone milestone) {
            this.milestone = milestone;
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
