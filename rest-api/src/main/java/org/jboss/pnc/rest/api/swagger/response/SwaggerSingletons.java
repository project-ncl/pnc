package org.jboss.pnc.rest.api.swagger.response;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactImportError;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildEnvironment;
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
import org.jboss.pnc.dto.RepositoryConfiguration;
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

    public static class BuildConfigurationSingleton extends Singleton<BuildConfiguration> {

        public BuildConfigurationSingleton(BuildConfiguration content) {
            super(content);
        }
    };

    public static class BuildConfigurationRevisionSingleton extends Singleton<BuildConfigurationRevision> {

        public BuildConfigurationRevisionSingleton(BuildConfigurationRevision content) {
            super(content);
        }
    };

    public static class BuildEnvironmentSingleton extends Singleton<BuildEnvironment> {

        public BuildEnvironmentSingleton(BuildEnvironment content) {
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

    public static class RepositoryConfigurationSingleton extends Singleton<RepositoryConfiguration> {

        public RepositoryConfigurationSingleton(RepositoryConfiguration content) {
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
