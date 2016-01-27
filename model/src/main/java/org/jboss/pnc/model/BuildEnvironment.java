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
package org.jboss.pnc.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * The BuildSystemImage, selected by the Environment Driver to run a build, based on the buildConfiguration requirements
 *
 * @author avibelli
 */
@Entity
public class BuildEnvironment implements GenericEntity<Integer> {

    private static final long serialVersionUID = 3170247997550146257L;

    public static final String SEQUENCE_NAME = "build_system_image_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @NotNull
    @Column(unique=true)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    /**
     * The URL of the repository which contains the build system images.
     */
    private String imageRepositoryUrl;

    /**
     * A unique identifier representing the system image, for example a Docker container ID.
     * This should never be modified once the db record has been created.
     */
    private String systemImageId;

    @Enumerated(EnumType.STRING)
    private BuildType buildType;

    public BuildEnvironment() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageRepositoryUrl() {
        return imageRepositoryUrl;
    }

    public void setImageRepositoryUrl(String imageRepositoryUrl) {
        this.imageRepositoryUrl = imageRepositoryUrl;
    }

    public String getSystemImageId() {
        return systemImageId;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    @Override
    public String toString() {
        return "SystemImage [name=" + name + "]";
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String imageRepositoryUrl;

        private String systemImageId;

        private BuildType buildType = BuildType.JAVA;

        private Builder() {
            
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildEnvironment build() {
            BuildEnvironment buildSystemImage = new BuildEnvironment();
            buildSystemImage.setId(id);
            buildSystemImage.setName(name);
            buildSystemImage.setDescription(description);
            buildSystemImage.setImageRepositoryUrl(imageRepositoryUrl);
            buildSystemImage.systemImageId = systemImageId;
            buildSystemImage.setBuildType(buildType);
            return buildSystemImage;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imageRepositoryUrl(String imageRepositoryUrl) {
            this.imageRepositoryUrl = imageRepositoryUrl;
            return this;
        }

        public Builder systemImageId(String systemImageId) {
            this.systemImageId = systemImageId;
            return this;
        }

        public Builder buildType(BuildType buildType) {
            this.buildType = buildType;
            return this;
        }

        
    }
}
