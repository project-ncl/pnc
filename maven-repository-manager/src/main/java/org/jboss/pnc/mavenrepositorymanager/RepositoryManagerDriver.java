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

import org.apache.commons.lang.StringUtils;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.folo.client.IndyFoloContentClientModule;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.DRIVER_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.PUBLIC_GROUP_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.SHARED_IMPORTS_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.UNTESTED_BUILDS_GROUP;

/**
 * Implementation of {@link RepositoryManager} that manages an <a href="https://github.com/jdcasey/indy">AProx</a> instance to
 * support repositories for Maven-ish builds.
 * <p>
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 *
 * @author <a href="mailto:jdcasey@commonjava.org">John Casey</a>
 */
@ApplicationScoped
public class RepositoryManagerDriver implements RepositoryManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Indy indy;

    private List<String> internalRepoPatterns;

    @Deprecated
    public RepositoryManagerDriver() { // workaround for CDI constructor parameter injection bug
    }

    @SuppressWarnings("resource")
    @Inject
    public  RepositoryManagerDriver(Configuration configuration) {
        MavenRepoDriverModuleConfig config;
        try {
            config = configuration
                    .getModuleConfig(new PncConfigProvider<>(MavenRepoDriverModuleConfig.class));
            String baseUrl = config.getBaseUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            if (!baseUrl.endsWith("/api")) {
                baseUrl += "/api";
            }

            internalRepoPatterns = new ArrayList<>();
            internalRepoPatterns.add(MavenRepositoryConstants.SHARED_IMPORTS_ID);

            List<String> extraInternalRepoPatterns = config.getInternalRepoPatterns();
            if (extraInternalRepoPatterns != null) {
                internalRepoPatterns.addAll(extraInternalRepoPatterns);
            }

            indy = new Indy(baseUrl, new IndyFoloAdminClientModule(), new IndyFoloContentClientModule(),
                    new IndyPromoteClientModule()).connect();

            setupGlobalRepos();

        } catch (ConfigurationParseException e) {
            throw new IllegalStateException("Cannot read configuration for " + DRIVER_ID + ".", e);
        } catch (IndyClientException e) {
            throw new IllegalStateException("Failed to setup shared-releases or shared-imports hosted repository: "
                    + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        indy.close();
    }

    /**
     * Only supports {@link ArtifactRepo.Type#MAVEN}.
     */
    @Override
    public boolean canManage(ArtifactRepo.Type managerType) {
        return (managerType == ArtifactRepo.Type.MAVEN);
    }

    /**
     * Use the AProx client API to setup global and build-set level repos and groups, then setup the repo/group needed for this
     * build. Calculate the URL to use for resolving artifacts using the AProx Folo API (Folo is an artifact activity-tracker).
     * Return a new session ({@link MavenRepositorySession}) containing this information.
     *
     * @throws RepositoryManagerException In the event one or more repositories or groups can't be created to support the build
     *                                    (or product, or shared-releases).
     */
    @Override
    public RepositorySession createBuildRepository(BuildExecution buildExecution) throws RepositoryManagerException {

        String buildId = buildExecution.getBuildContentId();
        try {
            setupBuildRepos(buildExecution);
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

            url = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, StoreType.group, buildId);
            deployUrl = indy.module(IndyFoloContentClientModule.class).trackingUrl(buildId, StoreType.hosted, buildId);
            logger.info("Using '{}' for Maven repository access in build: {}", url, buildId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module for the artifact tracker: %s", e,
                    e.getMessage());
        }

        return new MavenRepositorySession(indy, buildId, new MavenRepositoryConnectionInfo(url, deployUrl), internalRepoPatterns);
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
    private void setupBuildRepos(BuildExecution execution)
            throws IndyClientException {

        String buildContentId = execution.getBuildContentId();
        int id = execution.getId();

        // if the build-level group doesn't exist, create it.
        if (!indy.stores().exists(StoreType.group, buildContentId)) {
            // if the product-level storage repo (for in-progress product builds) doesn't exist, create it.
            if (!indy.stores().exists(StoreType.hosted, buildContentId)) {
                HostedRepository buildArtifacts = new HostedRepository(buildContentId);
                buildArtifacts.setAllowSnapshots(true);
                buildArtifacts.setAllowReleases(true);

                buildArtifacts.setDescription(String.format("Build output for PNC build #%s", id));

                indy.stores().create(buildArtifacts,
                        "Creating hosted repository for build: " + id + " (repo: " + buildContentId + ")", HostedRepository.class);
            }

            Group buildGroup = new Group(buildContentId);
            buildGroup.setDescription(String.format("Aggregation group for PNC build #%s", id));

            // build-local artifacts
            buildGroup.addConstituent(new StoreKey(StoreType.hosted, buildContentId));

            // Global-level repos, for captured/shared artifacts and access to the outside world
            addGlobalConstituents(buildGroup);

            indy.stores().create(buildGroup,
                    "Creating repository group for resolving artifacts in build: " + id + " (repo: " + buildContentId + ")", Group.class);
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
        group.addConstituent(new StoreKey(StoreType.group, UNTESTED_BUILDS_GROUP));

        // 2. global shared-imports artifacts
        group.addConstituent(new StoreKey(StoreType.hosted, SHARED_IMPORTS_ID));

        // 3. public group, containing remote proxies to the outside world
        group.addConstituent(new StoreKey(StoreType.group, PUBLIC_GROUP_ID));
    }

    /**
     * Lazily create the shared-releases and shared-imports global hosted repositories if they don't already exist.
     *
     * @throws IndyClientException
     */
    private void setupGlobalRepos() throws IndyClientException {
        // if the global shared-releases repository doesn't exist, create it.
        if (!indy.stores().exists(StoreType.group, UNTESTED_BUILDS_GROUP)) {
            Group sharedArtifacts = new Group(UNTESTED_BUILDS_GROUP);

            indy.stores().create(sharedArtifacts, "Creating global shared-builds repository group.", Group.class);
        }

        // if the global imports repo doesn't exist, create it.
        if (!indy.stores().exists(StoreType.hosted, SHARED_IMPORTS_ID)) {
            HostedRepository sharedImports = new HostedRepository(SHARED_IMPORTS_ID);
            sharedImports.setAllowSnapshots(false);
            sharedImports.setAllowReleases(true);

            indy.stores().create(sharedImports, "Creating global repository for hosting external imports used in builds.",
                    HostedRepository.class);
        }
    }

    /**
     * Convenience method for tests.
     */
    protected Indy getIndy() {
        return indy;
    }

    /**
     * Promote the hosted repository associated with a given project build to an arbitrary repository group.
     *
     * @return The promotion instance, which won't actually trigger promotion until its
     * {@link RunningRepositoryPromotion#monitor(java.util.function.Consumer, java.util.function.Consumer)} method is
     * called.
     */
    @Override
    public RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup) throws RepositoryManagerException {

        return new MavenRunningPromotion(StoreType.hosted, buildRecord.getBuildContentId(), toGroup, indy);
    }

    @Override
    public RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord) throws RepositoryManagerException {
        return new MavenRunningDeletion(StoreType.hosted, buildRecord.getBuildContentId(), indy);
    }

}
