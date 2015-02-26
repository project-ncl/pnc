/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model.builder;

import java.util.HashSet;
import java.util.Set;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.BuildConfigurationSet;

/**
 * @author avibelli
 *
 */
public class BuildConfigurationSetBuilder {

    private Integer id;

    private String name;

    private ProductVersion productVersion;

    private Set<BuildConfiguration> buildConfigurations = new HashSet<BuildConfiguration>();

    private BuildConfigurationSetBuilder() {

    }

    public BuildConfigurationSet build() {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setId(id);
        buildConfigurationSet.setName(name);
        buildConfigurationSet.setProductVersion(productVersion);
        buildConfigurationSet.setBuildConfigurations(buildConfigurations);
        for (BuildConfiguration buildConfiguration : buildConfigurations) {
            buildConfiguration.addBuildConfigurationSet(buildConfigurationSet);
        }

        return buildConfigurationSet;
    }

    public static BuildConfigurationSetBuilder newBuilder() {
        return new BuildConfigurationSetBuilder();
    }

    public BuildConfigurationSetBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public BuildConfigurationSetBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BuildConfigurationSetBuilder productVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public BuildConfigurationSetBuilder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
        return this;
    }

    public BuildConfigurationSetBuilder buildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfigurations.add(buildConfiguration);
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

}
