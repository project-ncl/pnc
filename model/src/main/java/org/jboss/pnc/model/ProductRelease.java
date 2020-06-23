/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.enums.SupportLevel;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.Date;

import static org.jboss.pnc.constants.Patterns.PRODUCT_RELEASE_VERSION;

/**
 * Represents a released version of a product. For example, a Beta, GA, or SP release. Each release is associated with a
 * product version (many releases for one version), and each release is associated with a single milestone (one to one).
 * For example, product version 1.0 could have three milestones (1.0.0.Build1, 1.0.0.Build2, and 1.0.0.Build3) and two
 * releases (1.0.0.Beta1 which was promoted from 1.0.0.Build1 and 1.0.0.GA which was promoted from 1.0.0.Build3).
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(indexes = @Index(name = "idx_productrelease_milestone", columnList = "productmilestone_id"))
public class ProductRelease implements GenericEntity<Integer> {

    private static final long serialVersionUID = 6314079319551264379L;

    public static final String SEQUENCE_NAME = "product_release_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Contains the milestone version string. This consists of a major, minor, and micro numeric version followed by an
     * alphanumeric qualifier. For example "1.0.0.ER1".
     */
    @Pattern(
            message = "The version should consist of three numeric parts and one alphanumeric qualifier each separated by a dot",
            regexp = PRODUCT_RELEASE_VERSION)
    @NotNull
    @Size(max = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    private SupportLevel supportLevel;

    private Date releaseDate;

    @NotNull
    @OneToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_productrelease_milestone"))
    private ProductMilestone productMilestone;

    /**
     * A CPE (Common Platform Enumeration) is a Red Hat identifier assigned to a particular product or product version
     * or product release. A product's CPE identifier is publicly used and can be found in numerous places to identify
     * content. CPEs are used to map packages that are security-relevant and delivered via security errata back to
     * products and by the CVE Engine to map errata to containers when grading.
     */
    @Size(max = 255)
    private String commonPlatformEnumeration;

    @Size(max = 50)
    private String productPagesCode;

    public ProductRelease() {

    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The product version entity associated with this release. The association is via the product milestone.
     * 
     * @return the product version entity associated with the linked product milestone.
     */
    public ProductVersion getProductVersion() {
        if (productMilestone != null) {
            return productMilestone.getProductVersion();
        }
        return null;
    }

    /**
     * The current support level of this product release.
     *
     * @return The support level enum
     */
    public SupportLevel getSupportLevel() {
        return supportLevel;
    }

    public void setSupportLevel(SupportLevel supportLevel) {
        this.supportLevel = supportLevel;
    }

    /**
     * The date of this release
     *
     * @return The date representing the release date
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
    }

    /**
     * @return common platform enumeration (cpe) of the product release
     */
    public String getCommonPlatformEnumeration() {
        return commonPlatformEnumeration;
    }

    /**
     * @return the common platform enumeration (cpe) of the product release to set
     */
    public void setCommonPlatformEnumeration(String commonPlatformEnumeration) {
        this.commonPlatformEnumeration = StringUtils.nullIfBlank(commonPlatformEnumeration);
    }

    /**
     * @return code of the product release from product pages
     */
    public String getProductPagesCode() {
        return productPagesCode;
    }

    /**
     * @param productPagesCode the code of the product release from product pages to set
     */
    public void setProductPagesCode(String productPagesCode) {
        this.productPagesCode = StringUtils.nullIfBlank(productPagesCode);
    }

    @Override
    public String toString() {
        return "ProductRelease [id=" + id + ", version=" + version + "]";
    }

    public static class Builder {

        private Integer id;

        private String version;

        private ProductMilestone productMilestone;

        private SupportLevel supportLevel;

        private Date releaseDate;

        private String commonPlatformEnumeration;

        private String productPagesCode;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public ProductRelease build() {
            ProductRelease productRelease = new ProductRelease();
            productRelease.setId(id);
            productRelease.setVersion(version);
            productRelease.setSupportLevel(supportLevel);
            productRelease.setReleaseDate(releaseDate);
            productRelease.setCommonPlatformEnumeration(commonPlatformEnumeration);
            productRelease.setProductPagesCode(productPagesCode);

            if (productMilestone != null) {
                productMilestone.setProductRelease(productRelease);
            }
            productRelease.setProductMilestone(productMilestone);

            return productRelease;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder supportLevel(SupportLevel supportLevel) {
            this.supportLevel = supportLevel;
            return this;
        }

        public Builder releaseDate(Date releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Builder commonPlatformEnumeration(String commonPlatformEnumeration) {
            this.commonPlatformEnumeration = commonPlatformEnumeration;
            return this;
        }

        public Builder productPagesCode(String productPagesCode) {
            this.productPagesCode = productPagesCode;
            return this;
        }

    }
}
