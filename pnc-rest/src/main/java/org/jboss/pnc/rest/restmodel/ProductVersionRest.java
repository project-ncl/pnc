package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "ProductVersion")
public class ProductVersionRest {

    private Integer id;

    private String version;

    private boolean released;

    private boolean supported;

    private String internalDownloadUrl;

    private Integer productId;

    List<Integer> projectIds;

    public ProductVersionRest() {
    }

    public ProductVersionRest(ProductVersion productVersion) {
        this.id = productVersion.getId();
        this.version = productVersion.getVersion();
        this.released = productVersion.isReleased();
        this.supported = productVersion.isSupported();
        this.internalDownloadUrl = productVersion.getInternalDownloadUrl();
        this.productId = productVersion.getProduct().getId();
        this.projectIds = nullableStreamOf(productVersion.getProductVersionProjects()).map(
                productVersionProjects -> productVersionProjects.getProject().getId()).collect(Collectors.toList());

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

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getInternalDownloadUrl() {
        return internalDownloadUrl;
    }

    public void setInternalDownloadUrl(String internalDownloadUrl) {
        this.internalDownloadUrl = internalDownloadUrl;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public List<Integer> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Integer> projectIds) {
        this.projectIds = projectIds;
    }

    public ProductVersion toProductVersion(Product product) {
        ProductVersion productVersionToBeUpdated = getProductVersionFromProductOrNewOne(product);
        return toProductVersion(productVersionToBeUpdated);
    }

    public ProductVersion toProductVersion(ProductVersion productVersion) {
        productVersion.setVersion(version);
        productVersion.setReleased(released);
        productVersion.setSupported(supported);
        productVersion.setInternalDownloadUrl(internalDownloadUrl);
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
