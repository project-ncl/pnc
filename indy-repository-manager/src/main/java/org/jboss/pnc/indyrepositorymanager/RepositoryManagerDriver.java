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
package org.jboss.pnc.indyrepositorymanager;

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
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.IgnoredPathSuffixes;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.InternalRepoPatterns;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.pkg.npm.model.NPMPackageTypeDescriptor.NPM_PKG_KEY;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.COMMON_BUILD_GROUP_CONSTITUENTS_GROUP;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.DRIVER_ID;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.TEMPORARY_BUILDS_GROUP;

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

    private InternalRepoPatterns internalRepoPatterns;

    private IgnoredPathSuffixes ignoredPathSuffixes;

    @Deprecated
    public RepositoryManagerDriver() { // workaround for CDI constructor parameter injection bug
        this.DEFAULT_REQUEST_TIMEOUT = 0;
        this.BUILD_PROMOTION_GROUP = "";
        this.TEMP_BUILD_PROMOTION_GROUP = "";
    }

    @Inject
    public RepositoryManagerDriver(Configuration configuration) {
        IndyRepoDriverModuleConfig config;
        try {
            config = configuration
                    .getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));
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

        List<String> constInternalRepoPatterns = new ArrayList<>();
        constInternalRepoPatterns.add(IndyRepositoryConstants.SHARED_IMPORTS_ID);
        internalRepoPatterns = new InternalRepoPatterns();
        internalRepoPatterns.setMaven(constInternalRepoPatterns);
        internalRepoPatterns.setNpm(new ArrayList<>(constInternalRepoPatterns));

        InternalRepoPatterns extraInternalRepoPatterns = config.getInternalRepoPatterns();
        if (extraInternalRepoPatterns != null) {
            internalRepoPatterns.addMaven(extraInternalRepoPatterns.getMaven());
            internalRepoPatterns.addNpm(extraInternalRepoPatterns.getNpm());
        }

        IgnoredPathSuffixes ignoredPathSuffixes = config.getIgnoredPathSuffixes();
        if (ignoredPathSuffixes == null) {
            this.ignoredPathSuffixes = new IgnoredPathSuffixes();
        } else {
            this.ignoredPathSuffixes = ignoredPathSuffixes; // TODO do we need a copy?
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
        return (managerType == TargetRepository.Type.MAVEN) || (managerType == TargetRepository.Type.NPM);
    }

    /**
     * Use the Indy client API to setup global and build-set level repos and groups, then setup the repo/group needed for this
     * build. Calculate the URL to use for resolving artifacts using the Indy Folo API (Folo is an artifact activity-tracker).
     * Return a new session ({@link IndyRepositorySession}) containing this information.
     *
     * @throws RepositoryManagerException In the event one or more repositories or groups can't be created to support the build
     *                                    (or product, or shared-releases).
     */
    @Override
    public RepositorySession createBuildRepository(BuildExecution buildExecution, String accessToken,
            String serviceAccountToken, TargetRepository.Type repositoryType) throws RepositoryManagerException {
        Indy indy = init(accessToken);
        Indy serviceAccountIndy = init(serviceAccountToken);

        String pakageType = getIndyPackageTypeKey(repositoryType);

        String buildId = buildExecution.getBuildContentId();
        try {
            setupBuildRepos(buildExecution, pakageType, indy);
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

            StoreKey groupKey = new StoreKey(pakageType, StoreType.group, buildId);
            url = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, groupKey);

            StoreKey hostedKey = new StoreKey(pakageType, StoreType.hosted, buildId);
            deployUrl = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, hostedKey);

            logger.info("Using '{}' for {} repository access in build: {}", url, pakageType, buildId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy client module for the artifact tracker: %s", e,
                    e.getMessage());
        }

        boolean tempBuild = buildExecution.isTempBuild();
        String buildPromotionGroup = tempBuild ? TEMP_BUILD_PROMOTION_GROUP : BUILD_PROMOTION_GROUP;
        return new IndyRepositorySession(indy, serviceAccountIndy, buildId, pakageType,
                new IndyRepositoryConnectionInfo(url, deployUrl),
                internalRepoPatterns, ignoredPathSuffixes, buildPromotionGroup, tempBuild);
    }

    private String getIndyPackageTypeKey(TargetRepository.Type repoType) {
        switch (repoType) {
            case MAVEN:
                return MAVEN_PKG_KEY;
            case NPM:
                return NPM_PKG_KEY;
            default:
                throw new IllegalArgumentException("Repository type " + repoType
                        + " is not supported by this repository manager driver.");
        }
    }

    /**
     * Create the hosted repository and group necessary to support a single build. The hosted repository holds artifacts
     * uploaded from the build, and the group coordinates access to this hosted repository, along with content from the
     * product-level content group with which this build is associated. The group also provides a tracking target, so the
     * repository manager can keep track of downloads and uploads for the build.
     *
     * @param execution The execution object, which contains the content id for creating the repo, and the build id.
     * @param pakageType the package type key used by Indy
     * @throws IndyClientException
     */
    private void setupBuildRepos(BuildExecution execution, String pakageType, Indy indy)
            throws IndyClientException {

        String buildContentId = execution.getBuildContentId();
        int id = execution.getId();

        // if the build-level group doesn't exist, create it.
        StoreKey groupKey = new StoreKey(pakageType, StoreType.group, buildContentId);

        if (!indy.stores().exists(groupKey)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            StoreKey hostedKey = new StoreKey(pakageType, StoreType.hosted, buildContentId);
            boolean tempBuild = execution.isTempBuild();
            if (!indy.stores().exists(hostedKey)) {
                HostedRepository buildArtifacts = new HostedRepository(pakageType, buildContentId);
                buildArtifacts.setAllowSnapshots(tempBuild);
                buildArtifacts.setAllowReleases(true);

                buildArtifacts.setDescription(String.format("Build output for PNC %s build #%s", pakageType, id));

                indy.stores().create(buildArtifacts, "Creating hosted repository for " + pakageType + " build: " + id
                        + " (repo: " + buildContentId + ")", HostedRepository.class);
            }

            Group buildGroup = new Group(pakageType, buildContentId);
            String adjective = tempBuild ? "temporary " : "";
            buildGroup.setDescription(String.format("Aggregation group for PNC %sbuild #%s", adjective, id));

            // build-local artifacts
            buildGroup.addConstituent(hostedKey);

            // Global-level repos, for captured/shared artifacts and access to the outside world
            addGlobalConstituents(pakageType, buildGroup, tempBuild);

            // add extra repositories removed from poms by the adjust process
            addExtraConstituents(pakageType, execution.getArtifactRepositories(), id, buildContentId, indy, buildGroup);

            indy.stores().create(buildGroup, "Creating repository group for resolving artifacts in build: " + id
                    + " (repo: " + buildContentId + ")", Group.class);
        }
    }

    /**
     * Adds extra remote repositories to the build group that are requested for the particular build. For a Maven build
     * these are repositories defined in the root pom removed by PME by the adjust process.
     *
     * @param pakageType
     *            the package type key used by Indy
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
     *
     * @throws IndyClientException
     *             in case of an issue when communicating with the repository manager
     */
    private void addExtraConstituents(String pakageType, List<ArtifactRepository> repositories, int buildId,
            String buildContentId, Indy indy, Group buildGroup) throws IndyClientException {
        if (repositories != null && !repositories.isEmpty()) {
            StoreListingDTO<RemoteRepository> existingRepos = indy.stores().listRemoteRepositories(pakageType);
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
                    String remoteName = "i-" + convertIllegalCharacters(repository.getId());

                    // find a free repository ID for the newly created repo
                    remoteKey = new StoreKey(pakageType, StoreType.remote, remoteName);
                    int i = 2;
                    while (indy.stores().exists(remoteKey)) {
                        remoteKey = new StoreKey(pakageType, StoreType.remote, remoteName + "-" + i++);
                    }

                    RemoteRepository remoteRepo = new RemoteRepository(pakageType, remoteKey.getName(), repository.getUrl());
                    remoteRepo.setAllowReleases(repository.getReleases());
                    remoteRepo.setAllowSnapshots(repository.getSnapshots());
                    remoteRepo.setDescription("Implicitly created " + pakageType + " repo for: " + repository.getName()
                            + " (" + repository.getId() + ") from repository declaration removed by PME in build "
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
     * Converts characters in a given string considered as illegal by Indy to underscores.
     *
     * @param name repository name
     * @return string with converted characters
     */
    private String convertIllegalCharacters(String name) {
        char[] result = new char[name.length()];
        for (int i = 0; i < name.length(); i++) {
            char checkedChar = name.charAt(i);
            if (Character.isLetterOrDigit(checkedChar) || checkedChar == '+' || checkedChar == '-' || checkedChar == '.') {
                result[i] = checkedChar;
            } else {
                result[i] = '_';
            }
        }
        return String.valueOf(result);
    }

    /**
     * Add the constituents that every build repository group should contain:
     * <ol>
     * <li>builds-untested (Group)</li>
     * <li>for temporary builds add also temporary-builds (Group)</li>
     * <li>shared-imports (Hosted Repo)</li>
     * <li>public (Group)</li>
     * </ol>
     * @param pakageType package type key used by Indy
     */
    private void addGlobalConstituents(String pakageType, Group group, boolean tempBuild) {
        // 1. global builds artifacts
        if (tempBuild) {
            group.addConstituent(new StoreKey(pakageType, StoreType.group, TEMPORARY_BUILDS_GROUP));
        }
        group.addConstituent(new StoreKey(pakageType, StoreType.group, COMMON_BUILD_GROUP_CONSTITUENTS_GROUP));
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
    public RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String pakageType, String toGroup,
            String accessToken) throws RepositoryManagerException {
        Indy indy = init(accessToken);
        return new IndyRunningPromotion(pakageType, StoreType.hosted, buildRecord.getBuildContentId(), toGroup, indy);
    }

    @Override
    public RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord, String pakageType, String accessToken)
            throws RepositoryManagerException {
        Indy indy = init(accessToken);
        return new IndyRunningDeletion(pakageType, StoreType.hosted, buildRecord.getBuildContentId(), indy);
    }

}
