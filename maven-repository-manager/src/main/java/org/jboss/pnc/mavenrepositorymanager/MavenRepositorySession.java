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

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.client.core.module.AproxContentClientModule;
import org.commonjava.aprox.folo.client.AproxFoloAdminClientModule;
import org.commonjava.aprox.folo.dto.TrackedContentDTO;
import org.commonjava.aprox.folo.dto.TrackedContentEntryDTO;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.commonjava.aprox.promote.client.AproxPromoteClientModule;
import org.commonjava.aprox.promote.model.PromoteRequest;
import org.commonjava.aprox.promote.model.PromoteResult;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.util.ArtifactPathInfo;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link RepositorySession} implementation that works with the Maven {@link RepositoryManagerDriver} (which connects to an
 * AProx server instance for repository management). This session contains connection information for rendering Maven
 * settings.xml files and the like, along with the components necessary to extract the artifacts (dependencies, build uploads)
 * for the associated build.
 * 
 * Artifact extraction also implies promotion of imported dependencies to a shared-imports Maven repository for safe keeping. In
 * the case of composed (chained) builds, it also implies promotion of the build output to the associated build-set repository
 * group, to expose them for use in successive builds in the chain.
 */
public class MavenRepositorySession implements RepositorySession
{

    private Aprox aprox;
    private final String buildRepoId;
    private String buildSetId;

    private final RepositoryConnectionInfo connectionInfo;
    private BuildExecutionType buildExecutionType;

