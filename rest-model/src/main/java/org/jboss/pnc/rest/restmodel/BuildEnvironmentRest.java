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

import io.swagger.annotations.ApiModelProperty;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BuildEnvironment")
public class BuildEnvironmentRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @ApiModelProperty(dataType = "string")
    private String name;

    @ApiModelProperty(dataType = "string")
    private String description;

    @ApiModelProperty(dataType = "string")
    private String systemImageRepositoryUrl;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    @ApiModelProperty(dataType = "string")
    private String systemImageId;

    private Map<String, String> attributes;

    @ApiModelProperty(dataType = "string")
    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private SystemImageType systemImageType;

    @ApiModelProperty(dataType = "boolean")
    private boolean deprecated;

    public BuildEnvironmentRest() {
    }

    public BuildEnvironmentRest(BuildEnvironment buildEnvironment) {
        this.id = buildEnvironment.getId();
        this.name = buildEnvironment.getName();
        this.description = buildEnvironment.getDescription();
        this.systemImageRepositoryUrl = buildEnvironment.getSystemImageRepositoryUrl();
        this.systemImageId = buildEnvironment.getSystemImageId();
        this.attributes = buildEnvironment.getAttributes();
        this.systemImageType = buildEnvironment.getSystemImageType();
        this.deprecated = buildEnvironment.isDeprecated();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public SystemImageType getSystemImageType() {
        return systemImageType;
    }

    public void setSystemImageType(SystemImageType systemImageType) {
        this.systemImageType = systemImageType;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Deprecated
    public String getImageRepositoryUrl() {
        return systemImageRepositoryUrl;
    }

    @Deprecated
    public void setImageRepositoryUrl(String systemImageRepositoryUrl) {
        this.systemImageRepositoryUrl = systemImageRepositoryUrl;
    }

    public String getSystemImageRepositoryUrl() {
        return systemImageRepositoryUrl;
    }

    public void setSystemImageRepositoryUrl(String systemImageRepositoryUrl) {
        this.systemImageRepositoryUrl = systemImageRepositoryUrl;
    }

    public String getSystemImageId() {
        return systemImageId;
    }

    public void setSystemImageId(String systemImageId) {
        this.systemImageId = systemImageId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public BuildEnvironment.Builder toDBEntityBuilder() {
        return BuildEnvironment.Builder.newBuilder()
                .id(this.getId())
                .name(this.getName())
                .description(this.getDescription())
                .systemImageRepositoryUrl(this.getSystemImageRepositoryUrl())
                .systemImageId(this.getSystemImageId())
                .attributes(this.getAttributes())
                .systemImageType(this.getSystemImageType())
                .deprecated(this.isDeprecated());
    }
}
