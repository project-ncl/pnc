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
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.commonjava.indy.promote.model.GroupPromoteRequest;
import org.commonjava.indy.promote.model.GroupPromoteResult;
import org.commonjava.indy.promote.model.PathsPromoteRequest;
import org.commonjava.indy.promote.model.PathsPromoteResult;
import org.commonjava.indy.promote.model.PromoteRequest;
import org.commonjava.indy.promote.model.ValidationResult;
import org.commonjava.atlas.maven.ident.ref.ArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.util.ArtifactPathInfo;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.SHARED_IMPORTS_ID;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.TEMPORARY_BUILDS_GROUP;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.UNTESTED_BUILDS_GROUP;

/**
 * {@link RepositorySession} implementation that works with the Maven {@link RepositoryManagerDriver} (which connects to an
 * Indy server instance for repository management). This session contains connection information for rendering Maven
 * settings.xml files and the like, along with the components necessary to extract the artifacts (dependencies, build uploads)
 * for the associated build.
 *
 * Artifact extraction also implies promotion of imported dependencies to a shared-imports Maven repository for safe keeping. In
 * the case of composed (chained) builds, it also implies promotion of the build output to the associated build-set repository
 * group, to expose them for use in successive builds in the chain.
 */
public class MavenRepositorySession implements RepositorySession {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-executor");

    private Set<String> ignoredPathSuffixes;

    private boolean isTempBuild;

    private Indy indy;
    private Indy serviceAccountIndy;
    private final String buildContentId;
    private List<String> internalRepoPatterns;

    private final RepositoryConnectionInfo connectionInfo;
    private boolean isSetBuild;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private String buildPromotionTarget;

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

    public MavenRepositorySession(Indy indy, Indy serviceAccountIndy, String buildContentId,
            MavenRepositoryConnectionInfo info, List<String> internalRepoPatterns,
            Set<String> ignoredPathSuffixes, String buildPromotionTarget, boolean isTempBuild) {
        this.indy = indy;
        this.serviceAccountIndy = serviceAccountIndy;
        this.buildContentId = buildContentId;
        this.internalRepoPatterns = internalRepoPatterns;
        this.ignoredPathSuffixes = ignoredPathSuffixes;
        this.isSetBuild = false; //TODO remove
        this.connectionInfo = info;
        this.buildPromotionTarget = buildPromotionTarget;
        this.isTempBuild = isTempBuild; //TODO define based on buildPromotionGroup
    }

    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }

    @Override
    public TargetRepository.Type getType() {
        return TargetRepository.Type.MAVEN;
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
            StoreKey key = new StoreKey(MAVEN_PKG_KEY, StoreType.group, buildContentId);
            serviceAccountIndy.stores().delete(key, "[Post-Build] Removing build aggregation group: " + buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve AProx stores module. Reason: %s", e, e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Returning built artifacts / dependencies:\nUploads:\n  {}\n\nDownloads:\n  {}\n\n",
                StringUtils.join(uploads, "\n  "), StringUtils.join(downloads, "\n  "));

        String log = "";
        CompletionStatus status = CompletionStatus.SUCCESS;
        try {
            promoteToBuildContentSet(uploads);
        } catch (RepositoryManagerException rme) {
            status = CompletionStatus.FAILED;
            log = rme.getMessage();
            logger.error("Promotion validation error(s): \n" + log);
            userLog.error("Artifact promotion failed. Promotion validation error(s): {}", log);
            // prevent saving artifacts and dependencies to a failed build
            downloads = Collections.emptyList();
            uploads = Collections.emptyList();
        }

        return new MavenRepositoryManagerResult(uploads, downloads, buildContentId, log, status);
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

            Map<StoreKey, Map<StoreKey, Set<String>>> toPromote = new HashMap<>();

            StoreKey sharedImports = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, MavenRepositoryConstants.SHARED_IMPORTS_ID);
