package org.jboss.pnc.mavenrepositorymanager;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.client.core.util.UrlUtils;
import org.commonjava.aprox.folo.client.AproxFoloAdminClientModule;
import org.commonjava.aprox.folo.client.AproxFoloContentClientModule;
import org.commonjava.aprox.folo.dto.TrackedContentDTO;
import org.commonjava.aprox.folo.dto.TrackedContentEntryDTO;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.HostedRepository;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.commonjava.maven.atlas.ident.util.ArtifactPathInfo;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of {@link RepositoryManager} that manages an <a href="https://github.com/jdcasey/aprox">AProx</a> instance to
 * support repositories for Maven-ish builds.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 * 
 * @author <a href="mailto:jdcasey@commonjava.org">John Casey</a>
 */
@ApplicationScoped
public class RepositoryManagerDriver implements RepositoryManager {

    private static final String MAVEN_REPOSITORY_CONFIG_SECTION = "maven-repository";

    private static final String BASE_URL_PROPERTY = "base.url";

    private static final String GROUP_ID_FORMAT = "product+%s+%s";

    private static final String REPO_ID_FORMAT = "build+%s+%s";

    private static final String PUBLIC_GROUP_ID = "public";

    private static final String SHARED_RELEASES_ID = "shared-releases";

    private static final String SHARED_IMPORTS_ID = "shared-imports";

    private Aprox aprox;

    @Deprecated
    public RepositoryManagerDriver() { //workaround for CDI constructor parameter injection bug
    }

    @Inject
    public RepositoryManagerDriver(Configuration configuration) {
        Properties properties = configuration.getModuleConfig(MAVEN_REPOSITORY_CONFIG_SECTION);

        String baseUrl = properties.getProperty(BASE_URL_PROPERTY);
        aprox = new Aprox(baseUrl, new AproxFoloAdminClientModule(), new AproxFoloContentClientModule()).connect();
    }

    /**
     * Only supports {@link RepositoryType#MAVEN}.
     */
    @Override
    public boolean canManage(RepositoryType managerType) {
        return (managerType == RepositoryType.MAVEN);
    }

    /**
     * Currently, we're using the AutoProx add-on of AProx. This add-on supports a series of rules (implemented as groovy
     * scripts), which match repository/group naming patterns and create remote repositories, hosted repositories, and groups in
     * flexible configurations on demand. Because these rules will create the necessary repositories the first time they are
     * accessed, this driver only has to formulate a repository URL that will trigger the appropriate AutoProx rule, then pass
     * this back via a {@link MavenRepositoryConfiguration} instance.
     * 
     * @throws RepositoryManagerException In the event one or more repositories or groups can't be created to support the build
     *         (or product, or shared-releases).
     */
    @Override
    public RepositoryConfiguration createRepository(ProjectBuildConfiguration projectBuildConfiguration,
            BuildCollection buildCollection) throws RepositoryManagerException {

        try {
            setupGlobalRepos();
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup shared-releases hosted repository: %s", e, e.getMessage());
        }

        ProductVersion pv = buildCollection.getProductVersion();

        String productRepoId = String.format(GROUP_ID_FORMAT, safeUrlPart(pv.getProduct().getName()),
                safeUrlPart(pv.getVersion()));
        try {
            setupProductRepos(productRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup product-local hosted repository or repository group: %s", e,
                    e.getMessage());
        }

        // TODO Better way to generate id that doesn't rely on System.currentTimeMillis() but will still be relatively fast.

        String buildRepoId = String.format(REPO_ID_FORMAT, safeUrlPart(projectBuildConfiguration.getProject().getName()),
                System.currentTimeMillis());
        try {
            setupBuildRepos(buildRepoId, productRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup build-local hosted repository or repository group: %s", e,
                    e.getMessage());
        }

        // since we're setting up a group/hosted repo per build, we can pin the tracking ID to the build repo ID.
        String url = trackingUrl(buildRepoId);

        // TODO: Use this once we upgrade beyond AProx 0.17.0
        // try {
        // url = aprox.module(AproxFoloContentClientModule.class).trackingUrl(buildRepoId, StoreType.group, buildRepoId);
        // } catch (AproxClientException e) {
        // throw new RepositoryManagerException("Failed to retrieve AProx client module for the artifact tracker: %s", e,
        // e.getMessage());
        // }

        return new MavenRepositoryConfiguration(buildRepoId, new MavenRepositoryConnectionInfo(url));
    }

