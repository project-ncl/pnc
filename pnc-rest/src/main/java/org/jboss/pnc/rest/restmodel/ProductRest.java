package org.jboss.pnc.rest.restmodel;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.builder.ProductBuilder;

@XmlRootElement(name = "Product")
public class ProductRest {

    private Integer id;

    private String name;

    private String description;

    private List<Integer> productVersionIds;

    public ProductRest() {
    }

    public ProductRest(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.productVersionIds = nullableStreamOf(product.getProductVersions()).map(productVersion -> productVersion.getId())
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getProductVersionIds() {
        return productVersionIds;
    }

    public void setProductVersionIds(List<Integer> productVersionIds) {
        this.productVersionIds = productVersionIds;
    }

    @XmlTransient
    public Product getProduct(ProductRest productRest) {
        ProductBuilder builder = ProductBuilder.newBuilder();
        builder.name(productRest.getName());
        builder.description(productRest.getDescription());
        return builder.build();
    }
}
