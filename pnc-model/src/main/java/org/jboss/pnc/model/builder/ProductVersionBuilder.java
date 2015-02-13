package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

import java.util.HashSet;
import java.util.Set;

/**
 * @author avibelli
 */
public class ProductVersionBuilder {

    private Integer id;

    private String version;

    private Product product;

    private Set<BuildRecordSet> productBuildRecordSets;

    private ProductVersionBuilder() {
        productBuildRecordSets = new HashSet<>();
    }

    public static ProductVersionBuilder newBuilder() {
        return new ProductVersionBuilder();
    }

    public ProductVersion build() {
        ProductVersion productVersion = new ProductVersion();
        productVersion.setId(id);
        productVersion.setVersion(version);

        if (product != null) {
            product.addVersion(productVersion);
            productVersion.setProduct(product);
        }

        // Set the bi-directional mapping
        for (BuildRecordSet buildRecordSet : productBuildRecordSets) {
            buildRecordSet.setProductVersion(productVersion);
        }
        productVersion.setProductBuildRecordSets(productBuildRecordSets);

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

    public ProductVersionBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public ProductVersionBuilder productBuildRecordSet(BuildRecordSet productBuildRecordSet) {
        this.productBuildRecordSets.add(productBuildRecordSet);
        return this;
    }

    public ProductVersionBuilder productBuildRecordSets(Set<BuildRecordSet> productBuildRecordSets) {
        this.productBuildRecordSets = productBuildRecordSets;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public Product getProduct() {
        return product;
    }

    public Set<BuildRecordSet> getProductBuildRecordSets() {
        return productBuildRecordSets;
    }

    public void setProductBuildRecordSets(Set<BuildRecordSet> productBuildRecordSets) {
        this.productBuildRecordSets = productBuildRecordSets;
    }

    public ProductVersionBuilder product(ProductBuilder productBuilder) {
        this.product = productBuilder.build();
        return this;
    }
}
