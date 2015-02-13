package org.jboss.pnc.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * The Class BuildRecordSet, that encapsulates the set of buildRecords that compose a specific version of a Product.
 *
 * There should be a unique constraint (productVersion, productBuildNumber)
 *
 * @author avibelli
 */
@Entity
public class BuildRecordSet implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer productBuildNumber;

    @Enumerated(EnumType.STRING)
    private ProductMilestone milestone;

    @ManyToOne(cascade = CascadeType.ALL)
    private ProductVersion productVersion;

    @ManyToMany
    private List<BuildRecord> buildRecord;

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
     * @return the productBuildNumber
     */
    public Integer getProductBuildNumber() {
        return productBuildNumber;
    }

    /**
     * @param productBuildNumber the productBuildNumber to set
     */
    public void setProductBuildNumber(Integer productBuildNumber) {
        this.productBuildNumber = productBuildNumber;
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
                + productVersion.getVersion() + ", productBuildNumber=" + productBuildNumber + "]";
    }

}
