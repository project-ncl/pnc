/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mavenrepositorymanager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientHttp;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.core.auth.IndyClientAuthenticator;
import org.commonjava.indy.client.core.auth.OAuth20BearerTokenAuthenticator;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.folo.client.IndyFoloContentClientModule;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.dto.StoreListingDTO;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.DRIVER_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.PUBLIC_GROUP_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.SHARED_IMPORTS_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.TEMPORARY_BUILDS_GROUP;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.UNTESTED_BUILDS_GROUP;

/**
 * Implementation of {@link RepositoryManager} that manages an <a href="https://github.com/jdcasey/indy">Indy</a> instance to
 * support repositories for Maven-ish builds.
 * <p>
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 *
 * @author <a href="mailto:jdcasey@commonjava.org">John Casey</a>
 */
@ApplicationScoped
public class RepositoryManagerDriver implements RepositoryManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int DEFAULT_REQUEST_TIMEOUT;

    private final String BUILD_PROMOTION_GROUP;

    private final String TEMP_BUILD_PROMOTION_GROUP;

    private String baseUrl;
    private Map<String, Indy> indyMap = new HashMap<>();

    private List<String> internalRepoPatterns;

    private Set<String> ignoredPathSuffixes;

    @Deprecated
    public RepositoryManagerDriver() { // workaround for CDI constructor parameter injection bug
        this.DEFAULT_REQUEST_TIMEOUT = 0;
        this.BUILD_PROMOTION_GROUP = "";
        this.TEMP_BUILD_PROMOTION_GROUP = "";
    }

    @Inject
    public RepositoryManagerDriver(Configuration configuration) {
        MavenRepoDriverModuleConfig config;
        try {
            config = configuration
                    .getModuleConfig(new PncConfigProvider<>(MavenRepoDriverModuleConfig.class));
        } catch (ConfigurationParseException e) {
            throw new IllegalStateException("Cannot read configuration for " + DRIVER_ID + ".", e);
        }
        this.DEFAULT_REQUEST_TIMEOUT = config.getDefaultRequestTimeout();
        this.BUILD_PROMOTION_GROUP = config.getBuildPromotionGroup();
        this.TEMP_BUILD_PROMOTION_GROUP = config.getTempBuildPromotionGroup();

        baseUrl = StringUtils.stripEnd(config.getBaseUrl(), "/");
        if (!baseUrl.endsWith("/api")) {
            baseUrl += "/api";
        }

        internalRepoPatterns = new ArrayList<>();
        internalRepoPatterns.add(MavenRepositoryConstants.SHARED_IMPORTS_ID);

        List<String> extraInternalRepoPatterns = config.getInternalRepoPatterns();
        if (extraInternalRepoPatterns != null) {
            internalRepoPatterns.addAll(extraInternalRepoPatterns);
        }

        List<String> ignoredPathSuffixes = config.getIgnoredPathSuffixes();
        if (ignoredPathSuffixes == null) {
            this.ignoredPathSuffixes = Collections.emptySet();
        } else {
            this.ignoredPathSuffixes = new HashSet<>(ignoredPathSuffixes);
        }
    }

    private Indy init(String accessToken) {
        Indy indy = indyMap.get(accessToken);
        if (indy == null) { //TODO use indyConcurrentMap.computeIfAbsent
            IndyClientAuthenticator authenticator = null;
            if (accessToken != null) {
                authenticator = new OAuth20BearerTokenAuthenticator(accessToken);
            }
            try {
                SiteConfig siteConfig = new SiteConfigBuilder("indy", baseUrl)
                        .withRequestTimeoutSeconds(DEFAULT_REQUEST_TIMEOUT)
                        .withMaxConnections(IndyClientHttp.GLOBAL_MAX_CONNECTIONS)
                        .build();

                IndyClientModule[] modules = new IndyClientModule[] {
                        new IndyFoloAdminClientModule(),
                        new IndyFoloContentClientModule(),
                        new IndyPromoteClientModule() };

                indy = new Indy(siteConfig, authenticator, new IndyObjectMapper(true), modules);

                indyMap.put(accessToken, indy);
            } catch (IndyClientException e) {
                throw new IllegalStateException("Failed to create Indy client: " + e.getMessage(), e);
            }
        }
        return indy;
    }

    @Override
    public void close(String accessToken) {
        if (indyMap.containsKey(accessToken)) {
            IOUtils.closeQuietly(indyMap.get(accessToken));
            indyMap.remove(accessToken);
        }
    }

    /**
     * Only supports {@link TargetRepository.Type#MAVEN}.
     */
    @Override
    public boolean canManage(TargetRepository.Type managerType) {
        return (managerType == TargetRepository.Type.MAVEN);
    }

    /**
     * Use the Indy client API to setup global and build-set level repos and groups, then setup the repo/group needed for this
     * build. Calculate the URL to use for resolving artifacts using the AProx Folo API (Folo is an artifact activity-tracker).
     * Return a new session ({@link MavenRepositorySession}) containing this information.
     *
     * @throws RepositoryManagerException In the event one or more repositories or groups can't be created to support the build
     *                                    (or product, or shared-releases).
     */
    @Override
    public RepositorySession createBuildRepository(BuildExecution buildExecution, String accessToken)
            throws RepositoryManagerException {
        Indy indy = init(accessToken);

        String buildId = buildExecution.getBuildContentId();
        try {
            setupBuildRepos(buildExecution, indy);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to setup repository or repository group for this build: %s", e,
                    e.getMessage());
        }

        // since we're setting up a group/hosted repo per build, we can pin the tracking ID to the build repo ID.
        String url;
        String deployUrl;

        try {
            // manually initialize the tracking record, just in case (somehow) nothing gets downloaded/uploaded.
            indy.module(IndyFoloAdminClientModule.class).initReport(buildId);

            StoreKey groupKey = new StoreKey(MAVEN_PKG_KEY, StoreType.group, buildId);
            url = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, groupKey);

            StoreKey hostedKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildId);
            deployUrl = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, hostedKey);

            logger.info("Using '{}' for Maven repository access in build: {}", url, buildId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module for the artifact tracker: %s", e,
                    e.getMessage());
        }

        boolean tempBuild = buildExecution.isTempBuild();
        String buildPromotionGroup = tempBuild ? TEMP_BUILD_PROMOTION_GROUP : BUILD_PROMOTION_GROUP;
        return new MavenRepositorySession(indy, buildId, new MavenRepositoryConnectionInfo(url, deployUrl),
                internalRepoPatterns, ignoredPathSuffixes, buildPromotionGroup, tempBuild);
    }

    /**
     * Create the hosted repository and group necessary to support a single build. The hosted repository holds artifacts
     * uploaded from the build, and the group coordinates access to this hosted repository, along with content from the
     * product-level content group with which this build is associated. The group also provides a tracking target, so the
     * repository manager can keep track of downloads and uploads for the build.
     *
     * @param execution The execution object, which contains the content id for creating the repo, and the build id.
     * @throws IndyClientException
     */
    private void setupBuildRepos(BuildExecution execution, Indy indy)
            throws IndyClientException {

        String buildContentId = execution.getBuildContentId();
        int id = execution.getId();

        // if the build-level group doesn't exist, create it.
        StoreKey groupKey = new StoreKey(MAVEN_PKG_KEY, StoreType.group, buildContentId);

        if (!indy.stores().exists(groupKey)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            StoreKey hostedKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildContentId);
            boolean tempBuild = execution.isTempBuild();
            if (!indy.stores().exists(hostedKey)) {
                HostedRepository buildArtifacts = new HostedRepository(MAVEN_PKG_KEY, buildContentId);
                buildArtifacts.setAllowSnapshots(tempBuild);
                buildArtifacts.setAllowReleases(true);

                buildArtifacts.setDescription(String.format("Build output for PNC build #%s", id));

                indy.stores().create(buildArtifacts,
                        "Creating hosted repository for build: " + id + " (repo: " + buildContentId + ")", HostedRepository.class);
            }

            Group buildGroup = new Group(MAVEN_PKG_KEY, buildContentId);
            String adjective = tempBuild ? "temporary " : "";
            buildGroup.setDescription(String.format("Aggregation group for PNC %sbuild #%s", adjective, id));

            // build-local artifacts
            buildGroup.addConstituent(hostedKey);

            // Global-level repos, for captured/shared artifacts and access to the outside world
            addGlobalConstituents(buildGroup, tempBuild);

            // add extra repositories removed from poms by the adjust process
            addExtraConstituents(execution.getArtifactRepositories(), id, buildContentId, indy, buildGroup);

            indy.stores().create(buildGroup, "Creating repository group for resolving artifacts in build: " + id
                    + " (repo: " + buildContentId + ")", Group.class);
        }
    }

    /**
     * Adds extra remote repositories to the build group that are requested for the particular build. For a Maven build
     * these are repositories defined in the root pom removed by PME by the adjust process.
     *
     * @param repositories
     *            the list of repositories to be added
     * @param buildId
     *            build ID
     * @param buildContentId
     *            build content ID
     * @param indy
     *            Indy client
     * @param buildGroup
     *            target build group in which the repositories are added
     * @throws IndyClientException
     *             in case of an issue when communicating with the repository manager
     */
    private void addExtraConstituents(List<ArtifactRepository> repositories, int buildId, String buildContentId, Indy indy,
            Group buildGroup) throws IndyClientException {
        if (repositories != null && !repositories.isEmpty()) {
            StoreListingDTO<RemoteRepository> existingRepos = indy.stores().listRemoteRepositories(MAVEN_PKG_KEY);
            for (ArtifactRepository repository : repositories) {
                StoreKey remoteKey = null;
                for (RemoteRepository existingRepo : existingRepos) {
                    if (existingRepo.getUrl().equals(repository.getUrl())) {
                        remoteKey = existingRepo.getKey();
                        break;
                    }
                }

                if (remoteKey == null) {
                    // this is basically an implied repo, so using the same prefix "i-"
                    String remoteName = "i-" + repository.getId();

                    // find a free repository ID for the newly created repo
                    remoteKey = new StoreKey(MAVEN_PKG_KEY, StoreType.remote, remoteName);
                    int i = 2;
                    while (indy.stores().exists(remoteKey)) {
                        remoteKey = new StoreKey(MAVEN_PKG_KEY, StoreType.remote, remoteName + "-" + i++);
                    }

                    RemoteRepository remoteRepo = new RemoteRepository(MAVEN_PKG_KEY, remoteKey.getName(), repository.getUrl());
                    remoteRepo.setAllowReleases(repository.getReleases());
                    remoteRepo.setAllowSnapshots(repository.getSnapshots());
                    remoteRepo.setDescription("Implicitly created repo for: " + repository.getName() + " ("
                            + repository.getId() + ") from repository declaration removed by PME in build "
                            + buildId + " (repo: " + buildContentId + ")");
                    indy.stores().create(remoteRepo, "Creating extra remote repository " + repository.getName() + " ("
                            + repository.getId() + ") for build: " + buildId + " (repo: " + buildContentId + ")",
                            RemoteRepository.class);
                }

                buildGroup.addConstituent(remoteKey);
            }
        }
    }

    /**
     * Add the constituents that every build repository group should contain:
     * <ol>
     * <li>builds-untested (Group)</li>
     * <li>for temporary builds add also temporary-builds (Group)</li>
     * <li>shared-imports (Hosted Repo)</li>
     * <li>public (Group)</li>
     * </ol>
     */
    private void addGlobalConstituents(Group group, boolean tempBuild) {
        // 1. global builds artifacts
        group.addConstituent(new StoreKey(MAVEN_PKG_KEY, StoreType.group, UNTESTED_BUILDS_GROUP));
        if (tempBuild) {
            group.addConstituent(new StoreKey(MAVEN_PKG_KEY, StoreType.group, TEMPORARY_BUILDS_GROUP));
        }

        // 2. global shared-imports artifacts
        group.addConstituent(new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, SHARED_IMPORTS_ID));

        // 3. public group, containing remote proxies to the outside world
        group.addConstituent(new StoreKey(MAVEN_PKG_KEY, StoreType.group, PUBLIC_GROUP_ID));
    }

    /**
     * Convenience method for tests.
     */
    protected Indy getIndy(String accessToken) {
        return init(accessToken);
    }

    /**
     * Promote the hosted repository associated with a given project build to an arbitrary repository group.
     *
     * @return The promotion instance, which won't actually trigger promotion until its
     * {@link RunningRepositoryPromotion#monitor(java.util.function.Consumer, java.util.function.Consumer)} method is
     * called.
     */
    @Override
    public RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup, String accessToken)
            throws RepositoryManagerException {
        Indy indy = init(accessToken);
        return new MavenRunningPromotion(StoreType.hosted, buildRecord.getBuildContentId(), toGroup, indy);
    }

    @Override
    public RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord, String accessToken)
            throws RepositoryManagerException {
        Indy indy = init(accessToken);
        return new MavenRunningDeletion(StoreType.hosted, buildRecord.getBuildContentId(), indy);
    }

}
