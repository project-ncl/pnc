/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author avibelli
 *
 */
@Entity
public class Product implements GenericEntity<Integer> {

    private static final long serialVersionUID = -9022966336791211855L;
    
    public static final String DEFAULT_SORTING_FIELD = "name";

    @Id
    @SequenceGenerator(name="product_id_seq", sequenceName="product_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="product_id_seq")
    private Integer id;

    @NotNull
    private String name;

    private String description;

    private String abbreviation;

    private String productCode;

    private String pgmSystemName;

    @OneToMany(mappedBy = "product", cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.REMOVE })
    private Set<ProductVersion> productVersions;

    /**
     * Instantiates a new product.
     */
    public Product() {
        productVersions = new HashSet<>();
    }

    /**
     * @param name
     * @param description
     */
    public Product(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Shortened informal name of the product
     */
    public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
     * @return Product code in internal systems
     */
	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

    /**
     * @return Name of the product used by program management planning system
     */
	public String getPgmSystemName() {
		return pgmSystemName;
	}

	public void setPgmSystemName(String pgmSystemName) {
		this.pgmSystemName = pgmSystemName;
	}

    /**
     * @return the productVersions
     */
    public Set<ProductVersion> getProductVersions() {
        return productVersions;
    }

    /**
     * @param productVersions the productVersions to set
     */
    public void setProductVersions(Set<ProductVersion> productVersions) {
        this.productVersions = productVersions;
    }

    /**
     * Add a version for the Product
     *
     * @param version
     * @return
     */
    public Set<ProductVersion> addVersion(ProductVersion version) {
        productVersions.add(version);
        return productVersions;
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String abbreviation;

        private String productCode;

        private String pgmSystemName;

        private Set<ProductVersion> productVersions;

        private Builder() {
            productVersions = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Product build() {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setDescription(description);
            product.setAbbreviation(abbreviation);
            product.setProductCode(productCode);
            product.setPgmSystemName(pgmSystemName);

            // Set the bi-directional mapping
            for (ProductVersion productVersion : productVersions) {
                productVersion.setProduct(product);
            }
            product.setProductVersions(productVersions);

            return product;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder abbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
            return this;
        }

        public Builder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder pgmSystemName(String pgmSystemName) {
            this.pgmSystemName = pgmSystemName;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersions.add(productVersion);
            return this;
        }

        public Builder productVersions(Set<ProductVersion> productVersions) {
            this.productVersions = productVersions;
            return this;
        }

        public Builder productVersion(ProductVersion.Builder productVersionBuilder) {
            this.productVersions.add(productVersionBuilder.build());
            return this;
        }

    }
}
