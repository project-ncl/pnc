package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.BuildCollection;
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

    private Set<BuildCollection> productBuildCollections;

    private ProductVersionBuilder() {
        productBuildCollections = new HashSet<>();
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
        for (BuildCollection buildCollection : productBuildCollections) {
            buildCollection.setProductVersion(productVersion);
        }
        productVersion.setProductBuildCollections(productBuildCollections);

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

    public ProductVersionBuilder productBuildCollection(BuildCollection productBuildCollection) {
        this.productBuildCollections.add(productBuildCollection);
        return this;
    }

    public ProductVersionBuilder productBuildCollections(Set<BuildCollection> productBuildCollections) {
        this.productBuildCollections = productBuildCollections;
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

    public Set<BuildCollection> getProductBuildCollections() {
        return productBuildCollections;
    }

    public ProductVersionBuilder product(ProductBuilder productBuilder) {
        this.product = productBuilder.build();
        return this;
    }
}