//            StoreKey sharedReleases = new StoreKey(StoreType.hosted, RepositoryManagerDriver.SHARED_RELEASES_ID);

            for (TrackedContentEntryDTO download : downloads) {
                String path = download.getPath();
                StoreKey source = download.getStoreKey();
                if (ignoreContent(path)) {
                    logger.debug("Ignoring download (matched in ignored-suffixes): {} (From: {})", download.getPath(), source);
                    continue;
                }

                // If the entry is from a hosted repository, it shouldn't be auto-promoted.
                // If the entry is already in shared-imports, it shouldn't be auto-promoted to there.
                // New binary imports will be coming from a remote repository...
                // TODO: Enterprise maven repository (product repo) handling...
                if (isExternalOrigin(source) && StoreType.hosted != source.getType()) {
                    StoreKey target = null;
                    Map<StoreKey, Set<String>> sources = null;
                    Set<String> paths = null;

                    // this has not been captured, so promote it.
                    switch (download.getAccessChannel()) {
                        case MAVEN_REPO:
                            target = sharedImports;
                            sources = toPromote.computeIfAbsent(target, t -> new HashMap<>());
                            paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                            paths.add(download.getPath());
                            paths.add(download.getPath() + ".md5");
                            paths.add(download.getPath() + ".sha1");
                            break;

                        case GENERIC_PROXY:
                            String remoteName = source.getName();
                            String hostedName = getGenericHostedRepoName(remoteName);
                            target = new StoreKey(source.getPackageType(), StoreType.hosted, hostedName);
                            sources = toPromote.computeIfAbsent(target, t -> new HashMap<>());
                            paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                            paths.add(download.getPath());
                            break;

                        default:
                            // do not promote anything else anywhere
                            break;
                    }
                }

                ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(path);

                String identifier;
                if (pathInfo == null) {
                    identifier = download.getOriginUrl();
                    if (identifier == null) {
                        // this is from a hosted repository, either shared-imports or a build, or something like that.
                        identifier = download.getLocalUrl();
                    }
                    identifier += '|' + download.getSha256();
                } else {
                    ArtifactRef aref = new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
                    identifier = aref.toString();
                }

                logger.info("Recording download: {}", identifier);

                String originUrl = download.getOriginUrl();
                if (originUrl == null) {
                    // this is from a hosted repository, either shared-imports or a build, or something like that.
                    originUrl = download.getLocalUrl();
                }

                TargetRepository targetRepository = getDownloadsTargetRepository(download);

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(download.getMd5())
                        .sha1(download.getSha1())
                        .sha256(download.getSha256())
                        .size(download.getSize())
                        .deployPath(download.getPath())
                        .originUrl(originUrl)
                        .importDate(Date.from(Instant.now()))
                        .filename(new File(path).getName())
                        .identifier(identifier)
                        .targetRepository(targetRepository);

                Artifact artifact = validateArtifact(artifactBuilder.build());
                deps.add(artifact);
            }

            for (Map.Entry<StoreKey, Map<StoreKey, Set<String>>> targetToSources : toPromote.entrySet()) {
                StoreKey target = targetToSources.getKey();
                for (Map.Entry<StoreKey, Set<String>> sourceToPaths : targetToSources.getValue().entrySet()) {
                    StoreKey source = sourceToPaths.getKey();
                    PathsPromoteRequest req = new PathsPromoteRequest(source, target, sourceToPaths.getValue()).setPurgeSource(false);
                    // set read-only only the generic http proxy hosted repos, not shared-imports
                    doPromoteByPath(req, GENERIC_PKG_KEY.equals(target.getPackageType()));
                }
            }
        }

        return deps;
    }

    private TargetRepository getDownloadsTargetRepository(TrackedContentEntryDTO download) throws RepositoryManagerException {
        TargetRepository targetRepository;
        TargetRepository.Type repoType = toRepoType(download.getAccessChannel());
        if (repoType.equals(TargetRepository.Type.MAVEN)) {
            targetRepository = TargetRepository.newBuilder()
                    .identifier("indy-maven")
                    .repositoryType(repoType)
                    .repositoryPath(getMavenTargetRepositoryPath(download))
                    .temporaryRepo(false)
                    .build();
        } else if (repoType.equals(TargetRepository.Type.GENERIC_PROXY)) {
            StoreKey source = download.getStoreKey();
            targetRepository = TargetRepository.newBuilder()
                    .identifier("indy-http")
                    .repositoryType(repoType)
                    .repositoryPath(getGenericTargetRepositoryPath(source))
                    .temporaryRepo(false)
                    .build();
        } else {
            throw new RepositoryManagerException("Repository type " + repoType + " is not yet supported.");
        }
        return targetRepository;
    }

    private String getMavenTargetRepositoryPath(TrackedContentEntryDTO download) {
        String result;
        StoreKey sk = download.getStoreKey();
        if (isExternalOrigin(sk)) {
            result = "/api/content/maven/hosted/" + SHARED_IMPORTS_ID + "/";
        } else {
            String localUrl = download.getLocalUrl();
            String path = download.getPath();
            result = localUrl.substring(localUrl.indexOf("/api/content/maven/"), localUrl.indexOf(path) + 1);
        }
        return result;
    }

    /**
     * For a remote generic http repo computes matching hosted repo name.
     *
     * @param remoteName the remote repo name
     * @return computed hosted repo name
     */
    private String getGenericHostedRepoName(String remoteName) {
        String hostedName;
        if (remoteName.startsWith("r-")) {
            hostedName = "h-" + remoteName.substring(2);
        } else {
            logger.warn("Unexpected generic http remote repo name {}. Using it for hosted repo "
                    + "without change, but it probably doesn't exist.", remoteName);
            hostedName = remoteName;
        }
        return hostedName;
    }

    private String getGenericTargetRepositoryPath(StoreKey source) {
        return "/api/content/generic-http/" + getGenericHostedRepoName(source.getName());
    }

    private TargetRepository getUploadsTargetRepository(TargetRepository.Type repoType) throws RepositoryManagerException {
        TargetRepository targetRepository;
        if (repoType.equals(TargetRepository.Type.MAVEN)) {
            String storePath;
            storePath = "group/" + buildPromotionTarget;
            targetRepository = TargetRepository.newBuilder()
                    .identifier("indy-maven")
                    .repositoryType(TargetRepository.Type.MAVEN)
                    .repositoryPath("/api/content/maven/" + storePath)
                    .temporaryRepo(isTempBuild)
                    .build();
        } else {
            throw new RepositoryManagerException("Repository type " + repoType + " is not yet supported.");
        }
        return targetRepository;
    }

    private boolean isExternalOrigin(StoreKey sk) {
        if (sk.getType() == StoreType.hosted) {
            return false;
        } else {
            String repoName = sk.getName();
            for (String pattern : internalRepoPatterns) {
//                Logger logger = LoggerFactory.getLogger(getClass());
//                logger.info( "Checking ")
                if (pattern.equals(repoName)) {
                    return false;
                }

                if (repoName.matches(pattern)) {
                    return false;
                }
            }

            return true;
        }
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

                String identifier;
                if (pathInfo == null) {
                    identifier = upload.getOriginUrl();
                    if (identifier == null) {
                        // this is to a hosted repository, either the build repo or something like that.
                        identifier = upload.getLocalUrl();
                    }
                    identifier += '|' + upload.getSha256();
                } else {
                    ArtifactRef aref = new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
                    identifier = aref.toString();
                }

                logger.info("Recording upload: {}", identifier);

                TargetRepository.Type repoType = toRepoType(upload.getAccessChannel());
                TargetRepository targetRepository = getUploadsTargetRepository(repoType);

                Artifact.Quality artifactQuality = getArtifactQuality(isTempBuild);
                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(upload.getMd5())
                        .sha1(upload.getSha1())
                        .sha256(upload.getSha256())
                        .size(upload.getSize())
                        .deployPath(upload.getPath())
                        .filename(new File(path).getName())
                        .identifier(identifier)
                        .targetRepository(targetRepository)
                        .artifactQuality(artifactQuality);

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
     * @param setReadonly flag telling if the target repo should be set to readOnly
     * @throws RepositoryManagerException When either the client API throws an exception due to something unexpected in
     *         transport, or if the promotion process results in an error.
     */
    private void doPromoteByPath(PathsPromoteRequest req, boolean setReadonly) throws RepositoryManagerException {
        IndyPromoteClientModule promoter;
        try {
            promoter = serviceAccountIndy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy promote client module. Reason: %s", e, e.getMessage());
        }

        try {
            PathsPromoteResult result = promoter.promoteByPath(req);
            if (result.getError() == null) {
                if (setReadonly && !isTempBuild) {
                    HostedRepository hosted = serviceAccountIndy.stores().load(req.getTarget(), HostedRepository.class);
                    hosted.setReadonly(true);
                    try {
                        serviceAccountIndy.stores().update(hosted, "Setting readonly after successful build and promotion.");
                    } catch (IndyClientException ex) {
                        try {
                            promoter.rollbackPathPromote(result);
                        } catch (IndyClientException ex2) {
                            logger.error("Failed to set readonly flag on repo: %s. Reason given was: %s.", ex, req.getTarget(), ex.getMessage());
                            throw new RepositoryManagerException(
                                    "Subsequently also failed to rollback the promotion of paths from %s to %s. Reason given was: %s",
                                    ex2, req.getSource(), req.getTarget(), ex2.getMessage());
                        }
                        throw new RepositoryManagerException("Failed to set readonly flag on repo: %s. Reason given was: %s",
                                ex, req.getTarget(), ex.getMessage());
                    }
                }
            } else {
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
     * Promote the build output to the target build group (using group promotion, where the build
     * repo is added to the group's membership) and marks the build output as readonly.
     *
     * @param uploads artifacts to be promoted
     */
    public void promoteToBuildContentSet(List<Artifact> uploads) throws RepositoryManagerException {
        IndyPromoteClientModule promoter;
        try {
            promoter = serviceAccountIndy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy promote client module. Reason: %s", e, e.getMessage());
        }

        StoreKey buildRepoKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildContentId);
        GroupPromoteRequest request = new GroupPromoteRequest(buildRepoKey, buildPromotionTarget);
        try {
            GroupPromoteResult result = promoter.promoteToGroup(request);
            if (result.succeeded()) {
                if (!isTempBuild) {
                    HostedRepository hosted = serviceAccountIndy.stores().load(buildRepoKey, HostedRepository.class);
                    hosted.setReadonly(true);
                    try {
                        serviceAccountIndy.stores().update(hosted, "Setting readonly after successful build and promotion.");
                    } catch (IndyClientException ex) {
                        try {
                            promoter.rollbackGroupPromote(request);
                        } catch (IndyClientException ex2) {
                            logger.error("Failed to set readonly flag on repo: %s. Reason given was: %s.", ex, buildRepoKey, ex.getMessage());
                            throw new RepositoryManagerException(
                                    "Subsequently also failed to rollback the promotion of repo: %s to group: %s. Reason given was: %s",
                                    ex2, request.getSource(), request.getTargetGroup(), ex2.getMessage());
                        }
                        throw new RepositoryManagerException("Failed to set readonly flag on repo: %s. Reason given was: %s",
                            ex, buildRepoKey, ex.getMessage());
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder();
                if (result.getError() != null) {
                    sb.append(result.getError()).append("\n");
                }
                ValidationResult validations = result.getValidations();
                if (validations != null) {
                    sb.append("One or more validation rules failed in rule-set ").append(validations.getRuleSet()).append(":\n");

                    validations.getValidatorErrors().forEach((rule, error) -> {
                        sb.append("- ").append(rule).append(":\n").append(error).append("\n\n");
                    });
                }
                String reason = sb.toString();

                throw new RepositoryManagerException("Failed to promote: %s to group: %s. Reason given was: %s",
                        request.getSource(), request.getTargetGroup(), reason);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, request, e.getMessage());
        }
    }

    private boolean ignoreContent(String path) {
        for (String suffix : ignoredPathSuffixes) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    private TargetRepository.Type toRepoType(AccessChannel accessChannel) {
        switch (accessChannel) {
            case MAVEN_REPO:
                return TargetRepository.Type.MAVEN;
            case GENERIC_PROXY:
                return TargetRepository.Type.GENERIC_PROXY;
            default:
                return TargetRepository.Type.GENERIC_PROXY;
        }
    }

    private Artifact.Quality getArtifactQuality(boolean isTempBuild) {
        if (isTempBuild) {
            return Artifact.Quality.TEMPORARY;
        } else {
            return Artifact.Quality.NEW;
        }
    }

}
