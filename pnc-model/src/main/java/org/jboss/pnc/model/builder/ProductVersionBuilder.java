package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

/**
 * @author avibelli
 */
public class ProductVersionBuilder {

    private Integer id;

    private String version;

    private Product product;

    private boolean released;

    private boolean supported;

    private String internalDownloadUrl;

    private BuildRecordSet buildRecordSet;

    private ProductVersionBuilder() {
    }

    public static ProductVersionBuilder newBuilder() {
        return new ProductVersionBuilder();
    }

    public ProductVersion build() {
        ProductVersion productVersion = new ProductVersion();
        productVersion.setId(id);
        productVersion.setVersion(version);
        productVersion.setReleased(released);
        productVersion.setSupported(supported);
        productVersion.setInternalDownloadUrl(internalDownloadUrl);
        productVersion.setBuildRecordSet(buildRecordSet);

        if (product != null) {
            product.addVersion(productVersion);
            productVersion.setProduct(product);
        }

        return productVersion;
    }

    public ProductVersionBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProductVersionBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ProductVersionBuilder released(boolean released) {
        this.released = released;
        return this;
    }

    public ProductVersionBuilder supported(boolean supported) {
        this.supported = supported;
        return this;
    }

    public ProductVersionBuilder internalDownloadUrl(String internalDownloadUrl) {
        this.internalDownloadUrl = internalDownloadUrl;
        return this;
    }

    public ProductVersionBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public ProductVersionBuilder buildRecordSet(BuildRecordSet buildRecordSet) {
        this.buildRecordSet = buildRecordSet;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public boolean isReleased() {
        return released;
    }

    public boolean isSupported() {
        return supported;
    }

    public String getInternalDownloadUrl() {
    	return internalDownloadUrl;
    }

    public Product getProduct() {
        return product;
    }

    public BuildRecordSet getBuildRecordSet() {
        return buildRecordSet;
    }

    public ProductVersionBuilder product(ProductBuilder productBuilder) {
        this.product = productBuilder.build();
        return this;
    }
}
