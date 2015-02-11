package org.jboss.pnc.rest.restmodel;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.builder.ProductVersionBuilder;

@XmlRootElement(name = "ProductVersion")
public class ProductVersionRest {

    private Integer id;

    private String version;

    private Integer productId;

    List<Integer> projectIds;

    public ProductVersionRest() {
    }

    public ProductVersionRest(ProductVersion productVersion) {
        this.id = productVersion.getId();
        this.version = productVersion.getVersion();
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

    public ProductVersion toProductVersion() {
        ProductVersionBuilder builder = ProductVersionBuilder.newBuilder();
        builder.id(id);
        builder.version(version);
        return builder.build();
    }

}
