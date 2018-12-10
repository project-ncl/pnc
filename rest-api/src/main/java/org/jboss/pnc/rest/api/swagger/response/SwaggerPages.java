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

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class SwaggerPages {
    public static class ArtifactImportErrorPage extends Page<ArtifactImportError>{};
    public static class ArtifactPage extends Page<Artifact>{};
    public static class BuildConfigurationPage extends Page<BuildConfiguration>{};
    public static class BuildConfigurationRevisionPage extends Page<BuildConfigurationRevision>{};
    public static class BuildEnvironmentPage extends Page<BuildEnvironment>{};
    public static class BuildPage extends Page<Build>{};
    public static class BuildPushResultPage extends Page<BuildPushResult>{};
    public static class DTOEntityPage extends Page<DTOEntity>{};
    public static class GroupBuildPage extends Page<GroupBuild>{};
    public static class GroupConfigPage extends Page<GroupConfiguration>{};
    public static class ProductPage extends Page<Product>{};
    public static class ProductMilestonePage extends Page<ProductMilestone>{};
    public static class ProductMilestoneReleasePage extends Page<ProductMilestoneRelease>{};
    public static class ProductReleasePage extends Page<ProductRelease>{};
    public static class ProductVersionPage extends Page<ProductVersion>{};
    public static class ProjectPage extends Page<Project>{};
    public static class RepositoryConfigurationPage extends Page<RepositoryConfiguration>{};
    public static class TargetRepositoryPage extends Page<TargetRepository>{};
    public static class UserPage extends Page<User>{};
    public static class BPMTaskPage extends Page<BPMTask>{};
}
