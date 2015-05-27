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

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecordSet")
public class BuildRecordSetRest {

    private Integer id;

    private Integer productMilestoneId;

    private Integer productReleaseId;

    private List<Integer> buildRecordIds;

    public BuildRecordSetRest() {
    }

    public BuildRecordSetRest(BuildRecordSet buildRecordSet) {
        this.id = buildRecordSet.getId();
        performIfNotNull(buildRecordSet.getProductMilestone() != null, () -> this.productMilestoneId = buildRecordSet
                .getProductMilestone().getId());
        performIfNotNull(buildRecordSet.getProductRelease() != null, () -> this.productReleaseId = buildRecordSet
                .getProductRelease().getId());
        this.buildRecordIds = nullableStreamOf(buildRecordSet.getBuildRecords()).map(buildRecord -> buildRecord.getId())
                .collect(Collectors.toList());

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductMilestoneId() {
        return productMilestoneId;
    }

    public void setMilestoneId(Integer productMilestoneId) {
        this.productMilestoneId = productMilestoneId;
    }

    public Integer getProductReleaseId() {
        return productReleaseId;
    }

    public void setProductReleaseId(Integer productReleaseId) {
        this.productReleaseId = productReleaseId;
    }

    public List<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }

    public void setBuildRecordIds(List<Integer> buildRecordIds) {
        this.buildRecordIds = buildRecordIds;
    }

    public BuildRecordSet toBuildRecordSet() {
        BuildRecordSet.Builder builder = BuildRecordSet.Builder.newBuilder();
        builder.id(id);

        performIfNotNull(productMilestoneId != null,
                () -> builder.productMilestone(ProductMilestone.Builder.newBuilder().id(productMilestoneId).build()));
        performIfNotNull(productReleaseId != null,
                () -> builder.productRelease(ProductRelease.Builder.newBuilder().id(productReleaseId).build()));

        nullableStreamOf(buildRecordIds).forEach(buildRecordId -> {
            BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder().id(buildRecordId);
            builder.buildRecord(buildRecordBuilder.build());
        });

        return builder.build();
    }

}
