/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "ProductMilestone")
public class ProductMilestoneRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private String version;

    private Date endDate;

    private Date startingDate;

    private Date plannedEndDate;

    private String downloadUrl;

    private String issueTrackerUrl;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private Integer productVersionId;

    private Integer performedBuildRecordSetId;

    private Integer distributedBuildRecordSetId;

    private Integer productReleaseId;

    public ProductMilestoneRest() {
    }

    public ProductMilestoneRest(Integer id) {
        this.id = id;
    }

    public ProductMilestoneRest(ProductMilestone productMilestone) {
        this.id = productMilestone.getId();
        this.version = productMilestone.getVersion();
        this.endDate = productMilestone.getEndDate();
        this.startingDate = productMilestone.getStartingDate();
        this.plannedEndDate = productMilestone.getPlannedEndDate();
        this.downloadUrl = productMilestone.getDownloadUrl();
        this.issueTrackerUrl = productMilestone.getIssueTrackerUrl();
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public Date getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(Date plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
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

    public ProductMilestone.Builder toDBEntityBuilder() {
        ProductMilestone.Builder builder = ProductMilestone.Builder.newBuilder()
                .id(id)
                .version(this.getVersion())
                .startingDate(this.getStartingDate())
                .endDate(this.getEndDate())
                .plannedEndDate(this.getPlannedEndDate())
                .downloadUrl(this.getDownloadUrl())
                .issueTrackerUrl(this.issueTrackerUrl)
                .productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build());

        performIfNotNull(distributedBuildRecordSetId, () -> builder.distributedBuildRecordSet(BuildRecordSet.Builder.newBuilder().id(distributedBuildRecordSetId).build()));
        performIfNotNull(performedBuildRecordSetId, () -> builder.performedBuildRecordSet(BuildRecordSet.Builder.newBuilder().id(performedBuildRecordSetId).build()));

        return builder;
    }
}
