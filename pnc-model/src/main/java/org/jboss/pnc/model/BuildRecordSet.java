package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;

/**
 * The Class BuildRecordSet, that encapsulates the set of buildRecords that compose a specific version of a Product.
 *
 * @author avibelli
 */
@Entity
public class BuildRecordSet implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    public static final String DEFAULT_SORTING_FIELD = "id";

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ProductMilestone milestone;

    @OneToOne(optional = true, mappedBy = "buildRecordSet")
    private ProductVersion productVersion;

    @ManyToMany
    @JoinTable(name = "build_record_set_map", joinColumns = { @JoinColumn(name = "build_record_set_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "build_record_id", referencedColumnName = "id") })
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

    @Override
    public String toString() {
        return "BuildRecordSet [productName=" + productVersion.getProduct().getName() + ", productVersion="
                + productVersion.getVersion() + "]";
    }

}