    // TODO: Remove once we upgrade beyond AProx 0.17.0
    private String trackingUrl(String buildRepoId) {
        return UrlUtils.buildUrl(aprox.getBaseUrl(), "/folo/track", buildRepoId, StoreType.group.singularEndpointName(),
                buildRepoId);
    }

    /**
     * Retrieve tracking report from repository manager. Add each tracked download to the dependencies of the build result. Add
     * each tracked upload to the built artifacts of the build result. Promote uploaded artifacts to the product-level storage.
     * Finally, clear the tracking report, and delete the hosted repository + group associated with the completed build.
     */
    @Override
    // TODO move under returned object (do not use the one from model) form createRepo
    public void persistArtifacts(RepositoryConfiguration repository, ProjectBuildResult buildResult)
            throws RepositoryManagerException {
        String trackingId = repository.getId();
        TrackedContentDTO report;
        try {
            report = aprox.module(AproxFoloAdminClientModule.class).getTrackingReport(trackingId, StoreType.group, trackingId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s. Reason: %s", e, trackingId,
                    e.getMessage());
        }

        List<Artifact> deps = new ArrayList<>();
        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        for (TrackedContentEntryDTO download : downloads) {
            Artifact artifact = new Artifact();
            artifact.setChecksum(download.getSha256());
            artifact.setDeployUrl(download.getOriginUrl());

            String path = download.getPath();
            artifact.setFilename(new File(path).getName());

            ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);
            artifact.setIdentifier(pathInfo.getProjectId().toString());

            artifact.setRepoType(RepositoryType.MAVEN);
            artifact.setStatus(ArtifactStatus.BINARY_IMPORTED);
            deps.add(artifact);
        }

        buildResult.setDependencies(deps);

        List<Artifact> builds = new ArrayList<>();
        Set<TrackedContentEntryDTO> uploads = report.getUploads();
        for (TrackedContentEntryDTO upload : uploads) {
            Artifact artifact = new Artifact();
            artifact.setChecksum(upload.getSha256());
            artifact.setDeployUrl(upload.getLocalUrl());

            String path = upload.getPath();
            artifact.setFilename(new File(path).getName());

            ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);
            artifact.setIdentifier(pathInfo.getProjectId().toString());

