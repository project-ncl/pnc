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

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProjectBuildResult;

/**
 * @author avibelli
 *
 */
public class BuildCollectionBuilder {

    private Integer id;

    private Integer productBuildBumber;

    private ProductMilestone milestone;

    private ProductVersion productVersion;

    private List<ProjectBuildResult> projectBuildResults;

    private BuildCollectionBuilder() {
        projectBuildResults = new ArrayList<>();
    }

    public static BuildCollectionBuilder newBuilder() {
        return new BuildCollectionBuilder();
    }

    public BuildCollection build() {
        BuildCollection buildCollection = new BuildCollection();
        buildCollection.setId(id);
        buildCollection.setProductBuildNumber(productBuildBumber);
        buildCollection.setMilestone(milestone);

        if (productVersion != null) {
            productVersion.addProductBuildCollection(buildCollection);
        }
        buildCollection.setProductVersion(productVersion);

        // Set the bi-directional mapping
        for (ProjectBuildResult projectBuildResult : projectBuildResults) {
            projectBuildResult.getBuildCollections().add(buildCollection);
        }

        buildCollection.setProjectBuildResult(projectBuildResults);

        return buildCollection;
    }

    public BuildCollectionBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public BuildCollectionBuilder productBuildBumber(Integer productBuildBumber) {
        this.productBuildBumber = productBuildBumber;
        return this;
    }

    public BuildCollectionBuilder productVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public BuildCollectionBuilder milestone(ProductMilestone milestone) {
        this.milestone = milestone;
        return this;
    }

    public BuildCollectionBuilder projectBuildResult(ProjectBuildResult projectBuildResult) {
        this.projectBuildResults.add(projectBuildResult);
        return this;
    }

    public BuildCollectionBuilder projectBuildResults(List<ProjectBuildResult> projectBuildResult) {
        this.projectBuildResults = projectBuildResult;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public Integer getProductBuildBumber() {
        return productBuildBumber;
    }

    public ProductMilestone getMilestone() {
        return milestone;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public List<ProjectBuildResult> getProjectBuildResults() {
        return projectBuildResults;
    }

}
