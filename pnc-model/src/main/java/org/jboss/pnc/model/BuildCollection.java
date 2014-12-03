package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The Class BuildCollection, that encapsulates the set of buildResults that compose a specific version of a Product.
 *
 * There should be a unique constraint (productVersion, productBuildBumber)
 *
 * @author avibelli
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "productVersion", "productBuildBumber" }) })
public class BuildCollection implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer productBuildBumber;

    @ManyToOne
    private ProductVersion productVersion;

    @ManyToMany
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
