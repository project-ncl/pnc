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
package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "ProductMilestone")
public class ProductMilestoneRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    /**
     * Version check to be correspond with the DB pattern in ProductMilestone.version. Version examples: 1.2.3.ER1,
     * 1.2.10.CR1, 1.2.CD1
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @Pattern(
            groups = { WhenCreatingNew.class, WhenUpdating.class },
            regexp = Patterns.PRODUCT_MILESTONE_VERSION,
            message = "Version doesn't match the required pattern " + Patterns.PRODUCT_MILESTONE_VERSION)
    private String version;

    private Date endDate;

    private Date startingDate;

    private Date plannedEndDate;

    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private Integer productVersionId;

    private Set<Integer> performedBuilds;

    private Set<Integer> distributedArtifactIds;

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
        this.productVersionId = productMilestone.getProductVersion().getId();
        this.performedBuilds = nullableStreamOf(productMilestone.getPerformedBuilds()).map(BuildRecord::getId)
                .collect(Collectors.toSet());
        this.distributedArtifactIds = nullableStreamOf(productMilestone.getDistributedArtifacts()).map(Artifact::getId)
                .collect(Collectors.toSet());
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

    public Set<Integer> getPerformedBuilds() {
        return performedBuilds;
    }

    public void setPerformedBuildRecordSetId(Set<Integer> performedBuilds) {
        this.performedBuilds = performedBuilds;
    }

    public Set<Integer> getDistributedArtifactIds() {
        return distributedArtifactIds;
    }

    public void setDistributedBuildRecordSetId(Set<Integer> distributedArtifactIds) {
        this.distributedArtifactIds = distributedArtifactIds;
    }

    public ProductMilestone.Builder toDBEntityBuilder() {
        ProductMilestone.Builder builder = ProductMilestone.Builder.newBuilder()
                .id(id)
                .version(this.getVersion())
                .startingDate(this.getStartingDate())
                .endDate(this.getEndDate())
                .plannedEndDate(this.getPlannedEndDate())
                .productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build());

        nullableStreamOf(this.getDistributedArtifactIds()).forEach(artifactId -> {
            Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder().id(artifactId);
            builder.distributedArtifact(artifactBuilder.build());
        });
        nullableStreamOf(this.getPerformedBuilds()).forEach(buildRecordId -> {
            BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder().id(buildRecordId);
            builder.performedBuild(buildRecordBuilder.build());
        });

        return builder;
    }
}
