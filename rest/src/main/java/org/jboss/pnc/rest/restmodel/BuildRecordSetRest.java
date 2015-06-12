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

    private Integer performedInProductMilestoneId;

    private Integer distributedInProductMilestoneId;

    private List<Integer> buildRecordIds;

    public BuildRecordSetRest() {
    }

    public BuildRecordSetRest(BuildRecordSet buildRecordSet) {
        this.id = buildRecordSet.getId();
        performIfNotNull(buildRecordSet.getPerformedInProductMilestone() != null, 
                () -> this.performedInProductMilestoneId = buildRecordSet.getPerformedInProductMilestone().getId());
        performIfNotNull(buildRecordSet.getDistributedInProductMilestone() != null, 
                () -> this.distributedInProductMilestoneId = buildRecordSet.getDistributedInProductMilestone().getId());
        this.buildRecordIds = nullableStreamOf(buildRecordSet.getBuildRecords()).map(buildRecord -> buildRecord.getId())
                .collect(Collectors.toList());

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPerformedInProductMilestoneId() {
        return this.performedInProductMilestoneId;
    }

    public void setPerformedInMilestoneId(Integer performedInProductMilestoneId) {
        this.performedInProductMilestoneId = performedInProductMilestoneId;
    }

    public Integer getDistributedInProductMilestoneId() {
        return this.distributedInProductMilestoneId;
    }

    public void setDistributedInProductMilestoneId(Integer distributedInProductMilestoneId) {
        this.distributedInProductMilestoneId = distributedInProductMilestoneId;
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

        performIfNotNull(performedInProductMilestoneId != null,
                () -> builder.performedInProductMilestone(ProductMilestone.Builder.newBuilder().id(performedInProductMilestoneId).build()));
        performIfNotNull(distributedInProductMilestoneId != null,
                () -> builder.distributedInProductMilestone(ProductMilestone.Builder.newBuilder().id(distributedInProductMilestoneId).build()));

        nullableStreamOf(buildRecordIds).forEach(buildRecordId -> {
            BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder().id(buildRecordId);
            builder.buildRecord(buildRecordBuilder.build());
        });

        return builder.build();
    }

}