            artifact.setRepoType(RepositoryType.MAVEN);
            artifact.setStatus(ArtifactStatus.BINARY_BUILT);
            builds.add(artifact);
        }

        buildResult.setBuiltArtifacts(builds);

        // TODO: Promote deps/downloads/imports (whatever we want to call them) into product-level (global?) imports hosted repo
        // TODO: Promote uploads/build artifacts to product-level hosted repo.

        // clean up.
        try {
            aprox.module(AproxFoloAdminClientModule.class).clearTrackingRecord(trackingId, StoreType.group, trackingId);
            aprox.stores().delete(StoreType.group, trackingId);
            aprox.stores().delete(StoreType.remote, trackingId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException(
                    "Failed to clean up build repositories / tracking information for: %s. Reason: %s", e, trackingId,
                    e.getMessage());
        }
    }

    /**
     * Create the hosted repository and group necessary to support a single build. The hosted repository holds artifacts
     * uploaded from the build, and the group coordinates access to this hosted repository, along with content from the
     * product-level content group with which this build is associated. The group also provides a tracking target, so the
     * repository manager can keep track of downloads and uploads for the build.
     * 
     * @param buildRepoId
     * @param productRepoId
     * @throws AproxClientException
     */
    private void setupBuildRepos(String buildRepoId, String productRepoId) throws AproxClientException {
        // if the build-level group doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, buildRepoId)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            if (!aprox.stores().exists(StoreType.hosted, buildRepoId)) {
                HostedRepository buildArtifacts = new HostedRepository(buildRepoId);
                buildArtifacts.setAllowSnapshots(true);
                buildArtifacts.setAllowReleases(true);

                aprox.stores().create(buildArtifacts, HostedRepository.class);
            }

            Group buildGroup = new Group(buildRepoId);

            // Priorities for build-local group:

            // 1. build-local artifacts
            buildGroup.addConstituent(new StoreKey(StoreType.hosted, buildRepoId));

            // 2. product-level group
            buildGroup.addConstituent(new StoreKey(StoreType.group, productRepoId));

            aprox.stores().create(buildGroup, Group.class);
        }
    }

    /**
     * Lazily create product-level hosted repository and group if they don't exist. The group uses the following content
     * preference order:
     * <ol>
     * <li>product-level hosted repository (artifacts built for this product release)</li>
     * <li>global shared-releases hosted repository (contains artifacts from "released" product versions)</li>
     * <li>global shared-imports hosted repository (contains anything imported for a previous build)</li>
     * <li>the 'public' group, which manages the allowed remote repositories from which imports can be downloaded</li>
     * </ol>
     * 
     * @param productRepoId
     * @throws AproxClientException
     */
    private void setupProductRepos(String productRepoId) throws AproxClientException {
        // if the product-level group doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, productRepoId)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            if (!aprox.stores().exists(StoreType.hosted, productRepoId)) {
                HostedRepository productArtifacts = new HostedRepository(productRepoId);
                productArtifacts.setAllowSnapshots(false);
                productArtifacts.setAllowReleases(true);

                aprox.stores().create(productArtifacts, HostedRepository.class);
            }

            Group productGroup = new Group(productRepoId);

            // Priorities for product-local group:

            // 1. product-local artifacts
            productGroup.addConstituent(new StoreKey(StoreType.hosted, productRepoId));

            // 2. global shared-releases artifacts
            productGroup.addConstituent(new StoreKey(StoreType.hosted, SHARED_RELEASES_ID));

            // 3. global shared-imports artifacts
            productGroup.addConstituent(new StoreKey(StoreType.hosted, SHARED_IMPORTS_ID));

            // 4. public group, containing remote proxies to the outside world
            // TODO: Configuration by product to determine whether outside world access is permitted.
            productGroup.addConstituent(new StoreKey(StoreType.group, PUBLIC_GROUP_ID));

            aprox.stores().create(productGroup, Group.class);
        }
    }

    /**
     * Lazily create the shared-releases and shared-imports global hosted repositories if they don't already exist.
     * 
     * @throws AproxClientException
     */
    private void setupGlobalRepos() throws AproxClientException {
        // if the global shared-releases repository doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.hosted, SHARED_RELEASES_ID)) {
            HostedRepository sharedArtifacts = new HostedRepository(SHARED_RELEASES_ID);
            sharedArtifacts.setAllowSnapshots(false);
            sharedArtifacts.setAllowReleases(true);

            aprox.stores().create(sharedArtifacts, HostedRepository.class);
        }

        // if the global imports repo doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.hosted, SHARED_IMPORTS_ID)) {
            HostedRepository productArtifacts = new HostedRepository(SHARED_IMPORTS_ID);
            productArtifacts.setAllowSnapshots(false);
            productArtifacts.setAllowReleases(true);

            aprox.stores().create(productArtifacts, HostedRepository.class);
        }
    }

    /**
     * Sift out spaces, pipe characters and colons (things that don't play well in URLs) from the project name, and convert them
     * to dashes. This is only for naming repositories, so an approximate match to the project in question is fine.
     */
    private String safeUrlPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }

}
