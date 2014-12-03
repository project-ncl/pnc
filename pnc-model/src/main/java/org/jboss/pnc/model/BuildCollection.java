package org.jboss.pnc.model;

import javax.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * The Class BuildCollection, that encapsulates the set of buildResults that compose a specific version of a Product (may be
 * refactored to a proper entity)
 *
 * @author avibelli
 */
@Entity
public class BuildCollection implements Serializable {

    private static final long serialVersionUID = 1633628406382742445L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer productBuildBumber;

    @ManyToOne(cascade = CascadeType.ALL)
    private Product product;

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
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
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
        return "BuildCollection [productName=" + product.getName() + ", productVersion=" + product.getVersion()
                + ", productBuildBumber=" + productBuildBumber + "]";
    }

}
