package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

/**
 * The Class BuildCollection, that encapsulates the set of buildResults that compose a specific version of a Product.
 *
 * There should be a unique constraint (productVersion, productBuildBumber)
 *
 * @author avibelli
 */
@Entity
public class BuildCollection implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer productBuildBumber;

    @Enumerated(EnumType.STRING)
    private ProductMilestone milestone;

    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_buildcollection_productversion")
    private ProductVersion productVersion;

    @ManyToMany
    @ForeignKey(name = "fk_buildcollection_projectbuildresult_buildcollection", inverseName = "fk_buildcollection_projectbuildresult_projectbuildresult")
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
     * @return the productBuildBumber
     */
    public Integer getProductBuildBumber() {
        return productBuildBumber;
    }

    /**
     * @param productBuildBumber the productBuildBumber to set
     */
    public void setProductBuildBumber(Integer productBuildBumber) {
        this.productBuildBumber = productBuildBumber;
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
                + productVersion.getVersion() + ", productBuildBumber=" + productBuildBumber + "]";
    }

}
