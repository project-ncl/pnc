package org.jboss.pnc.mavenrepositorymanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.folo.client.AproxFoloAdminClientModule;
import org.commonjava.aprox.folo.client.AproxFoloContentClientModule;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.HostedRepository;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.commonjava.aprox.promote.client.AproxPromoteClientModule;
import org.jboss.pnc.common.Configuration;
import org.commonjava.aprox.promote.model.PromoteRequest;
import org.commonjava.aprox.promote.model.PromoteResult;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.util.ArtifactPathInfo;
import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.builder.ArtifactBuilder;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;

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
    
    private static final Logger log = Logger.getLogger(RepositoryManagerDriver.class);
    
    public static final String DRIVER_ID = "maven-repo-driver";

    private static final String GROUP_ID_FORMAT = "product+%s+%s";

    private static final String REPO_ID_FORMAT = "build+%s+%s";

    public static final String PUBLIC_GROUP_ID = "public";

    public static final String SHARED_RELEASES_ID = "shared-releases";

    public static final String SHARED_IMPORTS_ID = "shared-imports";

    private Aprox aprox;

    @Deprecated
    public RepositoryManagerDriver() { // workaround for CDI constructor parameter injection bug
    }

    @Inject
    public RepositoryManagerDriver(Configuration<MavenRepoDriverModuleConfig> configuration) {
        MavenRepoDriverModuleConfig config;
        try {
            config = configuration.getModuleConfig(MavenRepoDriverModuleConfig.class);
            String baseUrl = config.getBaseUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            if (!baseUrl.endsWith("/api")) {
                baseUrl += "/api";
            }
            aprox = new Aprox(baseUrl, new AproxFoloAdminClientModule(), new AproxFoloContentClientModule(),
                    new AproxPromoteClientModule()).connect();
        } catch (ConfigurationParseException e) {
            log.error("Cannot read configuration for " + RepositoryManagerDriver.DRIVER_ID + ".", e);
        }
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
    public RepositoryConfiguration createRepository(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet)
            throws RepositoryManagerException {

        try {
            setupGlobalRepos();
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup shared-releases hosted repository: %s", e, e.getMessage());
        }

        ProductVersion pv = buildRecordSet.getProductVersion();

        String productRepoId = String.format(GROUP_ID_FORMAT, safeUrlPart(pv.getProduct().getName()),
                safeUrlPart(pv.getVersion()));
        try {
            setupProductRepos(productRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup product-local hosted repository or repository group: %s", e,
                    e.getMessage());
        }

        // TODO Better way to generate id that doesn't rely on System.currentTimeMillis() but will still be relatively fast.

        String buildRepoId = String.format(REPO_ID_FORMAT, safeUrlPart(buildConfiguration.getProject().getName()),
                System.currentTimeMillis());
        try {
            setupBuildRepos(buildRepoId, productRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup build-local hosted repository or repository group: %s", e,
                    e.getMessage());
        }

        // since we're setting up a group/hosted repo per build, we can pin the tracking ID to the build repo ID.
        String url;

        try {
            url = aprox.module(AproxFoloContentClientModule.class).trackingUrl(buildRepoId, StoreType.group, buildRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module for the artifact tracker: %s", e,
                    e.getMessage());
        }

        return new MavenRepositoryConfiguration(aprox, buildRepoId, productRepoId, new MavenRepositoryConnectionInfo(url));
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

    /**
     * Convenience method for tests.
     */
    protected Aprox getAprox() {
        return aprox;
    }

}
