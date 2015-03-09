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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import java.io.Serializable;

/**
 * Defines the relationship between the Product and the Project, for a specific version
 * <p>
 * I.e. Product_1 may be mapped to Project_A and Project_B for version 1.0, but mapped only to Project_B for version 1.1
 *
 * @author avibelli
 */
@Entity
public class ProductVersionProject implements Serializable {

    private static final long serialVersionUID = 2596901834161647987L;

    @Id
    @SequenceGenerator(name="product_version_project_id_seq", sequenceName="product_version_project_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="product_version_project_id_seq")
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    private ProductVersion productVersion;

    @ManyToOne
    private Project project;

    public ProductVersionProject() {
    }

    /**
     * @param productVersion
     * @param project
     */
    public ProductVersionProject(ProductVersion productVersion, Project project) {
        this.productVersion = productVersion;
        this.project = project;
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
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    public static class Builder {

        private Integer id;

        private ProductVersion productVersion;

        private Project project;

        private Builder() {

        }

        public ProductVersionProject build() {
            ProductVersionProject productVersionProject = new ProductVersionProject();
            productVersionProject.setId(id);
            productVersionProject.setProductVersion(productVersion);

            if (project != null) {
                project.addProductVersionProject(productVersionProject);
            }
            productVersionProject.setProject(project);

            return productVersionProject;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

    }

}
