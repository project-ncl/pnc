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
import org.jboss.pnc.dto.response.Singleton;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SwaggerSingletons {
    public static class ArtifactImportErrorSingleton extends Singleton<ArtifactImportError>{};
    public static class ArtifactSingleton extends Singleton<Artifact>{};
    public static class BuildConfigurationSingleton extends Singleton<BuildConfiguration>{};
    public static class BuildConfigurationRevisionSingleton extends Singleton<BuildConfigurationRevision>{};
    public static class BuildEnvironmentSingleton extends Singleton<BuildEnvironment>{};
    public static class BuildSingleton extends Singleton<Build>{};
    public static class BuildPushResultSingleton extends Singleton<BuildPushResult>{};
    public static class DTOEntitySingleton extends Singleton<DTOEntity>{};
    public static class GroupBuildSingleton extends Singleton<GroupBuild>{};
    public static class GroupConfigSingleton extends Singleton<GroupConfiguration>{};
    public static class ProductSingleton extends Singleton<Product>{};
    public static class ProductMilestoneSingleton extends Singleton<ProductMilestone>{};
    public static class ProductMilestoneReleaseSingleton extends Singleton<ProductMilestoneRelease>{};
    public static class ProductReleaseSingleton extends Singleton<ProductRelease>{};
    public static class ProductVersionSingleton extends Singleton<ProductVersion>{};
    public static class ProjectSingleton extends Singleton<Project>{};
    public static class RepositoryConfigurationSingleton extends Singleton<RepositoryConfiguration>{};
    public static class TargetRepositorySingleton extends Singleton<TargetRepository>{};
    public static class UserSingleton extends Singleton<User>{};
}
