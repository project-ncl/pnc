package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "ProductVersion")
public class ProductVersionRest {

    private Integer id;

    private String version;

    private Integer productId;

    List<Integer> productMilestoneIds = new ArrayList<Integer>();

    List<Integer> productReleaseIds = new ArrayList<Integer>();

    List<Integer> buildConfigurationSetIds;

    public ProductVersionRest() {
    }

    public ProductVersionRest(ProductVersion productVersion) {
        this.id = productVersion.getId();
        this.version = productVersion.getVersion();
        this.productId = productVersion.getProduct().getId();
        
        for (ProductMilestone milestone : productVersion.getProductMilestones()) {
            productMilestoneIds.add(milestone.getId());
        }

        for (ProductRelease release : productVersion.getProductReleases()) {
            productReleaseIds.add(release.getId());
        }

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public List<Integer> getBuildConfigurationSetIds() {
        return buildConfigurationSetIds;
    }

    public void setBuildConfigurationSetIds(List<Integer> buildConfigurationSetIds) {
        this.buildConfigurationSetIds = buildConfigurationSetIds;
    }

    public List<Integer> getProductMilestones() {
        return this.productMilestoneIds;
    }

    public void setProductMilestoneIds(List<Integer> productMilestoneIds) {
        this.productMilestoneIds = productMilestoneIds;
    }

    public List<Integer> getProductReleases() {
        return this.productReleaseIds;
    }

    public void setProductReleaseIds(List<Integer> productReleaseIds) {
        this.productReleaseIds = productReleaseIds;
    }

    public ProductVersion toProductVersion(Product product) {
        ProductVersion productVersionToBeUpdated = getProductVersionFromProductOrNewOne(product);
        return toProductVersion(productVersionToBeUpdated);
    }

    public ProductVersion toProductVersion(ProductVersion productVersion) {
        productVersion.setVersion(version);
        return productVersion;
    }

    /**
     * Checks if ProductVersion is present in Product. If it is true - returns it or creates new one otherwise.
     */
    private ProductVersion getProductVersionFromProductOrNewOne(Product product) {
        List<ProductVersion> productVersionsInProduct = nullableStreamOf(product.getProductVersions())
                .filter(productVersion -> productVersion.getId().equals(id))
                .collect(Collectors.toList());

        if(!productVersionsInProduct.isEmpty()) {
            return productVersionsInProduct.get(0);
        }

        ProductVersion productVersion = new ProductVersion();
        productVersion.setProduct(product);
        product.getProductVersions().add(productVersion);
        return productVersion;
    }

}
