package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The Class BuildCollection, that encapsulates the set of buildResults that compose a specific version of a Product (may be
 * refactored to a proper entity)
 *
 * @author avibelli
 */
@Entity
@Table(name = "build_collection")
@NamedQuery(name = "BuildCollection.findAll", query = "SELECT b FROM BuildCollection b")
public class BuildCollection implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_version")
    private String productVersion;

    private String description;

    @ManyToMany
    @JoinTable(name = "build_collection_project_build_result", joinColumns = { @JoinColumn(name = "build_collection_id") }, inverseJoinColumns = { @JoinColumn(name = "project_build_result_id") })
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
     * Gets the product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name.
     *
     * @param productName the new product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets the product version.
     *
     * @return the product version
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the product version.
     *
     * @param productVersion the new product version
     */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the project build result.
     *
     * @return the project build result
     */
    public List<ProjectBuildResult> getProjectBuildResult() {
        return projectBuildResult;
    }

    /**
     * Sets the project build result.
     *
     * @param projectBuildResult the new project build result
     */
    public void setProjectBuildResult(List<ProjectBuildResult> projectBuildResult) {
        this.projectBuildResult = projectBuildResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BuildCollection [productName=" + productName + ", productVersion=" + productVersion + "]";
    }

}
