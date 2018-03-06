/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.restmodel;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "Product")
public class ProductRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String name;

    private String description;

    @NotNull(groups =  { WhenCreatingNew.class, WhenUpdating.class })
    @Pattern(regexp = "[a-zA-Z0-9-]+", groups = { WhenCreatingNew.class, WhenUpdating.class })
    private String abbreviation;

    private String productCode;

    private String pgmSystemName;

    private List<Integer> productVersionIds;

    @Getter
    @Setter
    private Set<ProductVersionRefRest> productVersionRefs;

    public ProductRest() {
    }

    public ProductRest(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.abbreviation = product.getAbbreviation();
        this.productCode = product.getProductCode();
        this.pgmSystemName = product.getPgmSystemName();
        this.productVersionIds = nullableStreamOf(product.getProductVersions()).map(ProductVersion::getId)
                .collect(Collectors.toList());
        this.productVersionRefs = nullableStreamOf(product.getProductVersions()).map(ProductVersionRefRest::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
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

    public Product.Builder toDBEntityBuilder() {
        Product.Builder builder = Product.Builder.newBuilder()
                .id(id)
                .name(name)
                .description(description)
                .abbreviation(abbreviation)
                .productCode(productCode)
                .pgmSystemName(pgmSystemName);

        nullableStreamOf(productVersionIds).forEach(productVersionId -> {
            ProductVersion.Builder productVersionBuilder = ProductVersion.Builder.newBuilder().id(productVersionId);
            builder.productVersion(productVersionBuilder);
        });

        return builder;
    }
}
