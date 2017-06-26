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

import lombok.ToString;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildConfigurationSet")
@ToString
public class BuildConfigurationSetRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String name;

    private Integer productVersionId;

    private List<Integer> buildConfigurationIds = new LinkedList<>();

    public BuildConfigurationSetRest() {
    }

    public BuildConfigurationSetRest(BuildConfigurationSet buildConfigurationSet) {
        this.id = buildConfigurationSet.getId();
        this.name = buildConfigurationSet.getName();
        performIfNotNull(buildConfigurationSet.getProductVersion(), () ->this.productVersionId = buildConfigurationSet.getProductVersion().getId());

        buildConfigurationSet.getBuildConfigurations().forEach(bc -> buildConfigurationIds.add(bc.getId()));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public List<Integer> getBuildConfigurationIds() {
        return buildConfigurationIds;
    }

    public void setBuildConfigurationIds(List<Integer> buildConfigurationIds) {
        this.buildConfigurationIds = buildConfigurationIds;
    }

    public void addBuildConfiguration(BuildConfigurationRest buildConfigurationRest) {
        buildConfigurationIds.add(buildConfigurationRest.getId());
    }

    public void addBuildConfigurations(Collection<BuildConfigurationRest> buildConfigurationRestCollection) {
        buildConfigurationIds.addAll(buildConfigurationRestCollection.stream().map(BuildConfigurationRest::getId).collect(Collectors.toList()));
    }

    public BuildConfigurationSet.Builder toDBEntityBuilder() {
        BuildConfigurationSet.Builder builder = BuildConfigurationSet.Builder.newBuilder()
                .id(id)
                .name(name);

        performIfNotNull(productVersionId, () -> builder.productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build()));

        nullableStreamOf(buildConfigurationIds).forEach(buildConfigurationId -> {
            BuildConfiguration.Builder buildConfigurationBuilder = BuildConfiguration.Builder.newBuilder().id(buildConfigurationId);
            builder.buildConfiguration(buildConfigurationBuilder.build());
        });
        return builder;
    }

}
