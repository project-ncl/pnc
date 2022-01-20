/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.constants.Patterns;

/**
 * @author avibelli
 *
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = { @UniqueConstraint(name = "uk_product_abbreviation", columnNames = "abbreviation"),
                @UniqueConstraint(name = "uk_product_name", columnNames = "name") })
public class Product implements GenericEntity<Integer> {

    private static final long serialVersionUID = -9022966336791211855L;

    public static final String DEFAULT_SORTING_FIELD = "name";
    public static final String SEQUENCE_NAME = "product_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Column(unique = true)
    @NotNull
    @Size(max = 255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Column(unique = true)
    @NotNull
    @Size(max = 20)
    @Pattern(regexp = Patterns.PRODUCT_ABBREVIATION)
    private String abbreviation;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "product", cascade = { CascadeType.REFRESH, CascadeType.DETACH, CascadeType.REMOVE })
    private Set<ProductVersion> productVersions;

    @Size(max = 255)
    private String productManagers;

    @Size(max = 50)
    private String productPagesCode;

    /**
     * Instantiates a new product.
     */
    public Product() {
        productVersions = new HashSet<>();
    }

    public Product(String name, String description) {
        this();
        this.name = name;
        this.description = StringUtils.nullIfBlank(description);
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
    @Override
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
        this.description = StringUtils.nullIfBlank(description);
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
     * @return comma-separated list of Product Managers of the product
     */
    public String getProductManagers() {
        return productManagers;
    }

    /**
     * @param productManagers comma-separated list of Product Managers to set
     */
    public void setProductManagers(String productManagers) {
        this.productManagers = StringUtils.nullIfBlank(productManagers);
    }

    /**
     * @return code of the product from product pages
     */
    public String getProductPagesCode() {
        return productPagesCode;
    }

    /**
     * @param productPagesCode the code of the product from product pages to set
     */
    public void setProductPagesCode(String productPagesCode) {
        this.productPagesCode = StringUtils.nullIfBlank(productPagesCode);
    }

    /**
     * Add a version for the Product
     *
     * @param version Associate a new version with this product
     * @return True if the product did not already contain this version
     */
    public boolean addVersion(ProductVersion version) {
        version.setProduct(this);
        return productVersions.add(version);
    }

    /**
     * Remove a version from the Product
     *
     * @param version Remove version from this product
     * @return True if the product did contain that version
     */
    public boolean removeVersion(ProductVersion version) {
        version.setProduct(null);
        return productVersions.remove(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        return id != null && id.equals(((Product) o).getId());
    }

    @Override
    public int hashCode() {
        // Because the id is generated when the entity is stored to DB, we need to have constant hash code to achieve
        // equals+hashCode consistency across all JPA object states
        return 31;
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String abbreviation;

        private Set<ProductVersion> productVersions;

        private String productManagers;

        private String productPagesCode;

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
            product.setProductManagers(productManagers);
            product.setProductPagesCode(productPagesCode);

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

        public Builder productManagers(String productManagers) {
            this.productManagers = productManagers;
            return this;
        }

        public Builder productPagesCode(String productPagesCode) {
            this.productPagesCode = productPagesCode;
            return this;
        }

    }
}
