package org.jboss.pnc.mavenrepositorymanager;

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
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

import javax.annotation.PreDestroy;
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

    public static final String DRIVER_ID = "maven-repo-driver";

    public static final String PUBLIC_GROUP_ID = "public";

    public static final String SHARED_RELEASES_ID = "shared-releases";

    public static final String SHARED_IMPORTS_ID = "shared-imports";

    private Aprox aprox;

    @Deprecated
    public RepositoryManagerDriver() { // workaround for CDI constructor parameter injection bug
    }

    @SuppressWarnings("resource")
    @Inject
    public RepositoryManagerDriver(Configuration configuration) {
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

            setupGlobalRepos();

        } catch (ConfigurationParseException e) {
            throw new IllegalStateException("Cannot read configuration for " + RepositoryManagerDriver.DRIVER_ID + ".", e);
        } catch (AproxClientException e) {
            throw new IllegalStateException("Failed to setup shared-releases or shared-imports hosted repository: "
                    + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        aprox.close();
    }

    /**
     * Only supports {@link RepositoryType#MAVEN}.
     */
    @Override
    public boolean canManage(RepositoryType managerType) {
        return (managerType == RepositoryType.MAVEN);
    }

    /**
     * Use the AProx client API to setup global and build-set level repos and groups, then setup the repo/group needed for this
     * build. Calculate the URL to use for resolving artifacts using the AProx Folo API (Folo is an artifact activity-tracker).
     * Return a new session ({@link MavenRepositorySession}) containing this information.
     * 
     * @throws RepositoryManagerException In the event one or more repositories or groups can't be created to support the build
     *         (or product, or shared-releases).
     */
    @Override
    public RepositorySession createBuildRepository(BuildExecution buildExecution) throws RepositoryManagerException {

        String topId = buildExecution.getTopContentId();
        if (topId != null) {
            try {
                setupProductRepos(topId);
            } catch (AproxClientException e) {
                throw new RepositoryManagerException("Failed to setup product-level repository group: %s", e, e.getMessage());
            }
        }

        String setId = buildExecution.getBuildSetContentId();
        if (setId != null) {
            try {
                setupBuildSetRepos(setId);
            } catch (AproxClientException e) {
                throw new RepositoryManagerException("Failed to setup repository group for build configuration set: %s", e,
                        e.getMessage());
            }
        }

        String buildId = buildExecution.getBuildContentId();
        try {
            setupBuildRepos(buildId, setId, topId, buildExecution.getProjectName());
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to setup repository or repository group for this build: %s", e,
                    e.getMessage());
        }

        // since we're setting up a group/hosted repo per build, we can pin the tracking ID to the build repo ID.
        String url;

        try {
            url = aprox.module(AproxFoloContentClientModule.class).trackingUrl(buildId, StoreType.group, buildId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module for the artifact tracker: %s", e,
                    e.getMessage());
        }

        return new MavenRepositorySession(aprox, buildId, setId, buildExecution.getBuildExecutionType(),
                new MavenRepositoryConnectionInfo(url));
    }

    /**
     * Create the hosted repository and group necessary to support a single build. The hosted repository holds artifacts
     * uploaded from the build, and the group coordinates access to this hosted repository, along with content from the
     * product-level content group with which this build is associated. The group also provides a tracking target, so the
     * repository manager can keep track of downloads and uploads for the build.
     * 
     * @param buildRepoId
     * @param productRepoId
     * @param string
     * @throws AproxClientException
     */
    private void setupBuildRepos(String buildRepoId, String buildSetRepoId, String productRepoId, String projectName)
            throws AproxClientException {
        // if the build-level group doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, buildRepoId)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            if (!aprox.stores().exists(StoreType.hosted, buildRepoId)) {
                HostedRepository buildArtifacts = new HostedRepository(buildRepoId);
                buildArtifacts.setAllowSnapshots(true);
                buildArtifacts.setAllowReleases(true);

                aprox.stores().create(buildArtifacts,
                        "Creating hosted repository for build: " + buildRepoId + " of: " + projectName, HostedRepository.class);
            }

            Group buildGroup = new Group(buildRepoId);

            // build-local artifacts
            buildGroup.addConstituent(new StoreKey(StoreType.hosted, buildRepoId));

            if (buildSetRepoId != null) {
                // build-set-level group
                buildGroup.addConstituent(new StoreKey(StoreType.group, buildSetRepoId));
            }

            if (productRepoId != null) {
                // product-level group
                buildGroup.addConstituent(new StoreKey(StoreType.group, productRepoId));
            }

            // Global-level repos, for captured/shared artifacts and access to the outside world
            addGlobalConstituents(buildGroup);

            aprox.stores().create(buildGroup,
                    "Creating repository group for resolving artifacts in build: " + buildRepoId + " of: " + projectName,
                    Group.class);
        }
    }

    /**
     * Lazily create group related to a build set if it doesn't exist. The group will contain repositories for any builds that
     * have been promoted to it, to allow other related builds to access their artifacts.
     * 
     * @param setId
     * @throws AproxClientException
     */
    private void setupBuildSetRepos(String setId) throws AproxClientException {

        // if the product-level group doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, setId)) {
            Group setGroup = new Group(setId);

            aprox.stores().create(setGroup,
                    "Creating group: " + setId + " for access to repos of builds related to build configuration set.",
                    Group.class);
        }
    }

    /**
     * Lazily create group related to a build set if it doesn't exist. The group will contain repositories for any builds that
     * have been promoted to it, to allow other related builds to access their artifacts.
     * 
     * @param setId
     * @throws AproxClientException
     */
    private void setupProductRepos(String productId) throws AproxClientException {

        // if the product-level group doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, productId)) {
            Group productGroup = new Group(productId);

            aprox.stores().create(productGroup,
                    "Creating group: " + productId + " for access to repos of builds related to that product.", Group.class);
        }
    }

    /**
     * Add the constituents that every build repository group should contain:
     * <ol>
     * <li>shared-releases (Group)</li>
     * <li>shared-imports (Hosted Repo)</li>
     * <li>public (Group)</li>
     * </ol>
     */
    private void addGlobalConstituents(Group group) {
        // 1. global shared-releases artifacts
        group.addConstituent(new StoreKey(StoreType.group, SHARED_RELEASES_ID));

        // 2. global shared-imports artifacts
        group.addConstituent(new StoreKey(StoreType.hosted, SHARED_IMPORTS_ID));

        // 3. public group, containing remote proxies to the outside world
        group.addConstituent(new StoreKey(StoreType.group, PUBLIC_GROUP_ID));
    }

    /**
     * Lazily create the shared-releases and shared-imports global hosted repositories if they don't already exist.
     * 
     * @throws AproxClientException
     */
    private void setupGlobalRepos() throws AproxClientException {
        // if the global shared-releases repository doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.group, SHARED_RELEASES_ID)) {
            Group sharedArtifacts = new Group(SHARED_RELEASES_ID);

            aprox.stores().create(sharedArtifacts, "Creating global shared-builds repository group.", Group.class);
        }

        // if the global imports repo doesn't exist, create it.
        if (!aprox.stores().exists(StoreType.hosted, SHARED_IMPORTS_ID)) {
            HostedRepository sharedImports = new HostedRepository(SHARED_IMPORTS_ID);
            sharedImports.setAllowSnapshots(false);
            sharedImports.setAllowReleases(true);

            aprox.stores().create(sharedImports, "Creating global repository for hosting external imports used in builds.",
                    HostedRepository.class);
        }
    }

    /**
     * Convenience method for tests.
     */
    protected Aprox getAprox() {
        return aprox;
    }

    /**
     * Promote the hosted repository associated with a given project build to an arbitrary repository group.
     * 
     * @return The promotion instance, which won't actually trigger promotion until its
     *         {@link RunningRepositoryPromotion#monitor(java.util.function.Consumer, java.util.function.Consumer)} method is
     *         called.
     */
    @Override
    public RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup) throws RepositoryManagerException {

        return new MavenRunningPromotion(StoreType.hosted, buildRecord.getBuildContentId(), toGroup, aprox);
    }

    /**
     * Promote the repository group associated with a given set of project builds to an arbitrary repository group. This allows
     * handling a chain build's output as a single unit.
     * 
     * @return The promotion instance, which won't actually trigger promotion until its
     *         {@link RunningRepositoryPromotion#monitor(java.util.function.Consumer, java.util.function.Consumer)} method is
     *         called.
     */
    @Override
    public RunningRepositoryPromotion promoteBuildSet(BuildRecordSet buildRecordSet, String toGroup)
            throws RepositoryManagerException {

        return new MavenRunningPromotion(StoreType.group, buildRecordSet.getBuildSetContentId(), toGroup, aprox);
    }

}
