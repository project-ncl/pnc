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
package org.jboss.pnc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.ForeignKey;

/**
 * Class that contains all the versions for a Product
 *
 * @author avibelli
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"version", "product_id"})
})
public class ProductVersion implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    public static final String SEQUENCE_NAME = "product_version_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * The version string that represents this product version.  This should normally
     * consist of a major and minor version separated by a dot, for example "1.0".
     */
    @Pattern(message="The version should consist of two numeric parts separated by a dot" , regexp="^[0-9]+\\.[0-9]+$")
    @NotNull
    private String version;

    @NotNull
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @ForeignKey(name = "fk_productversion_product")
    private Product product;

    @OneToMany(mappedBy = "productVersion")
    private Set<BuildConfigurationSet> buildConfigurationSets;

    @OneToMany(mappedBy = "productVersion")
    private Set<ProductRelease> productReleases;

    @OneToMany(mappedBy = "productVersion")
    private Set<ProductMilestone> productMilestones;

    @OneToOne
    @ForeignKey(name = "fk_productversion_currentmilestone")
    private ProductMilestone currentProductMilestone;

    public ProductVersion() {
        buildConfigurationSets = new HashSet<>();
        productReleases = new HashSet<ProductRelease>();
        productMilestones = new HashSet<ProductMilestone>();
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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    public Set<ProductRelease> getProductReleases() {
        return productReleases;
    }

    public void setProductReleases(Set<ProductRelease> productReleases) {
        this.productReleases = productReleases;
    }

    public void addProductRelease(ProductRelease productRelease) {
        this.productReleases.add(productRelease);
    }

    public Set<ProductMilestone> getProductMilestones() {
        return productMilestones;
    }

    public void setProductMilestones(Set<ProductMilestone> productMilestones) {
        this.productMilestones = productMilestones;
    }

    public void addProductMilestone(ProductMilestone productMilestone) {
        this.productMilestones.add(productMilestone);
    }

    public ProductMilestone getCurrentProductMilestone() {
        return this.currentProductMilestone;
    }

    public void setCurrentProductMilestone(ProductMilestone currentProductMilestone) {
        this.currentProductMilestone = currentProductMilestone;
    }

    public Set<BuildConfigurationSet> getBuildConfigurationSets() {
        return buildConfigurationSets;
    }

    public void setBuildConfigurationSets(Set<BuildConfigurationSet> buildConfigurationSets) {
        this.buildConfigurationSets = buildConfigurationSets;
    }

    @Override
    public String toString() {
        return "ProductVersion [id=" + id + ", version=" + version + "]";
    }

    public static class Builder {

        private Integer id;

        private String version;

        private Product product;

        private Set<ProductRelease> productReleases = new HashSet<>();

        private Set<ProductMilestone> productMilestones = new HashSet<>();

        private ProductMilestone currentProductMilestone;

        private Set<BuildConfigurationSet> buildConfigurationSets = new HashSet<>();

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductVersion build() {
            ProductVersion productVersion = new ProductVersion();
            productVersion.setId(id);
            productVersion.setVersion(version);
            productVersion.setCurrentProductMilestone(currentProductMilestone);

            if (product != null) {
                product.addVersion(productVersion);
                productVersion.setProduct(product);
            }

            // Set the bi-directional mappings
            for (BuildConfigurationSet buildConfigurationSet : buildConfigurationSets) {
                buildConfigurationSet.setProductVersion(productVersion);
            }
            productVersion.setBuildConfigurationSets(buildConfigurationSets);

            for (ProductRelease productRelease : productReleases) {
                productRelease.setProductVersion(productVersion);
            }
            productVersion.setProductReleases(productReleases);

            for (ProductMilestone productMilestone : productMilestones) {
                productMilestone.setProductVersion(productVersion);
            }
            productVersion.setProductMilestones(productMilestones);

            return productVersion;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder product(Product product) {
            this.product = product;
            return this;
        }

        public Builder productReleases(Set<ProductRelease> productReleases) {
            this.productReleases = productReleases;
            return this;
        }

        public Builder productMilestones(Set<ProductMilestone> productMilestones) {
            this.productMilestones = productMilestones;
            return this;
        }

        public Builder currentProductMilestone(ProductMilestone currentProductMilestone) {
            this.currentProductMilestone = currentProductMilestone;
            return this;
        }

        public Builder product(Product.Builder productBuilder) {
            this.product = productBuilder.build();
            return this;
        }
    }
}
