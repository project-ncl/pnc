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
package org.jboss.pnc.model;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.Map;

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
     * The URL of the repository which contains the build system image.
     */
    private String systemImageRepositoryUrl;

    /**
     * A unique identifier representing the system image, for example a Docker container ID.
     * This should never be modified once the db record has been created.
     */
    @NotNull
    @Column(unique=true, updatable=false)
    @Index(name="idx_buildenvironment_systemimageid")
    private String systemImageId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="build_environment_attributes", joinColumns=@JoinColumn(name="build_environment_id"))
    @MapKeyColumn(name="name")
    @Column(name="value")
    private Map<String, String> attributes = new HashMap<String, String>();

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

    public String getSystemImageRepositoryUrl() {
        return systemImageRepositoryUrl;
    }

    public void setSystemImageRepositoryUrl(String systemImageRepositoryUrl) {
        this.systemImageRepositoryUrl = systemImageRepositoryUrl;
    }

    public String getSystemImageId() {
        return systemImageId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public String putAttribute(String key, String value) {
        return attributes.put(key, value);
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

        private String systemImageRepositoryUrl;

        private String systemImageId;

        private Map<String, String> attributes = new HashMap<>();

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
            buildSystemImage.setSystemImageRepositoryUrl(systemImageRepositoryUrl);
            buildSystemImage.systemImageId = systemImageId;
            buildSystemImage.setAttributes(attributes);
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

        public Builder systemImageRepositoryUrl(String systemImageRepositoryUrl) {
            this.systemImageRepositoryUrl = systemImageRepositoryUrl;
            return this;
        }

        public Builder systemImageId(String systemImageId) {
            this.systemImageId = systemImageId;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder buildType(BuildType buildType) {
            this.buildType = buildType;
            return this;
        }

        
    }
}
