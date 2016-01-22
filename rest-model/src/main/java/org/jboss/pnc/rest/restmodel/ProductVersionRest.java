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

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "ProductVersion")
public class ProductVersionRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String version;

    private Integer productId;

    private Integer currentProductMilestoneId;

    List<ProductMilestoneRest> productMilestones = new ArrayList<ProductMilestoneRest>();

    List<ProductReleaseRest> productReleases = new ArrayList<ProductReleaseRest>();

    List<BuildConfigurationSetRest> buildConfigurationSets = new ArrayList<BuildConfigurationSetRest>();

    List<BuildConfigurationRest> buildConfigurations = new ArrayList<BuildConfigurationRest>();

    public ProductVersionRest() {
    }

    public ProductVersionRest(ProductVersion productVersion) {
        this.id = productVersion.getId();
        this.version = productVersion.getVersion();
        this.productId = productVersion.getProduct().getId();
        this.currentProductMilestoneId = productVersion.getCurrentProductMilestone() != null
                ? productVersion.getCurrentProductMilestone().getId() : null;

        for (ProductMilestone milestone : productVersion.getProductMilestones()) {
            productMilestones.add(new ProductMilestoneRest(milestone));
            if (milestone.getProductRelease() != null) {
                productReleases.add(new ProductReleaseRest(milestone.getProductRelease()));
            }
        }

        for (BuildConfiguration buildConfiguration : productVersion.getBuildConfigurations()) {
            buildConfigurations.add(new BuildConfigurationRest(buildConfiguration));
        }

        for (BuildConfigurationSet buildConfigurationSet : productVersion.getBuildConfigurationSets()) {
            buildConfigurationSets.add(new BuildConfigurationSetRest(buildConfigurationSet));
        }

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

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public List<ProductMilestoneRest> getProductMilestones() {
        return productMilestones;
    }

    public void setProductMilestones(List<ProductMilestoneRest> productMilestones) {
        this.productMilestones = productMilestones;
    }

    public List<ProductReleaseRest> getProductReleases() {
        return productReleases;
    }

    public void setProductReleases(List<ProductReleaseRest> productReleases) {
        this.productReleases = productReleases;
    }

    public List<BuildConfigurationSetRest> getBuildConfigurationSets() {
        return buildConfigurationSets;
    }

    public void setBuildConfigurationSets(List<BuildConfigurationSetRest> buildConfigurationSets) {
        this.buildConfigurationSets = buildConfigurationSets;
    }

    public List<BuildConfigurationRest> getBuildConfigurations() {
        return buildConfigurations;
    }

    public void setBuildConfigurations(List<BuildConfigurationRest> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    public Integer getCurrentProductMilestoneId() {
        return currentProductMilestoneId;
    }

    public void setCurrentProductMilestoneId(Integer currentProductMilestoneId) {
        this.currentProductMilestoneId = currentProductMilestoneId;
    }

    public ProductVersion toProductVersion(Product product) {
        ProductVersion productVersionToBeUpdated = getProductVersionFromProductOrNewOne(product);
        return toProductVersion(productVersionToBeUpdated);
    }

    public ProductVersion toProductVersion(ProductVersion productVersion) {
        productVersion.setVersion(version);

        if (currentProductMilestoneId != null) {
            List<ProductMilestone> productMilestones = nullableStreamOf(productVersion.getProductMilestones())
                    .filter(productMilestone -> productMilestone.getId().equals(currentProductMilestoneId))
                    .collect(Collectors.toList());

            if (!productMilestones.isEmpty()) {
                productVersion.setCurrentProductMilestone(productMilestones.get(0));
            } else {
                productVersion.setCurrentProductMilestone(null);
            }
        } else {
            productVersion.setCurrentProductMilestone(null);
        }

        return productVersion;
    }

    /**
     * Checks if ProductVersion is present in Product. If it is true - returns it or creates new one otherwise.
     */
    private ProductVersion getProductVersionFromProductOrNewOne(Product product) {
        List<ProductVersion> productVersionsInProduct = nullableStreamOf(product.getProductVersions())
                .filter(productVersion -> productVersion.getId().equals(id)).collect(Collectors.toList());

        if (!productVersionsInProduct.isEmpty()) {
            return productVersionsInProduct.get(0);
        }

        ProductVersion productVersion = new ProductVersion();
        productVersion.setProduct(product);
        product.getProductVersions().add(productVersion);
        return productVersion;
    }

}