    // TODO: Create and pass in suitable parameters to Aprox to create the
    //       proxy repository.
    public MavenRepositorySession(Aprox aprox, String buildRepoId, String buildSetId, BuildExecutionType buildExecutionType,
            MavenRepositoryConnectionInfo info)
    {
        this.aprox = aprox;
        this.buildRepoId = buildRepoId;
        this.buildSetId = buildSetId;
        this.buildExecutionType = buildExecutionType;
        this.connectionInfo = info;
    }


    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }


    @Override
    public RepositoryType getType() {
        return RepositoryType.MAVEN;
    }


    @Override
    public String getBuildRepositoryId() {
        return buildRepoId;
    }

    @Override
    public String getBuildSetRepositoryId() {
        return buildSetId;
    }

    @Override
    public RepositoryConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    /**
     * Retrieve tracking report from repository manager. Add each tracked download to the dependencies of the build result. Add
     * each tracked upload to the built artifacts of the build result. Promote uploaded artifacts to the product-level storage.
     * Finally, clear the tracking report, and delete the hosted repository + group associated with the completed build.
     */
    @Override
    public RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException {
        TrackedContentDTO report;
        try {
            report = aprox.module(AproxFoloAdminClientModule.class).getTrackingReport(buildRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s. Reason: %s", e, buildRepoId,
                    e.getMessage());
        }
        if ( report == null)
        {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s.", buildRepoId);
        }

        List<Artifact> uploads = processUploads(report);
        List<Artifact> downloads = processDownloads(report);

        promoteToBuildContentSet();

        RepositoryManagerResult repositoryManagerResult = new MavenRepositoryManagerResult(uploads, downloads);

        // clean up.
        try {
            aprox.module(AproxFoloAdminClientModule.class).clearTrackingRecord(buildRepoId);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException(
                    "Failed to clean up build repositories / tracking information for: %s. Reason: %s", e, buildRepoId,
                    e.getMessage());
        }
        return repositoryManagerResult;
    }

    /**
     * Promote all build dependencies NOT ALREADY CAPTURED to the hosted repository holding store for the shared imports and
     * return dependency artifacts meta data.
     *
     * @param report The tracking report that contains info about artifacts downloaded by the build
     * @return List of dependency artifacts meta data
     * @throws RepositoryManagerException In case of a client API transport error or an error during promotion of artifacts
     */
    private List<Artifact> processDownloads(TrackedContentDTO report) throws RepositoryManagerException {

        AproxContentClientModule content;
        try {
            content = aprox.content();
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module. Reason: %s", e, e.getMessage());
        }

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        if (downloads != null) {
            List<Artifact> deps = new ArrayList<>();

            Map<StoreKey, Set<String>> toPromote = new HashMap<>();

            StoreKey sharedImports = new StoreKey(StoreType.hosted, RepositoryManagerDriver.SHARED_IMPORTS_ID);
            StoreKey sharedReleases = new StoreKey(StoreType.hosted, RepositoryManagerDriver.SHARED_RELEASES_ID);

            for (TrackedContentEntryDTO download : downloads) {
                StoreKey sk = download.getStoreKey();
                if (!sharedImports.equals(sk) && !sharedReleases.equals(sk)) {
                    // this has not been captured, so promote it.
                    Set<String> paths = toPromote.get(sk);
                    if (paths == null) {
                        paths = new HashSet<>();
                        toPromote.put(sk, paths);
                    }

                    paths.add(download.getPath());
                }

                String path = download.getPath();
                ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);
                if (pathInfo == null) {
                    // metadata file. Ignore.
                    continue;
                }

                ArtifactRef aref = new ArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier(), false);

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder().checksum(download.getSha256())
                        .deployUrl(content.contentUrl(download.getStoreKey(), download.getPath()))
                        .filename(new File(path).getName()).identifier(aref.toString()).repoType(RepositoryType.MAVEN)
                        .status(ArtifactStatus.BINARY_IMPORTED);

                deps.add(artifactBuilder.build());
            }

            for (Map.Entry<StoreKey, Set<String>> entry : toPromote.entrySet()) {
                PromoteRequest req = new PromoteRequest(entry.getKey(), sharedImports, entry.getValue()).setPurgeSource(false);
                doPromote(req);
            }

            return deps;
        }
        return Collections.emptyList();
    }

    /**
     * Return output artifacts metadata.
     *
     * @param report The tracking report that contains info about artifacts uploaded (output) from the build
     * @return List of output artifacts meta data
     * @throws RepositoryManagerException In case of a client API transport error or an error during promotion of artifacts
     */
    private List<Artifact> processUploads(TrackedContentDTO report)
            throws RepositoryManagerException {

        Set<TrackedContentEntryDTO> uploads = report.getUploads();
        if (uploads != null) {
            List<Artifact> builds = new ArrayList<>();

            for (TrackedContentEntryDTO upload : uploads) {
                String path = upload.getPath();
                ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);
                if (pathInfo == null) {
                    // metadata file. Ignore.
                    continue;
                }

                ArtifactRef aref = new ArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier(), false);

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder().checksum(upload.getSha256())
                        .deployUrl(upload.getLocalUrl()).filename(new File(path).getName()).identifier(aref.toString())
                        .repoType(RepositoryType.MAVEN).status(ArtifactStatus.BINARY_BUILT);

                builds.add(artifactBuilder.build());
            }

            return builds;
        }
        return Collections.emptyList();
    }

    /**
     * Promotes a set of artifact paths (or everything, if the path-set is missing) from a particular AProx artifact store to
     * another, and handle the various error conditions that may arise. If the promote call fails, attempt to rollback before
     * throwing an exception.
     *
     * @param req The promotion request to process, which contains source and target store keys, and (optionally) the set of
     *        paths to promote
     * @throws RepositoryManagerException When either the client API throws an exception due to something unexpected in
     *         transport, or if the promotion process results in an error.
     */
    private void doPromote(PromoteRequest req) throws RepositoryManagerException {
        AproxPromoteClientModule promoter;
        try {
            promoter = aprox.module(AproxPromoteClientModule.class);
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module. Reason: %s", e, e.getMessage());
        }

        try {
            PromoteResult result = promoter.promote(req);
            if (result.getError() != null) {
                String addendum = "";
                try {
                    PromoteResult rollback = promoter.rollback(result);
                    if (rollback.getError() != null) {
                        addendum = "\nROLLBACK WARNING: Promotion rollback also failed! Reason given: " + result.getError();
                    }

                } catch (AproxClientException e) {
                    throw new RepositoryManagerException("Rollback failed for promotion of: %s. Reason: %s", e, req,
                            e.getMessage());
                }

                throw new RepositoryManagerException("Failed to promote: %s. Reason given was: %s%s", req, result.getError(),
                        addendum);
            }
        } catch (AproxClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, req, e.getMessage());
        }
    }


    /**
     * If the execution type is {@link BuildExecutionType#COMPOSED_BUILD} and build-set repository ID is set, add the build
     * repository (hosted component) containing the build output as a member of the group corresponding to that build-set ID. If
     * the build-set group doesn't exist, try to create it.
     */
    public void promoteToBuildContentSet() throws RepositoryManagerException {
        if (buildExecutionType == BuildExecutionType.COMPOSED_BUILD && buildSetId != null) {
            try {
                Group setGroup = aprox.stores().load(StoreType.group, buildSetId, Group.class);
                if (setGroup == null) {
                    setGroup = new Group(buildSetId, new StoreKey(StoreType.hosted, buildRepoId));
                    aprox.stores().create(setGroup, "Adding build-set group: " + buildSetId, Group.class);
                } else {
                    setGroup.addConstituent(new StoreKey(StoreType.hosted, buildRepoId));
                    aprox.stores().update(setGroup, "Adding build: " + buildRepoId + " to build-set: " + buildSetId);
                }
            } catch (AproxClientException e) {
                throw new RepositoryManagerException(
                        "Failed to promote build repository: %s to build-set group: %s. Reason: %s", e, buildRepoId,
                        buildSetId, e.getMessage());
            }
        }
    }

}
