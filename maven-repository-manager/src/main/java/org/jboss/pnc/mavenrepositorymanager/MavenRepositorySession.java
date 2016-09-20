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
import org.commonjava.indy.client.core.module.IndyContentClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.folo.dto.TrackedContentDTO;
import org.commonjava.indy.folo.dto.TrackedContentEntryDTO;
import org.commonjava.indy.model.core.AccessChannel;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.commonjava.indy.promote.model.GroupPromoteRequest;
import org.commonjava.indy.promote.model.GroupPromoteResult;
import org.commonjava.indy.promote.model.PathsPromoteRequest;
import org.commonjava.indy.promote.model.PathsPromoteResult;
import org.commonjava.indy.promote.model.ValidationResult;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.util.ArtifactPathInfo;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
public class MavenRepositorySession implements RepositorySession {

    private static Set<String> IGNORED_PATH_SUFFIXES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("maven-metadata.xml", ".sha1", ".md5", ".asc")));

    private Indy indy;
    private final String buildContentId;

    private final RepositoryConnectionInfo connectionInfo;
    private boolean isSetBuild;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // TODO: Create and pass in suitable parameters to Indy to create the
    //       proxy repository.
    @Deprecated
    public MavenRepositorySession(Indy indy, String buildContentId, boolean isSetBuild,
                                  MavenRepositoryConnectionInfo info) {
        this.indy = indy;
        this.buildContentId = buildContentId;
        this.isSetBuild = isSetBuild;
        this.connectionInfo = info;
    }

    public MavenRepositorySession(Indy indy, String buildContentId, MavenRepositoryConnectionInfo info) {
        this.indy = indy;
        this.buildContentId = buildContentId;
        this.isSetBuild = false; //TODO remove
        this.connectionInfo = info;
    }

    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }

    @Override
    public ArtifactRepo.Type getType() {
        return ArtifactRepo.Type.MAVEN;
    }

    @Override
    public String getBuildRepositoryId() {
        return buildContentId;
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
            IndyFoloAdminClientModule foloAdmin = indy.module(IndyFoloAdminClientModule.class);
            boolean sealed = foloAdmin.sealTrackingRecord(buildContentId);
            if (!sealed) {
                throw new RepositoryManagerException("Failed to seal content-tracking record for: %s.", buildContentId);
            }

            report = foloAdmin.getTrackingReport(buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s. Reason: %s", e, buildContentId,
                    e.getMessage());
        }
        if (report == null) {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s.", buildContentId);
        }

        Comparator<Artifact> comp = (one, two) -> one.getIdentifier().compareTo(two.getIdentifier());

        List<Artifact> uploads = processUploads(report);
        Collections.sort(uploads, comp);

        List<Artifact> downloads = processDownloads(report);
        Collections.sort(downloads, comp);

        try {
            indy.stores().delete(StoreType.group, buildContentId, "[Post-Build] Removing build aggregation group: " + buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx stores module. Reason: %s", e, e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Returning built artifacts / dependencies:\nUploads:\n  {}\n\nDownloads:\n  {}\n\n",
                StringUtils.join(uploads, "\n  "), StringUtils.join(downloads, "\n  "));

        promoteToBuildContentSet(); //TODO report user readable responses using MavenRepositoryManagerResult with log and status

        return new MavenRepositoryManagerResult(uploads, downloads, buildContentId);
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
        Logger logger = LoggerFactory.getLogger(getClass());

        IndyContentClientModule content;
        try {
            content = indy.content();
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module. Reason: %s", e, e.getMessage());
        }

        List<Artifact> deps = new ArrayList<>();

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        if (downloads != null) {

            Map<StoreKey, Set<String>> toPromote = new HashMap<>();

            StoreKey sharedImports = new StoreKey(StoreType.hosted, MavenRepositoryConstants.SHARED_IMPORTS_ID);
//            StoreKey sharedReleases = new StoreKey(StoreType.hosted, RepositoryManagerDriver.SHARED_RELEASES_ID);

            for (TrackedContentEntryDTO download : downloads) {
                if (ignoreContent(download.getPath())) {
                    logger.debug("Ignoring download (matched in ignored-suffixes): {} (From: {})", download.getPath(), download.getStoreKey());
                    continue;
                }

                StoreKey sk = download.getStoreKey();

                // If the entry is from a hosted repository, it shouldn't be auto-promoted.
                // If the entry is already in shared-imports, it shouldn't be auto-promoted to there.
                // New binary imports will be coming from a remote repository...
                // TODO: Enterprise maven repository (product repo) handling...
                if (!sharedImports.equals(sk) && StoreType.hosted != sk.getType()) {
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
                    logger.info("NOT logging file download: {}. It does not appear to be an artifact. (From: {})", path, sk);
                    continue;
                }

                ArtifactRef aref = new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
                logger.info("Recording download: {}", aref);

                String originUrl = download.getOriginUrl();
                if (originUrl == null) {
                    // this is from a hosted repository, either shared-imports or a build, or something like that.
                    originUrl = download.getLocalUrl();
                }

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(download.getMd5())
                        .sha1(download.getSha1())
                        .sha256(download.getSha256())
                        .size(download.getSize())
                        .deployUrl(content.contentUrl(download.getStoreKey(), download.getPath()))
                        .originUrl(originUrl)
                        .importDate(Date.from(Instant.now()))
                        .filename(new File(path).getName())
                        .identifier(aref.toString())
                        .repoType(toRepoType(download.getAccessChannel()));

                Artifact artifact = validateArtifact(artifactBuilder.build());
                deps.add(artifact);
            }

            for (Map.Entry<StoreKey, Set<String>> entry : toPromote.entrySet()) {
                PathsPromoteRequest req = new PathsPromoteRequest(entry.getKey(), sharedImports, entry.getValue()).setPurgeSource(false);
                doPromoteByPath(req);
            }
        }

        return deps;
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
        Logger logger = LoggerFactory.getLogger(getClass());

        Set<TrackedContentEntryDTO> uploads = report.getUploads();
        if (uploads != null) {
            List<Artifact> builds = new ArrayList<>();

            for (TrackedContentEntryDTO upload : uploads) {
                String path = upload.getPath();
                if (ignoreContent(path)) {
                    logger.debug("Ignoring upload (matched in ignored-suffixes): {} (From: {})", path, upload.getStoreKey());
                    continue;
                }

                ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);
                if (pathInfo == null) {
                    // metadata file. Ignore.
                    logger.info("NOT logging file upload: {}. It does not appear to be an artifact. (From: {})", path, upload.getStoreKey());
                    continue;
                }

                ArtifactRef aref = new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
                logger.info("Recording upload: {}", aref);

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(upload.getMd5())
                        .sha1(upload.getSha1())
                        .sha256(upload.getSha256())
                        .size(upload.getSize())
                        .deployUrl(upload.getLocalUrl())
                        .filename(new File(path).getName())
                        .identifier(aref.toString())
                        .repoType(ArtifactRepo.Type.MAVEN);

                Artifact artifact = validateArtifact(artifactBuilder.build());
                builds.add(artifact);
            }

            return builds;
        }
        return Collections.emptyList();
    }

    /**
     * Check artifact for any validation errors.  If there are constraint violations, then a RepositoryManagerException is thrown.
     * Otherwise the artifact is returned.
     *
     * @param artifact to validate
     * @return the same artifact
     * @throws RepositoryManagerException if there are constraint violations
     */
    private Artifact validateArtifact(Artifact artifact) throws RepositoryManagerException {
        Set<ConstraintViolation<Artifact>> violations = validator.validate(artifact);
        if (!violations.isEmpty()) {
            throw new RepositoryManagerException("Repository manager returned invalid artifact: " + artifact.toString() + " Constraint Violations: %s", violations);
        }
        return artifact;
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
    private void doPromoteByPath(PathsPromoteRequest req) throws RepositoryManagerException {
        IndyPromoteClientModule promoter;
        try {
            promoter = indy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module. Reason: %s", e, e.getMessage());
        }

        try {
            PathsPromoteResult result = promoter.promoteByPath(req);
            if (result.getError() != null) {
                String addendum = "";
                try {
                    PathsPromoteResult rollback = promoter.rollbackPathPromote(result);
                    if (rollback.getError() != null) {
                        addendum = "\nROLLBACK WARNING: Promotion rollback also failed! Reason given: " + result.getError();
                    }

                } catch (IndyClientException e) {
                    throw new RepositoryManagerException("Rollback failed for promotion of: %s. Reason: %s", e, req,
                            e.getMessage());
                }

                throw new RepositoryManagerException("Failed to promote: %s. Reason given was: %s%s", req, result.getError(),
                        addendum);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, req, e.getMessage());
        }
    }


    /**
     * Promote the build output to shared-releases (using group promotion, where the build repo is added to the group's
     * membership).
     */
    public void promoteToBuildContentSet() throws RepositoryManagerException {
        IndyPromoteClientModule promoter;
        try {
            promoter = indy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx client module. Reason: %s", e, e.getMessage());
        }

        GroupPromoteRequest request = new GroupPromoteRequest(new StoreKey(StoreType.hosted, buildContentId), MavenRepositoryConstants.UNTESTED_BUILDS_GROUP);
        try {
            GroupPromoteResult result = promoter.promoteToGroup(request);
            if (!result.succeeded()) {
                String reason = result.getError();
                if (reason == null) {
                    ValidationResult validations = result.getValidations();
                    if (validations != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("One or more validation rules failed in rule-set: ").append(validations.getRuleSet());

                        validations.getValidatorErrors().forEach((rule, error) -> {
                            sb.append(rule).append(":\n  ").append(error).append("\n\n");
                        });

                        reason = sb.toString();
                    }
                }

                throw new RepositoryManagerException("Failed to promote: %s to group: %s. Reason given was: %s", request.getSource(), request.getTargetGroup(), reason);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, request, e.getMessage());
        }
    }

    private boolean ignoreContent(String path) {
        for (String suffix : IGNORED_PATH_SUFFIXES) {
            if (path.endsWith(suffix))
                return true;
        }

        return false;
    }

    private ArtifactRepo.Type toRepoType(AccessChannel accessChannel) {
        switch (accessChannel) {
            case MAVEN_REPO:
                return ArtifactRepo.Type.MAVEN;
            case GENERIC_PROXY:
                return ArtifactRepo.Type.GENERIC_PROXY;
            default:
                return ArtifactRepo.Type.GENERIC_PROXY;
        }
    }
}
