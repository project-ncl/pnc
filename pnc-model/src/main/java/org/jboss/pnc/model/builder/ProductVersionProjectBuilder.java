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

import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersionProject;
import org.jboss.pnc.model.Project;

/**
 * @author avibelli
 *
 */
public class ProductVersionProjectBuilder {

    private Integer id;

    private ProductVersion productVersion;

    private Project project;

    private ProductVersionProjectBuilder() {

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

    public static ProductVersionProjectBuilder newBuilder() {
        return new ProductVersionProjectBuilder();
    }

    public ProductVersionProjectBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProductVersionProjectBuilder productVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public ProductVersionProjectBuilder project(Project project) {
        this.project = project;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public Project getProject() {
        return project;
    }

}
