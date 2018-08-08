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

import org.apache.commons.lang.StringUtils;
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
import org.commonjava.indy.promote.model.GroupPromoteRequest;
import org.commonjava.indy.promote.model.GroupPromoteResult;
import org.commonjava.indy.promote.model.PathsPromoteRequest;
import org.commonjava.indy.promote.model.PathsPromoteResult;
import org.commonjava.indy.promote.model.ValidationResult;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.util.ArtifactPathInfo;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.IgnoredPathSuffixes;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig.InternalRepoPatterns;
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
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.TEMPORARY_BUILDS_GROUP;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.UNTESTED_BUILDS_GROUP;

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
public class IndyRepositorySession implements RepositorySession {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-executor");

    /** PackageType-specific ignored suffixes. */
    private IgnoredPathSuffixes ignoredPathSuffixes;

    private boolean isTempBuild;

    private Indy indy;
    private Indy serviceAccountIndy;
    private final String buildContentId;
    private final String packageKey;
    /**
     * PackageType-specific internal repository name patterns.
     */
    private InternalRepoPatterns internalRepoPatterns;

    private final RepositoryConnectionInfo connectionInfo;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private String buildPromotionGroup;

    public IndyRepositorySession(Indy indy, Indy serviceAccountIndy, String buildContentId, String packageKey,
            IndyRepositoryConnectionInfo info, InternalRepoPatterns internalRepoPatterns,
            IgnoredPathSuffixes ignoredPathSuffixes, String buildPromotionGroup, boolean isTempBuild) {
        this.indy = indy;
        this.serviceAccountIndy = serviceAccountIndy;
        this.buildContentId = buildContentId;
        this.packageKey = packageKey;
        this.internalRepoPatterns = internalRepoPatterns;
        this.ignoredPathSuffixes = ignoredPathSuffixes;
        this.connectionInfo = info;
        this.buildPromotionGroup = buildPromotionGroup;
        this.isTempBuild = isTempBuild; //TODO define based on buildPromotionGroup
    }

    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }

    @Override
    public TargetRepository.Type getType() {
        return toRepoType(packageKey);
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
            StoreKey key = new StoreKey(packageKey, StoreType.group, buildContentId);
            serviceAccountIndy.stores().delete(key, "[Post-Build] Removing build aggregation group: " + buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy stores module. Reason: %s", e, e.getMessage());
        }

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Returning built artifacts / dependencies:\nUploads:\n  {}\n\nDownloads:\n  {}\n\n",
                StringUtils.join(uploads, "\n  "), StringUtils.join(downloads, "\n  "));

        String log = "";
        CompletionStatus status = CompletionStatus.SUCCESS;
        try {
            promoteToBuildContentSet();
        } catch (RepositoryManagerException rme) {
            status = CompletionStatus.FAILED;
            log = rme.getMessage();
            logger.error("Promotion validation error(s): \n" + log);
            userLog.error("Artifact promotion failed. Promotion validation error(s): {}", log);
        }

        return new IndyRepositoryManagerResult(uploads, downloads, buildContentId, log, status);
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
            throw new RepositoryManagerException("Failed to retrieve Indy client module. Reason: %s", e, e.getMessage());
        }

        List<Artifact> deps = new ArrayList<>();

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        if (downloads != null) {
            Map<StoreKey, Set<String>> toPromote = new HashMap<>();

            for (TrackedContentEntryDTO download : downloads) {
                String path = download.getPath();
                StoreKey storeKey = download.getStoreKey();
                String packageType = storeKey.getPackageType();
                if (ignoreContent(packageType, path)) {
                    logger.debug("Ignoring download (matched in ignored-suffixes): {} (From: {})", download.getPath(), storeKey);
                    continue;
                }

                // If the entry is from a hosted repository, it shouldn't be auto-promoted.
                // If the entry is already in shared-imports, it shouldn't be auto-promoted to there.
                // New binary imports will be coming from a remote repository...
                if (isExternalOrigin(storeKey) && StoreType.hosted != storeKey.getType()) {
                    if (MAVEN_PKG_KEY.equals(packageType) || NPM_PKG_KEY.equals(packageType)) {
                        // this has not been captured, so promote it.
                        Set<String> paths = toPromote.get(storeKey);
                        if (paths == null) {
                            paths = new HashSet<>();
                            toPromote.put(storeKey, paths);
                        }

                        paths.add(path);
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

                TargetRepository.Type repoType = toRepoType(packageType);
                TargetRepository targetRepository = getDownloadsTargetRepository(repoType, content);

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

            Map<String, StoreKey> promotionTargets = new HashMap<>();
            for (Map.Entry<StoreKey, Set<String>> entry : toPromote.entrySet()) {
                StoreKey source = entry.getKey();
                StoreKey target = getPromotionTarget(source.getPackageType(), promotionTargets);
                PathsPromoteRequest req = new PathsPromoteRequest(source, target, entry.getValue()).setPurgeSource(false);
                doPromoteByPath(req);
            }
        }

        return deps;
    }

    private StoreKey getPromotionTarget(String packageType, Map<String, StoreKey> promotionTargets) {
        if (!promotionTargets.containsKey(packageType)) {
            StoreKey storeKey = new StoreKey(packageType, StoreType.hosted, IndyRepositoryConstants.SHARED_IMPORTS_ID);
            promotionTargets.put(packageType, storeKey);
        }
        return promotionTargets.get(packageType);
    }

    private TargetRepository getDownloadsTargetRepository(TargetRepository.Type repoType, IndyContentClientModule content)
            throws RepositoryManagerException {
        String identifier;
        String repoPath;
        if (repoType == TargetRepository.Type.MAVEN) {
                identifier = "indy-maven";
                repoPath = content.contentPath(new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, IndyRepositoryConstants.SHARED_IMPORTS_ID));
        } else if (repoType == TargetRepository.Type.NPM) {
                identifier = "indy-npm";
                repoPath = content.contentPath(new StoreKey(NPM_PKG_KEY, StoreType.hosted, IndyRepositoryConstants.SHARED_IMPORTS_ID));
        } else if (repoType == TargetRepository.Type.GENERIC_PROXY) {
                identifier = "indy-http";
                repoPath = "/not-available/"; //TODO set the path for http cache
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

    private TargetRepository getUploadsTargetRepository(TargetRepository.Type repoType,
            IndyContentClientModule content) throws RepositoryManagerException {
        String groupName = (isTempBuild ? TEMPORARY_BUILDS_GROUP : UNTESTED_BUILDS_GROUP);
        StoreKey storeKey;
        String identifier;
        if (repoType == TargetRepository.Type.MAVEN) {
            storeKey = new StoreKey(MAVEN_PKG_KEY, StoreType.group, groupName);
            identifier = "indy-maven";
        } else if (repoType == TargetRepository.Type.NPM) {
            storeKey = new StoreKey(NPM_PKG_KEY, StoreType.group, groupName);
            identifier = "indy-npm";
        } else {
            throw new RepositoryManagerException("Repository type " + repoType
                    + " is not supported for uploads by Indy repo manager driver.");
        }

        String repoPath = content.contentPath(storeKey);
        return TargetRepository.newBuilder()
                .identifier(identifier)
                .repositoryType(repoType)
                .repositoryPath(repoPath)
                .temporaryRepo(isTempBuild)
                .build();
    }

    private boolean isExternalOrigin(StoreKey storeKey) {
        String repoName = storeKey.getName();
        List<String> patterns;
        switch (storeKey.getPackageType()) {
            case MAVEN_PKG_KEY:
                patterns = internalRepoPatterns.getMaven();
                break;
            case NPM_PKG_KEY:
                patterns = internalRepoPatterns.getNpm();
                break;
            default:
                throw new IllegalArgumentException("Package type " + storeKey.getPackageType()
                        + " is not supported by Indy repository manager driver.");
        }

        for (String pattern : patterns) {
//            Logger logger = LoggerFactory.getLogger(getClass());
//            logger.info( "Checking ")
            if (pattern.equals(repoName)) {
                return false;
            }

            if (repoName.matches(pattern)) {
                return false;
            }
        }

        return true;
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
                StoreKey storeKey = upload.getStoreKey();
                if (ignoreContent(storeKey.getPackageType(), path)) {
                    logger.debug("Ignoring upload (matched in ignored-suffixes): {} (From: {})", path, storeKey);
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

                IndyContentClientModule content;
                try {
                    content = indy.content();
                } catch (IndyClientException e) {
                    throw new RepositoryManagerException("Failed to retrieve Indy content module. Reason: %s", e, e.getMessage());
                }

                TargetRepository.Type repoType = toRepoType(storeKey.getPackageType());
                TargetRepository targetRepository = getUploadsTargetRepository(repoType, content);

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
     * Promotes a set of artifact paths (or everything, if the path-set is missing) from a particular Indy artifact store to
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
            promoter = serviceAccountIndy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy client module. Reason: %s", e, e.getMessage());
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
     * Promote the build output to the correct build group (using group promotion, where the build repo is added to the
     * group's membership) and marks the build output as readonly.
     */
    public void promoteToBuildContentSet() throws RepositoryManagerException {
        IndyPromoteClientModule promoter;
        try {
            promoter = serviceAccountIndy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to retrieve Indy client module. Reason: %s", e, e.getMessage());
        }

        StoreKey hostedKey = new StoreKey(packageKey, StoreType.hosted, buildContentId);
        GroupPromoteRequest request = new GroupPromoteRequest(hostedKey, buildPromotionGroup);
        try {
            GroupPromoteResult result = promoter.promoteToGroup(request);
            if (result.succeeded()) {
                if (!isTempBuild) {
                    HostedRepository hosted = serviceAccountIndy.stores().load(hostedKey, HostedRepository.class);
                    hosted.setReadonly(true);
                    try {
                        serviceAccountIndy.stores().update(hosted, "Setting readonly after successful build and promotion.");
                    } catch (IndyClientException ex) {
                        try {
                            promoter.rollbackGroupPromote(request);
                        } catch (IndyClientException ex2) {
                            logger.error("Failed to set readonly flag on repo: %s. Reason given was: %s.", ex, hostedKey, ex.getMessage());
                            throw new RepositoryManagerException(
                                    "Subsequently also failed to rollback the promotion of repo: %s to group: %s. Reason given was: %s",
                                    ex2, request.getSource(), request.getTargetGroup(), ex2.getMessage());
                        }
                        throw new RepositoryManagerException("Failed to set readonly flag on repo: %s. Reason given was: %s",
                                ex, hostedKey, ex.getMessage());
                    }
                }
            } else {
                String reason = result.getError();
                if (reason == null) {
                    ValidationResult validations = result.getValidations();
                    if (validations != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("One or more validation rules failed in rule-set ").append(validations.getRuleSet()).append(":\n");

                        validations.getValidatorErrors().forEach((rule, error) -> {
                            sb.append("- ").append(rule).append(":\n").append(error).append("\n\n");
                        });

                        reason = sb.toString();
                    }
                }

                throw new RepositoryManagerException("Failed to promote: %s to group: %s. Reason given was: %s",
                        request.getSource(), request.getTargetGroup(), reason);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, request, e.getMessage());
        }
    }

    private boolean ignoreContent(String packageType, String path) {
        List<String> suffixes;
        switch (packageType) {
            case MAVEN_PKG_KEY:
                suffixes = ignoredPathSuffixes.getMavenWithShared();
                break;
            case NPM_PKG_KEY:
                suffixes = ignoredPathSuffixes.getNpmWithShared();
                break;
            case GENERIC_PKG_KEY:
                suffixes = ignoredPathSuffixes.getShared();
                break;
            default:
                throw new IllegalArgumentException("Package type " + packageType
                        + " is not supported by Indy repository manager driver.");
        }

        for (String suffix : suffixes) {
            if (path.endsWith(suffix)) {
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

}
