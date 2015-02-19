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

package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * Defines the relationship between the Product and the Project, for a specific version
 * 
 * I.e. Product_1 may be mapped to Project_A and Project_B for version 1.0, but mapped only to Project_B for version 1.1
 * 
 * @author avibelli
 *
 */
@Entity
public class BuildConfigurationSet implements Serializable {

    private static final long serialVersionUID = 2596901834161647987L;

    public static final String DEFAULT_SORTING_FIELD = "id";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @ManyToOne
    private ProductVersion productVersion;

    @ManyToMany
    @JoinTable(
            name="build_configuration_set_map",
            joinColumns={@JoinColumn(name="build_configuration_set_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="build_configuration_id", referencedColumnName="id")})
    private Set<BuildConfiguration> buildConfigurations;

    public BuildConfigurationSet() {
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getName() {
        return name;
    }

    /**
     * @param id the id to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the productVersion
     */
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion the productVersion to set
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * @return the buildConfigurations set
     */
    public Set<BuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

    /**
     * @param buildConfigurations the buildConfigurations to set
     */
    public void setBuildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    /**
     * @param buildConfiguration the buildConfiguration to add to the set
     */
    public void addBuildConfigurations(BuildConfiguration buildConfiguration) {
        this.buildConfigurations.add(buildConfiguration);
    }

}
