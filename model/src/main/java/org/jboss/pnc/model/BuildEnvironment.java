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
package org.jboss.pnc.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.jboss.pnc.enums.SystemImageType;

/**
 * The BuildEnvironment, selected by the Environment Driver to run a build, based on the buildConfiguration requirements
 *
 * @author avibelli
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = { @UniqueConstraint(
                name = "uk_buildenvironment_imageid_imagerepositoryurl",
                columnNames = { "systemImageId", "systemImageRepositoryUrl" }) },
        indexes = { @Index(name = "idx_buildenvironment_systemimageid", columnList = "systemimageid") })
public class BuildEnvironment implements GenericEntity<Integer> {

    private static final long serialVersionUID = 3170247997550146257L;

    public static final String SEQUENCE_NAME = "build_system_image_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @NotNull
    @Size(max = 255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    /**
     * The URL of the repository which contains the build system image.
     */
    @Size(max = 255)
    private String systemImageRepositoryUrl;

    /**
     * A unique identifier such representing the system image, for example a Docker container ID or a checksum of a VM
     * image. This must never be modified to ensure build reproducibility.
     */
    @NotNull
    @Column(unique = true, updatable = false)
    @Size(max = 255)
    private String systemImageId;

    @NotNull
    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private SystemImageType systemImageType;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "build_environment_attributes",
            joinColumns = @JoinColumn(
                    name = "build_environment_id",
                    foreignKey = @ForeignKey(name = "fk_build_environment_attributes_buildenvironment")))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    private Map<String, String> attributes = new HashMap<>();

    @NotNull
    private boolean deprecated = false;

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

    public SystemImageType getSystemImageType() {
        return systemImageType;
    }

    public void setSystemImageType(SystemImageType systemImageType) {
        this.systemImageType = systemImageType;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public String toString() {
        return "Build Environment [name: " + name + ", image id: " + this.systemImageId + "]";
    }

    // Needed for mapstruct to be able to use builders for immutable types (systemImageId)
    public static BuildEnvironment.Builder builder() {
        return Builder.newBuilder();
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String systemImageRepositoryUrl;

        private String systemImageId;

        private Map<String, String> attributes = new HashMap<>();

        private SystemImageType systemImageType;

        private Boolean deprecated = false;

        private Builder() {

        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildEnvironment build() {
            BuildEnvironment buildEnvironment = new BuildEnvironment();
            buildEnvironment.setId(id);
            buildEnvironment.setName(name);
            buildEnvironment.setDescription(description);
            buildEnvironment.setSystemImageRepositoryUrl(systemImageRepositoryUrl);
            buildEnvironment.systemImageId = systemImageId;
            buildEnvironment.setAttributes(attributes);
            buildEnvironment.setSystemImageType(systemImageType);
            buildEnvironment.deprecated = deprecated;
            return buildEnvironment;
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

        public Builder systemImageType(SystemImageType systemImageType) {
            this.systemImageType = systemImageType;
            return this;
        }

        public Builder deprecated(boolean isDeprecated) {
            this.deprecated = isDeprecated;
            return this;
        }

    }
}
