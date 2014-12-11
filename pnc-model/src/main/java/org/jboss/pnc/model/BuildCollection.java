package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

/**
 * The Class BuildCollection, that encapsulates the set of buildResults that compose a specific version of a Product.
 *
 * There should be a unique constraint (productVersion, productBuildBumber)
 *
 * @author avibelli
 */
@Entity
@Table(name = "build_collection")
public class BuildCollection implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "product_build_number")
    private Integer productBuildNumber;

    @Column(nullable = false, length = 20, name = "product_milestone")
    @Enumerated(EnumType.STRING)
    private ProductMilestone productMilestone;

    @JoinColumn(name = "product_version_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_build_collection_product_version")
    private ProductVersion productVersion;

    @Column(name = "project_build_result")
    @ManyToMany
    @ForeignKey(name = "fk_build_collection_project_build_result_build_collection", inverseName = "fk_build_collection_project_build_result_project_build_result")
    private List<ProjectBuildResult> projectBuildResult;

    /**
     * Instantiates a new builds the collection.
     */
    public BuildCollection() {

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
     * @return the productMilestone
     */
    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    /**
     * @param productMilestone the productMilestone to set
     */
    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
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
     * @return the projectBuildResult
     */
    public List<ProjectBuildResult> getProjectBuildResult() {
        return projectBuildResult;
    }

    /**
     * @param projectBuildResult the projectBuildResult to set
     */
    public void setProjectBuildResult(List<ProjectBuildResult> projectBuildResult) {
        this.projectBuildResult = projectBuildResult;
    }

    @Override
    public String toString() {
        return "BuildCollection [productName=" + productVersion.getProduct().getName() + ", productVersion="
                + productVersion.getVersion() + ", productBuildNumber=" + productBuildNumber + "]";
    }

}
