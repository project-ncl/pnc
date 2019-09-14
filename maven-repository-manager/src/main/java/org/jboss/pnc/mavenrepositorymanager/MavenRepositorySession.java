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
import org.commonjava.atlas.maven.ident.ref.ArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.util.ArtifactPathInfo;
import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.commonjava.atlas.npm.ident.util.NpmPackagePathInfo;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.module.IndyContentClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.folo.dto.TrackedContentDTO;
import org.commonjava.indy.folo.dto.TrackedContentEntryDTO;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.commonjava.indy.promote.model.AbstractPromoteResult;
import org.commonjava.indy.promote.model.GroupPromoteRequest;
import org.commonjava.indy.promote.model.GroupPromoteResult;
import org.commonjava.indy.promote.model.PathsPromoteRequest;
import org.commonjava.indy.promote.model.PathsPromoteResult;
import org.commonjava.indy.promote.model.PromoteRequest;
import org.commonjava.indy.promote.model.ValidationResult;
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
import static org.commonjava.indy.pkg.npm.model.NPMPackageTypeDescriptor.NPM_PKG_KEY;
import static org.jboss.pnc.mavenrepositorymanager.MavenRepositoryConstants.SHARED_IMPORTS_ID;

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

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private String buildPromotionTarget;

    // TODO: Create and pass in suitable parameters to Indy to create the
    //       proxy repository.
    @Deprecated
    public MavenRepositorySession(Indy indy, String buildContentId, boolean isSetBuild,
                                  MavenRepositoryConnectionInfo info) {
        this.indy = indy;
        this.buildContentId = buildContentId;
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
            throw new RepositoryManagerException("Failed to retrieve Indy content module. Reason: %s", e, e.getMessage());
        }

        List<Artifact> deps = new ArrayList<>();

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        if (downloads != null) {
            Map<StoreKey, Map<StoreKey, Set<String>>> toPromote = new HashMap<>();

            Map<String, StoreKey> promotionTargets = new HashMap<>();
            for (TrackedContentEntryDTO download : downloads) {
                String path = download.getPath();
                StoreKey source = download.getStoreKey();
                if (ignoreContent(source, path)) {
                    logger.debug("Ignoring download (matched in ignored-suffixes): {} (From: {})", path, source);
                    continue;
                }

                // If the entry is from a hosted repository (also shared-imports), it shouldn't be auto-promoted.
                // New binary imports will be coming from a remote repository...
                if (isExternalOrigin(source)) {
                    StoreKey target = null;
                    Map<StoreKey, Set<String>> sources = null;
                    Set<String> paths = null;

                    String packageType = source.getPackageType();
                    // this has not been captured, so promote it.
                    switch (packageType) {
                        case MAVEN_PKG_KEY:
                        case NPM_PKG_KEY:
                            target = getPromotionTarget(packageType, promotionTargets);
                            sources = toPromote.computeIfAbsent(target, t -> new HashMap<>());
                            paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                            if (MAVEN_PKG_KEY.equals(packageType)) {
                                paths.add(path);
                                paths.add(path + ".md5");
                                paths.add(path + ".sha1");
                            } else if (NPM_PKG_KEY.equals(packageType)) {
                                if (path.matches("^/(?:@[^/]+/)?([^/]+)/-/\\1-.+\\.tgz$")) {
                                    paths.add(path);
                                } else if (path.matches("^/(?:@[^/]+/)?[^/]+$")) {
                                    // skip metadata
                                } else {
                                    logger.warn("Unrecognized NPM download path: {}", path);
                                    paths.add(path);
                                }
                            }
                            break;

                        case GENERIC_PKG_KEY:
                            String remoteName = source.getName();
                            String hostedName = getGenericHostedRepoName(remoteName);
                            target = new StoreKey(packageType, StoreType.hosted, hostedName);
                            sources = toPromote.computeIfAbsent(target, t -> new HashMap<>());
                            paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                            paths.add(path);
                            break;

                        default:
                            // do not promote anything else anywhere
                            break;
                    }
                }

                String identifier = computeIdentifier(download);

                logger.info("Recording download: {}", identifier);

                String originUrl = download.getOriginUrl();
                if (originUrl == null) {
                    // this is from a hosted repository, either shared-imports or a build, or something like that.
                    originUrl = download.getLocalUrl();
                }

                TargetRepository targetRepository = getDownloadsTargetRepository(download, content);

                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(download.getMd5())
                        .sha1(download.getSha1())
                        .sha256(download.getSha256())
                        .size(download.getSize())
                        .deployPath(path)
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

    private StoreKey getPromotionTarget(String packageType, Map<String, StoreKey> promotionTargets) {
        if (!promotionTargets.containsKey(packageType)) {
            StoreKey storeKey = new StoreKey(packageType, StoreType.hosted, SHARED_IMPORTS_ID);
            promotionTargets.put(packageType, storeKey);
        }
        return promotionTargets.get(packageType);
    }

    private TargetRepository getDownloadsTargetRepository(TrackedContentEntryDTO download, IndyContentClientModule content) throws RepositoryManagerException {
        String identifier;
        String repoPath;
        StoreKey source = download.getStoreKey();
        TargetRepository.Type repoType = toRepoType(source.getPackageType());
        if (repoType == TargetRepository.Type.MAVEN || repoType == TargetRepository.Type.NPM) {
            identifier = "indy-" + repoType.name().toLowerCase();
            repoPath = getTargetRepositoryPath(download, content);
        } else if (repoType == TargetRepository.Type.GENERIC_PROXY) {
            identifier = "indy-http";
            repoPath = getGenericTargetRepositoryPath(source);
        } else {
            throw new RepositoryManagerException("Repository type " + repoType
                    + " is not supported by Indy repo manager driver.");
        }

        return TargetRepository.newBuilder()
                .identifier(identifier)
                .repositoryType(repoType)
                .repositoryPath(repoPath)
                .temporaryRepo(false)
                .build();
    }

    private String getTargetRepositoryPath(TrackedContentEntryDTO download, IndyContentClientModule content) {
        String result;
        StoreKey sk = download.getStoreKey();
        String packageType = sk.getPackageType();
        if (isExternalOrigin(sk)) {
            result = "/api/" + content.contentPath(new StoreKey(packageType, StoreType.hosted, SHARED_IMPORTS_ID));
        } else {
            result = "/api/" + content.contentPath(sk);
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
        return "/api/content/generic-http/hosted/" + getGenericHostedRepoName(source.getName());
    }

    private TargetRepository getUploadsTargetRepository(TrackedContentEntryDTO upload,
            IndyContentClientModule content) throws RepositoryManagerException {
        StoreKey storeKey = upload.getStoreKey();
        String pkgType = storeKey.getPackageType();
        TargetRepository.Type repoType = toRepoType(pkgType);
        StoreKey targetKey;
        if (repoType == TargetRepository.Type.MAVEN) {
            targetKey = new StoreKey(pkgType, isTempBuild ? StoreType.group : StoreType.hosted, buildPromotionTarget);
        } else if (repoType == TargetRepository.Type.NPM) {
            targetKey = storeKey;
        } else {
            throw new RepositoryManagerException("Repository type " + repoType
                    + " is not supported for uploads by Indy repo manager driver.");
        }

        String repoPath = "/api/" + content.contentPath(targetKey);
        TargetRepository targetRepository = TargetRepository.newBuilder()
                .identifier("indy-" + repoType.name().toLowerCase())
                .repositoryType(repoType)
                .repositoryPath(repoPath)
                .temporaryRepo((repoType == TargetRepository.Type.MAVEN) && isTempBuild)
                .build();
        return targetRepository;
    }

    private boolean isExternalOrigin(StoreKey storeKey) {
        if (storeKey.getType() == StoreType.hosted) {
            return false;
        } else {
            String repoName = storeKey.getName();
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

            IndyContentClientModule content;
            try {
                content = indy.content();
            } catch (IndyClientException e) {
                throw new RepositoryManagerException("Failed to retrieve Indy content module. Reason: %s", e, e.getMessage());
            }

            for (TrackedContentEntryDTO upload : uploads) {
                String path = upload.getPath();
                StoreKey storeKey = upload.getStoreKey();
                if (ignoreContent(storeKey, path)) {
                    logger.debug("Ignoring upload (matched in ignored-suffixes): {} (From: {})", path, storeKey);
                    continue;
                }

                String identifier = computeIdentifier(upload);

                logger.info("Recording upload: {}", identifier);

                TargetRepository targetRepository = getUploadsTargetRepository(upload, content);

                Artifact.Quality artifactQuality = getArtifactQuality(isTempBuild);
                Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                        .md5(upload.getMd5())
                        .sha1(upload.getSha1())
                        .sha256(upload.getSha256())
                        .size(upload.getSize())
                        .deployPath(path)
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
     * Computes identifier string for an artifact. If the download path is valid for a package-type specific artifact it
     * creates the identifier accordingly.
     *
     * @param transfer the download or upload that we want to generate identifier for
     * @return generated identifier
     */
    private String computeIdentifier(final TrackedContentEntryDTO transfer) {
        String identifier = null;

        switch (transfer.getStoreKey().getPackageType()) {
            case MAVEN_PKG_KEY:
                ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(transfer.getPath());
                if (pathInfo != null) {
                    ArtifactRef aref = new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
                    identifier = aref.toString();
                }
                break;

            case NPM_PKG_KEY:
                NpmPackagePathInfo npmPathInfo = NpmPackagePathInfo.parse(transfer.getPath());
                if (npmPathInfo != null) {
                    NpmPackageRef packageRef = new NpmPackageRef(npmPathInfo.getName(), npmPathInfo.getVersion());
                    identifier = packageRef.toString();
                }
                break;

            case GENERIC_PKG_KEY:
                // handle generic downloads along with other invalid download paths for other package types
                break;

            default:
                // do not do anything by default
                logger.warn("Package type {} is not handled by Indy repository session.", transfer.getStoreKey().getPackageType());
                break;
        }

        if (identifier == null) {
            identifier = computeGenericIdentifier(transfer.getOriginUrl(), transfer.getLocalUrl(), transfer.getSha256());
        }

        return identifier;
    }

    /**
     * Compute the identifier string for a generic download, that does not match package type specific files structure.
     * It prefers to use the origin URL if it is not empty. In case it is then it uses local URL, which can never be
     * empty, it is the local file mirror in Indy. After that it attaches the sha256 separated by a pipe.
     *
     * @param originUrl the origin URL of the transfer, it can be null
     * @param localUrl url where the artifact was backed up in Indy
     * @param sha256 the SHA-256 of the transfer
     * @return the generated identifier
     */
    private String computeGenericIdentifier(String originUrl, String localUrl, String sha256) {
        String identifier = originUrl;
        if (identifier == null) {
            // this is from/to a hosted repository, either the build repo or something like that.
            identifier = localUrl;
        }
        identifier += '|' + sha256;
        return identifier;
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
     * Promotes a set of artifact paths (or everything, if the path-set is missing) from a particular Indy artifact store to
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
            if (result.succeeded()) {
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
                String error = getValidationError(result);
                throw new RepositoryManagerException("Failed to promote: %s. Reason given was: %s", req, error);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, req, e.getMessage());
        }
    }


    /**
     * Promote the build output to the consolidated build repo (using path promotion, where the build
     * repo contents are added to the repo's contents) and marks the build output as readonly.
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
        PromoteRequest<?> request = null;
        try {
            if (isTempBuild) {
                request = new GroupPromoteRequest(buildRepoKey, buildPromotionTarget);
                GroupPromoteRequest gpReq = (GroupPromoteRequest) request;
                GroupPromoteResult result = promoter.promoteToGroup(gpReq);
                if (!result.succeeded()) {
                    String reason = getValidationError(result);
                    throw new RepositoryManagerException("Failed to promote: %s to group: %s. Reason given was: %s",
                            request.getSource(), gpReq.getTargetGroup(), reason);
                }
            } else {
                StoreKey buildTarget = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildPromotionTarget);
                Set<String> paths = new HashSet<>();
                for (Artifact a : uploads) {
                    if (a.getTargetRepository().getRepositoryType() == TargetRepository.Type.MAVEN) {
                        paths.add(a.getDeployPath());
                        paths.add(a.getDeployPath() + ".md5");
                        paths.add(a.getDeployPath() + ".sha1");
                    }
                }
                request = new PathsPromoteRequest(buildRepoKey, buildTarget, paths);
                PathsPromoteRequest ppReq = (PathsPromoteRequest) request;

                PathsPromoteResult result = promoter.promoteByPath(ppReq);
                if (result.succeeded()) {
                    HostedRepository buildRepo = serviceAccountIndy.stores().load(buildRepoKey, HostedRepository.class);
                    buildRepo.setReadonly(true);
                    try {
                        serviceAccountIndy.stores().update(buildRepo,
                                "Setting readonly after successful build and promotion.");
                    } catch (IndyClientException ex) {
                        logger.error("Failed to set readonly flag on repo: %s. Reason given was: %s."
                                + " But the promotion to consolidated repo %s succeeded.", ex, buildRepoKey,
                                ex.getMessage(), buildPromotionTarget);
                    }
                } else {
                    String reason = getValidationError(result);
                    throw new RepositoryManagerException("Failed to promote files from %s to target %s. Reason given was: %s",
                            request.getSource(), ppReq.getTarget(), reason);
                }
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, request, e.getMessage());
        }
    }

    /**
     * Computes error message from a failed promotion result. It means either error must not be empty
     * or validations need to contain at least 1 validation error.
     *
     * @param result the promotion result
     * @return the error message
     */
    private String getValidationError(AbstractPromoteResult<?> result) {
        StringBuilder sb = new StringBuilder();
        String errorMsg = result.getError();
        ValidationResult validations = result.getValidations();
        if (errorMsg != null) {
            sb.append(errorMsg);
            if (validations != null) {
                sb.append("\n");
            }
        }
        if (validations != null) {
            sb.append("One or more validation rules failed in rule-set ").append(validations.getRuleSet()).append(":\n");

            validations.getValidatorErrors().forEach((rule, error) -> {
                sb.append("- ").append(rule).append(":\n").append(error).append("\n\n");
            });
        }
        if (sb.length() == 0) {
            sb.append("(no error message received)");
        }
        return sb.toString();
    }

    private boolean ignoreContent(StoreKey source, String path) {
        for (String suffix : ignoredPathSuffixes) {
            if (path.endsWith(suffix)) {
                return true;
            } else if (NPM_PKG_KEY.equals(source.getPackageType()) && path.matches("^/(?:@[^/]+/)?[^/]+$")) {
                return true;
            }
        }

        return false;
    }

    private TargetRepository.Type toRepoType(String packageType) {
        switch (packageType) {
            case MAVEN_PKG_KEY:
                return TargetRepository.Type.MAVEN;
            case NPM_PKG_KEY:
                return TargetRepository.Type.NPM;
            case GENERIC_PKG_KEY:
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

    @Override
    public void close() {
        IOUtils.closeQuietly(indy);
        IOUtils.closeQuietly(serviceAccountIndy);
    }

}
