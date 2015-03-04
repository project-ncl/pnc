package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "Product")
public class ProductRest {

    private Integer id;

    private String name;

    private String description;

    private String abbreviation;

    private String productCode;

    private String pgmSystemName;

    private List<Integer> productVersionIds;

    public ProductRest() {
    }

    public ProductRest(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.abbreviation = product.getAbbreviation();
        this.productCode = product.getProductCode();
        this.pgmSystemName = product.getPgmSystemName();
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

    public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getPgmSystemName() {
		return pgmSystemName;
	}

	public void setPgmSystemName(String pgmSystemName) {
		this.pgmSystemName = pgmSystemName;
	}

    public List<Integer> getProductVersionIds() {
        return productVersionIds;
    }

    public void setProductVersionIds(List<Integer> productVersionIds) {
        this.productVersionIds = productVersionIds;
    }

    public Product toProduct() {
        Product.Builder builder = Product.Builder.newBuilder();

        builder.id(id);
        builder.name(name);
        builder.description(description);
        builder.abbreviation(abbreviation);
        builder.productCode(productCode);
        builder.pgmSystemName(pgmSystemName);
        nullableStreamOf(productVersionIds).forEach(productVersionId -> {
            ProductVersion.Builder productVersionBuilder = ProductVersion.Builder.newBuilder().id(productVersionId);
            builder.productVersion(productVersionBuilder);
        });

        return builder.build();
    }
}
