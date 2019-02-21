/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.api.swagger.response;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactImportError;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRelease;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.internal.bpm.BPMTask;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Singleton;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SwaggerSingletons {

    public static class ArtifactImportErrorSingleton extends Singleton<ArtifactImportError> {

        public ArtifactImportErrorSingleton(ArtifactImportError content) {
            super(content);
        }
    };

    public static class ArtifactSingleton extends Singleton<Artifact> {

        public ArtifactSingleton(Artifact content) {
            super(content);
        }
    };

    public static class BuildConfigSingleton extends Singleton<BuildConfiguration> {

        public BuildConfigSingleton(BuildConfiguration content) {
            super(content);
        }
    };

    public static class BuildConfigRevisionSingleton extends Singleton<BuildConfigurationRevision> {

        public BuildConfigRevisionSingleton(BuildConfigurationRevision content) {
            super(content);
        }
    };

    public static class BuildEnvironmentSingleton extends Singleton<Environment> {

        public BuildEnvironmentSingleton(Environment content) {
            super(content);
        }
    };

    public static class BuildSingleton extends Singleton<Build> {

        public BuildSingleton(Build content) {
            super(content);
        }
    };

    public static class BuildPushResultSingleton extends Singleton<BuildPushResult> {

        public BuildPushResultSingleton(BuildPushResult content) {
            super(content);
        }
    };

    public static class DTOEntitySingleton extends Singleton<DTOEntity> {

        public DTOEntitySingleton(DTOEntity content) {
            super(content);
        }
    };

    public static class GroupBuildSingleton extends Singleton<GroupBuild> {

        public GroupBuildSingleton(GroupBuild content) {
            super(content);
        }
    };

    public static class GroupConfigSingleton extends Singleton<GroupConfiguration> {

        public GroupConfigSingleton(GroupConfiguration content) {
            super(content);
        }
    };

    public static class ProductSingleton extends Singleton<Product> {

        public ProductSingleton(Product content) {
            super(content);
        }
    };

    public static class ProductMilestoneSingleton extends Singleton<ProductMilestone> {

        public ProductMilestoneSingleton(ProductMilestone content) {
            super(content);
        }
    };

    public static class ProductMilestoneReleaseSingleton extends Singleton<ProductMilestoneRelease> {

        public ProductMilestoneReleaseSingleton(ProductMilestoneRelease content) {
            super(content);
        }
    };

    public static class ProductReleaseSingleton extends Singleton<ProductRelease> {

        public ProductReleaseSingleton(ProductRelease content) {
            super(content);
        }
    };

    public static class ProductVersionSingleton extends Singleton<ProductVersion> {

        public ProductVersionSingleton(ProductVersion content) {
            super(content);
        }
    };

    public static class ProjectSingleton extends Singleton<Project> {

        public ProjectSingleton(Project content) {
            super(content);
        }
    };

    public static class SCMRepositorySingleton extends Singleton<SCMRepository> {

        public SCMRepositorySingleton(SCMRepository content) {
            super(content);
        }
    };

    public static class TargetRepositorySingleton extends Singleton<TargetRepository> {

        public TargetRepositorySingleton(TargetRepository content) {
            super(content);
        }
    };

    public static class UserSingleton extends Singleton<User> {

        public UserSingleton(User content) {
            super(content);
        }
    };

    public static class BPMTaskSingleton extends Singleton<BPMTask> {

        public BPMTaskSingleton(BPMTask content) {
            super(content);
        }
    };
}
