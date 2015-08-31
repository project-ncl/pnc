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

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "ProductMilestone")
public class ProductMilestoneRest implements GenericRestEntity<Integer> {

    private Integer id;

    private String version;

    private Date releaseDate;

    private Date startingDate;

    private Date plannedReleaseDate;

    private String downloadUrl;

    private Integer productVersionId;

    private Integer performedBuildRecordSetId;

    private Integer distributedBuildRecordSetId;

    private Integer productReleaseId;

    public ProductMilestoneRest() {
    }

    public ProductMilestoneRest(ProductMilestone productMilestone) {
        this.id = productMilestone.getId();
        this.version = productMilestone.getVersion();
        this.releaseDate = productMilestone.getReleaseDate();
        this.startingDate = productMilestone.getStartingDate();
        this.plannedReleaseDate = productMilestone.getPlannedReleaseDate();
        this.downloadUrl = productMilestone.getDownloadUrl();
        this.productVersionId = productMilestone.getProductVersion().getId();
        if (productMilestone.getPerformedBuildRecordSet() != null) {
            this.performedBuildRecordSetId = productMilestone.getPerformedBuildRecordSet().getId();
        }
        if (productMilestone.getDistributedBuildRecordSet() != null) {
            this.distributedBuildRecordSetId = productMilestone.getDistributedBuildRecordSet().getId();
        }
        if (productMilestone.getProductRelease() != null) {
            this.productReleaseId = productMilestone.getProductRelease().getId();
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public Date getPlannedReleaseDate() {
        return plannedReleaseDate;
    }

    public void setPlannedReleaseDate(Date plannedReleaseDate) {
        this.plannedReleaseDate = plannedReleaseDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Integer getProductReleaseId() {
        return productReleaseId;
    }

    public void setProductReleaseId(Integer productReleaseId) {
        this.productReleaseId = productReleaseId;
    }

    public Integer getPerformedBuildRecordSetId() {
        return performedBuildRecordSetId;
    }

    public void setPerformedBuildRecordSetId(Integer performedBuildRecordSetId) {
        this.performedBuildRecordSetId = performedBuildRecordSetId;
    }

    public Integer getDistributedBuildRecordSetId() {
        return distributedBuildRecordSetId;
    }

    public void setDistributedBuildRecordSetId(Integer distributedBuildRecordSetId) {
        this.distributedBuildRecordSetId = distributedBuildRecordSetId;
    }

    /**
     * Create a new ProductMilestone object based on this ProductMilestoneRest
     * 
     * @param productVersion The product version associated with this product milestone
     * @return The new product milestone
     */
    public ProductMilestone toProductMilestone(ProductVersion productVersion) {
        return ProductMilestone.Builder.newBuilder()
                .productVersion(productVersion)
                .id(id)
                .version(version)
                .releaseDate(releaseDate)
                .startingDate(startingDate)
                .plannedReleaseDate(plannedReleaseDate)
                .downloadUrl(downloadUrl)
                .build();
    }

    /**
     * Merge the fields of this product milestone rest with the given product milestone
     * Note: Changing the product version of a product milestone is not allowed.  If the 
     * product version of the given milestone is different than the current milestone, the 
     * change will be ignored.
     * 
     * @param productMilestone
     * @return The product milestone with updated attributes to match this ProductMilestoneRest
     */
    public ProductMilestone toProductMilestone(ProductMilestone productMilestone) {
        productMilestone.setVersion(version);
        productMilestone.setId(id);
        productMilestone.setReleaseDate(releaseDate);
        productMilestone.setStartingDate(startingDate);
        productMilestone.setPlannedReleaseDate(plannedReleaseDate);
        productMilestone.setDownloadUrl(downloadUrl);

        if (performedBuildRecordSetId != null) {
            BuildRecordSet performedBuildRecordSet = BuildRecordSet.Builder.newBuilder().id(performedBuildRecordSetId)
                    .performedInProductMilestone(productMilestone).build();
            productMilestone.setPerformedBuildRecordSet(performedBuildRecordSet);
        }

        if (distributedBuildRecordSetId != null) {
            BuildRecordSet distributedBuildRecordSet = BuildRecordSet.Builder.newBuilder().id(distributedBuildRecordSetId)
                    .distributedInProductMilestone(productMilestone).build();
            productMilestone.setDistributedBuildRecordSet(distributedBuildRecordSet);
        }

        if (productReleaseId != null) {
            ProductRelease productRelease = ProductRelease.Builder.newBuilder().id(productReleaseId)
                    .productMilestone(productMilestone).build();
            productMilestone.setProductRelease(productRelease);
        }

        return productMilestone;
    }

}
