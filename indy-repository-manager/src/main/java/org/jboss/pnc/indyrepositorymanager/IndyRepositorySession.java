/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import org.commonjava.indy.promote.model.PathsPromoteRequest;
import org.commonjava.indy.promote.model.PathsPromoteResult;
import org.commonjava.indy.promote.model.ValidationResult;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.commonjava.indy.model.core.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.commonjava.indy.pkg.npm.model.NPMPackageTypeDescriptor.NPM_PKG_KEY;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.SHARED_IMPORTS_ID;

/**
 * {@link RepositorySession} implementation that works with the Maven {@link RepositoryManagerDriver} (which connects to
 * an Indy server instance for repository management). This session contains connection information for rendering Maven
 * settings.xml files and the like, along with the components necessary to extract the artifacts (dependencies, build
 * uploads) for the associated build.
 *
 * Artifact extraction also implies promotion of imported dependencies to a shared-imports Maven repository for safe
 * keeping. In the case of composed (chained) builds, it also implies promotion of the build output to the associated
 * build-set repository group, to expose them for use in successive builds in the chain.
 */
public class IndyRepositorySession implements RepositorySession {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-executor");

    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private boolean isTempBuild;

    private Indy indy;
    private Indy serviceAccountIndy;
    private final String buildContentId;
    private final String packageType;
    /**
     * PackageType-specific internal repository name patterns.
     */
    private ArtifactFilter artifactFilter;

    private final RepositoryConnectionInfo connectionInfo;

    private final Validator validator;

    private String buildPromotionTarget;

    private BuildCategory buildCategory;

    private static Set<String> checksumSuffixes;
    static {
        checksumSuffixes = new HashSet<>(4);
        checksumSuffixes.add("md5");
        checksumSuffixes.add("sha1");
        checksumSuffixes.add("sha256");
        checksumSuffixes.add("sha512");
    }

    public IndyRepositorySession(
            Indy indy,
            Indy serviceAccountIndy,
            String buildContentId,
            String packageType,
            IndyRepositoryConnectionInfo info,
            ArtifactFilter artifactFilter,
            String buildPromotionTarget,
            BuildCategory buildCategory,
            boolean isTempBuild) {
        this.validator = validatorFactory.getValidator();
        this.indy = indy;
        this.serviceAccountIndy = serviceAccountIndy;
        this.buildContentId = buildContentId;
        this.packageType = packageType;
        this.artifactFilter = artifactFilter;
        this.connectionInfo = info;
        this.buildPromotionTarget = buildPromotionTarget;
        this.buildCategory = buildCategory;
        this.isTempBuild = isTempBuild;
    }

    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }

    @Override
    public RepositoryType getType() {
        return toRepoType(packageType);
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
     * Retrieve tracking report from repository manager. Add each tracked download to the dependencies of the build
     * result. Add each tracked upload to the built artifacts of the build result. Promote uploaded artifacts to the
     * product-level storage. Finally delete the group associated with the completed build.
     *
     * @param liveBuild {@inheritDoc} - if true, the promotion of the collected artifacts and dependencies is done,
     *        tracking report is sealed and the build group is removed, if false it only reads the data without any
     *        actions
     */
    @Override
    public RepositoryManagerResult extractBuildArtifacts(final boolean liveBuild) throws RepositoryManagerException {

        TrackedContentDTO report = sealAndGetTrackingReport(liveBuild);

        Comparator<Artifact> comp = (one, two) -> one.getIdentifier().compareTo(two.getIdentifier());

        Uploads uploads = collectUploads(report);
        List<Artifact> uploadedArtifacts = uploads.getData();
        Collections.sort(uploadedArtifacts, comp);

        List<Artifact> downloadedArtifacts = null;
        String log = "";
        CompletionStatus status = CompletionStatus.SUCCESS;

        try {
            downloadedArtifacts = processDownloads(report, liveBuild);
            Collections.sort(downloadedArtifacts, comp);
        } catch (PromotionValidationException ex) {
            status = CompletionStatus.FAILED;
            log = ex.getMessage();
            logger.warn("Dependencies promotion failed. Error(s): {}", log);
            userLog.error("Dependencies promotion failed. Error(s): {}", log);
        }

        if (liveBuild) {
            deleteBuildGroup();
        }

        // if the promotion of dependencies succeeded...
        if (status == CompletionStatus.SUCCESS) {
            logger.info(
                    "Returning built artifacts / dependencies:\nUploads:\n  {}\n\nDownloads:\n  {}\n\n",
                    StringUtils.join(uploads.getData(), "\n  "),
                    StringUtils.join(downloadedArtifacts, "\n  "));

            if (liveBuild) {
                logger.info("BEGIN: promotion to build content set");
                StopWatch stopWatch = StopWatch.createStarted();

                try {
                    promoteToBuildContentSet(uploads.getPromotion());
                } catch (PromotionValidationException ex) {
                    status = CompletionStatus.FAILED;
                    log = ex.getMessage();
                    logger.warn("Built artifact promotion failed. Error(s): {}", log);
                    userLog.error("Built artifact promotion failed. Error(s): {}", log);
                }

                logger.info(
                        "END: promotion to build content set, took: {} seconds",
                        stopWatch.getTime(TimeUnit.SECONDS));
                stopWatch.reset();
            }
        }

        if (status == CompletionStatus.FAILED) {
            // prevent saving artifacts and dependencies to a failed build
            downloadedArtifacts = Collections.emptyList();
            uploadedArtifacts = Collections.emptyList();
        }

        return new IndyRepositoryManagerResult(uploadedArtifacts, downloadedArtifacts, buildContentId, log, status);
    }

    private TrackedContentDTO sealAndGetTrackingReport(boolean seal) throws RepositoryManagerException {
        TrackedContentDTO report;
        try {
            IndyFoloAdminClientModule foloAdmin = indy.module(IndyFoloAdminClientModule.class);
            if (seal) {
                userLog.info("Sealing tracking record");
                boolean sealed = foloAdmin.sealTrackingRecord(buildContentId);
                if (!sealed) {
                    throw new RepositoryManagerException(
                            "Failed to seal content-tracking record for: %s.",
                            buildContentId);
                }
            }

            userLog.info("Getting tracking report");
            report = foloAdmin.getTrackingReport(buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException(
                    "Failed to retrieve tracking report for: %s. Reason: %s",
                    e,
                    buildContentId,
                    e.getMessage());
        }
        if (report == null) {
            throw new RepositoryManagerException("Failed to retrieve tracking report for: %s.", buildContentId);
        }
        return report;
    }

    @Override
    public void deleteBuildGroup() throws RepositoryManagerException {
        logger.info("BEGIN: Removing build aggregation group: {}", buildContentId);
        userLog.info("Removing build aggregation group");
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            StoreKey key = new StoreKey(packageType, StoreType.group, buildContentId);
            serviceAccountIndy.stores().delete(key, "[Post-Build] Removing build aggregation group: " + buildContentId);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException(
                    "Failed to retrieve Indy stores module. Reason: %s",
                    e,
                    e.getMessage());
        }
        logger.info(
                "END: Removing build aggregation group: {}, took: {} seconds",
                buildContentId,
                stopWatch.getTime(TimeUnit.SECONDS));
        stopWatch.reset();
    }

    /**
     * Promote all build dependencies NOT ALREADY CAPTURED to the hosted repository holding store for the shared imports
     * and return dependency artifacts meta data.
     *
     * @param report The tracking report that contains info about artifacts downloaded by the build
     * @param promote flag if collected dependencies should be promoted
     * @return List of dependency artifacts meta data
     * @throws RepositoryManagerException In case of a client API transport error or an error during promotion of
     *         artifacts
     * @throws PromotionValidationException when the promotion process results in an error due to validation failure
     */
    private List<Artifact> processDownloads(final TrackedContentDTO report, final boolean promote)
            throws RepositoryManagerException, PromotionValidationException {
        List<Artifact> deps;

        logger.info("BEGIN: Process artifacts downloaded by build");
        userLog.info("Processing dependencies");
        StopWatch stopWatch = StopWatch.createStarted();

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        if (CollectionUtils.isEmpty(downloads)) {
            deps = Collections.emptyList();
        } else {
            deps = collectDownloadedArtifacts(report);

            if (promote) {
                Map<StoreKey, Map<StoreKey, Set<String>>> depMap = collectDownloadsPromotionMap(downloads);
                promoteDownloads(depMap);
            }
        }

        logger.info("END: Process artifacts downloaded by build, took {} seconds", stopWatch.getTime(TimeUnit.SECONDS));
        return deps;
    }

    private List<Artifact> collectDownloadedArtifacts(TrackedContentDTO report) throws RepositoryManagerException {
        IndyContentClientModule content;
        try {
            content = indy.content();
        } catch (IndyClientException e) {
            throw new RepositoryManagerException(
                    "Failed to retrieve Indy content module. Reason: %s",
                    e,
                    e.getMessage());
        }

        Set<TrackedContentEntryDTO> downloads = report.getDownloads();
        List<Artifact> deps = new ArrayList<>(downloads.size());
        for (TrackedContentEntryDTO download : downloads) {
            String path = download.getPath();
            if (artifactFilter.acceptsForData(download)) {
                String identifier = computeIdentifier(download);
                String purl = computePurl(download);

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
                        .purl(purl)
                        .targetRepository(targetRepository);

                Artifact artifact = validateArtifact(artifactBuilder.build());
                deps.add(artifact);
            }
        }
        return deps;
    }

    private Map<StoreKey, Map<StoreKey, Set<String>>> collectDownloadsPromotionMap(
            Set<TrackedContentEntryDTO> downloads) {
        Map<StoreKey, Map<StoreKey, Set<String>>> depMap = new HashMap<>();
        Map<String, StoreKey> promotionTargets = new HashMap<>();
        for (TrackedContentEntryDTO download : downloads) {
            String path = download.getPath();
            StoreKey source = download.getStoreKey();
            String packageType = source.getPackageType();
            if (artifactFilter.acceptsForPromotion(download, true)) {
                StoreKey target = null;
                Map<StoreKey, Set<String>> sources = null;
                Set<String> paths = null;

                // this has not been captured, so promote it.
                switch (packageType) {
                    case MAVEN_PKG_KEY:
                    case NPM_PKG_KEY:
                        target = getPromotionTarget(packageType, promotionTargets);
                        sources = depMap.computeIfAbsent(target, t -> new HashMap<>());
                        paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                        paths.add(path);
                        if (MAVEN_PKG_KEY.equals(packageType) && !isChecksum(path)) {
                            // add the standard checksums to ensure, they are promoted (Maven usually uses only one, so
                            // the other would be missing) but avoid adding checksums of checksums.
                            paths.add(path + ".md5");
                            paths.add(path + ".sha1");
                        }
                        break;

                    case GENERIC_PKG_KEY:
                        String remoteName = source.getName();
                        String hostedName = getGenericHostedRepoName(remoteName);
                        target = new StoreKey(packageType, StoreType.hosted, hostedName);
                        sources = depMap.computeIfAbsent(target, t -> new HashMap<>());
                        paths = sources.computeIfAbsent(source, s -> new HashSet<>());

                        paths.add(path);
                        break;

                    default:
                        // do not promote anything else anywhere
                        break;
                }
            }
        }

        return depMap;
    }

    private boolean isChecksum(String path) {
        String suffix = StringUtils.substringAfterLast(path, ".");
        return checksumSuffixes.contains(suffix);
    }

    /**
     * Promotes by path downloads captured in given map. The key in the map is promotion target store key. The value is
     * another map, where key is promotion source store key and value is list of paths to be promoted.
     *
     * @param depMap dependencies map
     * @throws RepositoryManagerException in case of an unexpected error during promotion
     * @throws PromotionValidationException when the promotion process results in an error due to validation failure
     */
    private void promoteDownloads(Map<StoreKey, Map<StoreKey, Set<String>>> depMap)
            throws RepositoryManagerException, PromotionValidationException {
        for (Map.Entry<StoreKey, Map<StoreKey, Set<String>>> targetToSources : depMap.entrySet()) {
            StoreKey target = targetToSources.getKey();
            for (Map.Entry<StoreKey, Set<String>> sourceToPaths : targetToSources.getValue().entrySet()) {
                StoreKey source = sourceToPaths.getKey();
                PathsPromoteRequest req = new PathsPromoteRequest(source, target, sourceToPaths.getValue())
                        .setPurgeSource(false);
                // set read-only only the generic http proxy hosted repos, not shared-imports
                boolean readonly = !isTempBuild && GENERIC_PKG_KEY.equals(target.getPackageType());

                StopWatch stopWatchDoPromote = StopWatch.createStarted();
                try {
                    logger.info(
                            "BEGIN: doPromoteByPath: source: '{}', target: '{}', readonly: {}",
                            req.getSource().toString(),
                            req.getTarget().toString(),
                            readonly);
                    userLog.info(
                            "Promoting {} dependencies from {} to {}",
                            req.getPaths().size(),
                            req.getSource(),
                            req.getTarget());

                    doPromoteByPath(req, false, readonly);

                    logger.info(
                            "END: doPromoteByPath: source: '{}', target: '{}', readonly: {}, took: {} seconds",
                            req.getSource().toString(),
                            req.getTarget().toString(),
                            readonly,
                            stopWatchDoPromote.getTime(TimeUnit.SECONDS));
                } catch (RepositoryManagerException ex) {
                    logger.info(
                            "END: doPromoteByPath: source: '{}', target: '{}', readonly: {}, took: {} seconds",
                            req.getSource().toString(),
                            req.getTarget().toString(),
                            readonly,
                            stopWatchDoPromote.getTime(TimeUnit.SECONDS));
                    throw ex;
                }
            }
        }
    }

    private StoreKey getPromotionTarget(String packageType, Map<String, StoreKey> promotionTargets) {
        if (!promotionTargets.containsKey(packageType)) {
            StoreKey storeKey = new StoreKey(packageType, StoreType.hosted, SHARED_IMPORTS_ID);
            promotionTargets.put(packageType, storeKey);
        }
        return promotionTargets.get(packageType);
    }

    private TargetRepository getDownloadsTargetRepository(
            TrackedContentEntryDTO download,
            IndyContentClientModule content) throws RepositoryManagerException {
        String identifier;
        String repoPath;
        StoreKey source = download.getStoreKey();
        RepositoryType repoType = toRepoType(source.getPackageType());
        if (repoType == RepositoryType.MAVEN || repoType == RepositoryType.NPM) {
            identifier = "indy-" + repoType.name().toLowerCase();
            repoPath = getTargetRepositoryPath(download, content);
        } else if (repoType == RepositoryType.GENERIC_PROXY) {
            identifier = "indy-http";
            repoPath = getGenericTargetRepositoryPath(source);
        } else {
            throw new RepositoryManagerException(
                    "Repository type " + repoType + " is not supported by Indy repo manager driver.");
        }
        if (!repoPath.endsWith("/")) {
            repoPath += '/';
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
        if (artifactFilter.ignoreDependencySource(sk)) {
            result = "/api/" + content.contentPath(sk);
        } else {
            result = "/api/" + content.contentPath(new StoreKey(packageType, StoreType.hosted, SHARED_IMPORTS_ID));
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
        if (remoteName.startsWith("r-") || remoteName.startsWith("g-")) {
            hostedName = "h-" + remoteName.substring(2);
        } else {
            logger.error(
                    "Unexpected generic-http remote repo/group name {}. Using it for hosted repo "
                            + "without change, but it probably doesn't exist.",
                    remoteName);
            hostedName = remoteName;
        }
        return hostedName;
    }

    private String getGenericTargetRepositoryPath(StoreKey source) {
        return "/api/content/generic-http/hosted/" + getGenericHostedRepoName(source.getName());
    }

    private TargetRepository getUploadsTargetRepository(RepositoryType repoType, IndyContentClientModule content)
            throws RepositoryManagerException {
        StoreKey storeKey;
        String identifier;
        if (repoType == RepositoryType.MAVEN) {
            storeKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildPromotionTarget);
            identifier = ReposiotryIdentifier.INDY_MAVEN;
        } else if (repoType == RepositoryType.NPM) {
            storeKey = new StoreKey(NPM_PKG_KEY, StoreType.hosted, buildPromotionTarget);
            identifier = ReposiotryIdentifier.INDY_NPM;
        } else {
            throw new RepositoryManagerException(
                    "Repository type " + repoType + " is not supported for uploads by Indy repo manager driver.");
        }

        String repoPath = "/api/" + content.contentPath(storeKey);
        if (!repoPath.endsWith("/")) {
            repoPath += '/';
        }
        return TargetRepository.newBuilder()
                .identifier(identifier)
                .repositoryType(repoType)
                .repositoryPath(repoPath)
                .temporaryRepo(isTempBuild)
                .build();
    }

    /**
     * Return list of output artifacts for promotion.
     *
     * @param report The tracking report that contains info about artifacts uploaded (output) from the build
     * @return List of output artifacts meta data
     * @throws RepositoryManagerException In case of a client API transport error or an error during promotion of
     *         artifacts
     */
    private Uploads collectUploads(TrackedContentDTO report) throws RepositoryManagerException {

        List<Artifact> data;
        List<String> promotion;

        logger.info("BEGIN: Process artifacts uploaded from build");
        userLog.info("Processing built artifacts");
        StopWatch stopWatch = StopWatch.createStarted();

        Set<TrackedContentEntryDTO> uploads = report.getUploads();
        if (CollectionUtils.isEmpty(uploads)) {
            data = Collections.emptyList();
            promotion = Collections.emptyList();
        } else {
            data = new ArrayList<>();
            Set<String> promotionSet = new HashSet<>();

            IndyContentClientModule content;
            try {
                content = indy.content();
            } catch (IndyClientException e) {
                throw new RepositoryManagerException(
                        "Failed to retrieve Indy content module. Reason: %s",
                        e,
                        e.getMessage());
            }

            for (TrackedContentEntryDTO upload : uploads) {
                String path = upload.getPath();
                StoreKey storeKey = upload.getStoreKey();

                if (artifactFilter.acceptsForData(upload)) {
                    String identifier = computeIdentifier(upload);
                    String purl = computePurl(upload);

                    logger.info("Recording upload: {}", identifier);

                    RepositoryType repoType = toRepoType(storeKey.getPackageType());
                    TargetRepository targetRepository = getUploadsTargetRepository(repoType, content);

                    ArtifactQuality artifactQuality = getArtifactQuality(isTempBuild);
                    Artifact.Builder artifactBuilder = Artifact.Builder.newBuilder()
                            .md5(upload.getMd5())
                            .sha1(upload.getSha1())
                            .sha256(upload.getSha256())
                            .size(upload.getSize())
                            .deployPath(upload.getPath())
                            .filename(new File(path).getName())
                            .identifier(identifier)
                            .purl(purl)
                            .targetRepository(targetRepository)
                            .artifactQuality(artifactQuality)
                            .buildCategory(buildCategory);

                    Artifact artifact = validateArtifact(artifactBuilder.build());
                    data.add(artifact);
                }

                if (artifactFilter.acceptsForPromotion(upload, false)) {
                    promotionSet.add(path);
                    if (MAVEN_PKG_KEY.equals(storeKey.getPackageType()) && !isChecksum(path)) {
                        // add the standard checksums to ensure, they are promoted (Maven usually uses only one, so
                        // the other would be missing) but avoid adding checksums of checksums.
                        promotionSet.add(path + ".md5");
                        promotionSet.add(path + ".sha1");
                    }
                }
            }
            promotion = new ArrayList<>(promotionSet);
        }
        logger.info("END: Process artifacts uploaded from build, took {} seconds", stopWatch.getTime(TimeUnit.SECONDS));
        return new Uploads(data, promotion);
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
                    ArtifactRef aref = new SimpleArtifactRef(
                            pathInfo.getProjectId(),
                            pathInfo.getType(),
                            pathInfo.getClassifier());
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
                logger.warn(
                        "Package type {} is not handled by Indy repository session.",
                        transfer.getStoreKey().getPackageType());
                break;
        }

        if (identifier == null) {
            identifier = computeGenericIdentifier(
                    transfer.getOriginUrl(),
                    transfer.getLocalUrl(),
                    transfer.getSha256());
        }

        return identifier;
    }

    /**
     * Computes purl string for an artifact.
     *
     * @param transfer the download or upload that we want to generate identifier for
     * @return generated purl
     */
    private String computePurl(final TrackedContentEntryDTO transfer) {
        String purl = null;

        try {
            switch (transfer.getStoreKey().getPackageType()) {
                case MAVEN_PKG_KEY:

                    ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(transfer.getPath());
                    if (pathInfo != null) {
                        // See https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#maven
                        PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                                .withType(PackageURL.StandardTypes.MAVEN)
                                .withNamespace(pathInfo.getProjectId().getGroupId())
                                .withName(pathInfo.getProjectId().getArtifactId())
                                .withVersion(pathInfo.getVersion())
                                .withQualifier(
                                        "type",
                                        StringUtils.isEmpty(pathInfo.getType()) ? "jar" : pathInfo.getType());

                        if (!StringUtils.isEmpty(pathInfo.getClassifier())) {
                            purlBuilder.withQualifier("classifier", pathInfo.getClassifier());
                        }
                        purl = purlBuilder.build().toString();
                    }
                    break;

                case NPM_PKG_KEY:

                    NpmPackagePathInfo npmPathInfo = NpmPackagePathInfo.parse(transfer.getPath());
                    if (npmPathInfo != null) {
                        // See https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#npm
                        PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                                .withType(PackageURL.StandardTypes.NPM)
                                .withVersion(npmPathInfo.getVersion().toString());

                        String[] scopeAndName = npmPathInfo.getName().split("/");
                        if (scopeAndName.length == 1) {
                            // No scope
                            purlBuilder.withName(scopeAndName[0]);

                            purl = purlBuilder.build().toString();
                        } else if (scopeAndName.length == 2) {
                            // Scoped package
                            purlBuilder.withNamespace(scopeAndName[0]);
                            purlBuilder.withName(scopeAndName[1]);

                            purl = purlBuilder.build().toString();
                        }
                    }
                    break;

                case GENERIC_PKG_KEY:
                    // handle generic downloads along with other invalid download paths for other package types
                    break;

                default:
                    // do not do anything by default
                    logger.warn(
                            "Package type {} is not handled by Indy repository session.",
                            transfer.getStoreKey().getPackageType());
                    break;
            }

            if (purl == null) {
                purl = computeGenericPurl(
                        transfer.getPath(),
                        transfer.getOriginUrl(),
                        transfer.getLocalUrl(),
                        transfer.getSha256());
            }

        } catch (MalformedPackageURLException ex) {
            logger.error(
                    "Cannot calculate purl for path {}. Reason given was: {}.",
                    transfer.getPath(),
                    ex.getMessage(),
                    ex);
        }
        return purl;
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
     * Compute the purl string for a generic download, that does not match package type specific files structure. It
     * prefers to use the origin URL if it is not empty. In case it is then it uses local URL, which can never be empty,
     * it is the local file mirror in Indy. After that it attaches the sha256 separated by a pipe.
     *
     * @param originUrl the origin URL of the transfer, it can be null
     * @param localUrl url where the artifact was backed up in Indy
     * @param sha256 the SHA-256 of the transfer
     * @return the generated purl
     * @throws MalformedPackageURLException
     */
    private String computeGenericPurl(String path, String originUrl, String localUrl, String sha256)
            throws MalformedPackageURLException {
        // See https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#generic
        String name = new File(path).getName();
        String downloadUrl = originUrl != null ? originUrl : localUrl;

        PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                .withType(PackageURL.StandardTypes.GENERIC)
                .withName(name)
                .withQualifier("download_url", downloadUrl)
                .withQualifier("checksum", "sha256:" + sha256);

        return purlBuilder.build().toString();
    }

    /**
     * Check artifact for any validation errors. If there are constraint violations, then a RepositoryManagerException
     * is thrown. Otherwise the artifact is returned.
     *
     * @param artifact to validate
     * @return the same artifact
     * @throws RepositoryManagerException if there are constraint violations
     */
    private Artifact validateArtifact(Artifact artifact) throws RepositoryManagerException {
        Set<ConstraintViolation<Artifact>> violations = validator.validate(artifact);
        if (!violations.isEmpty()) {
            throw new RepositoryManagerException(
                    "Repository manager returned invalid artifact: " + artifact.toString()
                            + " Constraint Violations: %s",
                    violations);
        }
        return artifact;
    }

    /**
     * Promotes a set of artifact paths (or everything, if the path-set is missing) from a particular Indy artifact
     * store to another, and handle the various error conditions that may arise. If the promote call fails, attempt to
     * rollback before throwing an exception.
     *
     * @param req The promotion request to process, which contains source and target store keys, and (optionally) the
     *        set of paths to promote
     * @param setTargetRO flag telling if the target repo should be set to readOnly
     * @param setSourceRO flag telling if the source repo should be set to readOnly
     * @throws RepositoryManagerException when the client API throws an exception due to something unexpected in
     *         transport
     * @throws PromotionValidationException when the promotion process results in an error due to validation failure
     */
    private void doPromoteByPath(PathsPromoteRequest req, boolean setSourceRO, boolean setTargetRO)
            throws RepositoryManagerException, PromotionValidationException {
        IndyPromoteClientModule promoter;
        try {
            promoter = serviceAccountIndy.module(IndyPromoteClientModule.class);
        } catch (IndyClientException e) {
            throw new RepositoryManagerException(
                    "Failed to retrieve Indy promote client module. Reason: %s",
                    e,
                    e.getMessage());
        }

        try {
            PathsPromoteResult result = promoter.promoteByPath(req);
            if (result.succeeded()) {
                if (setSourceRO) {
                    setHostedReadOnly(req.getSource(), promoter, result);
                }
                if (setTargetRO) {
                    setHostedReadOnly(req.getTarget(), promoter, result);
                }
            } else {
                String error = getValidationError(result);
                throw new PromotionValidationException("Failed to promote: %s. Reason given was: %s", req, error);
            }
        } catch (IndyClientException e) {
            throw new RepositoryManagerException("Failed to promote: %s. Reason: %s", e, req, e.getMessage());
        }
    }

    /**
     * Sets readonly flag on a hosted repo after promotion. If it fails, it rolls back the promotion and throws
     * RepositoryManagerException.
     *
     * @param key the hosted repo key to be set readonly
     * @param promoter promote client module used for potential rollback
     * @param result the promotion result used for potential rollback
     * @throws IndyClientException in case the repo data cannot be loaded
     * @throws RepositoryManagerException in case the repo update fails
     */
    private void setHostedReadOnly(StoreKey key, IndyPromoteClientModule promoter, PathsPromoteResult result)
            throws IndyClientException, RepositoryManagerException {
        HostedRepository hosted = serviceAccountIndy.stores().load(key, HostedRepository.class);
        hosted.setReadonly(true);
        try {
            serviceAccountIndy.stores().update(hosted, "Setting readonly after successful build and promotion.");
        } catch (IndyClientException ex) {
            try {
                promoter.rollbackPathPromote(result);
            } catch (IndyClientException ex2) {
                logger.error(
                        "Failed to set readonly flag on repo: {}. Reason given was: {}.",
                        key,
                        ex.getMessage(),
                        ex);
                throw new RepositoryManagerException(
                        "Subsequently also failed to rollback the promotion of paths from %s to %s. Reason "
                                + "given was: %s",
                        ex2,
                        result.getRequest().getSource(),
                        result.getRequest().getTarget(),
                        ex2.getMessage());
            }
            throw new RepositoryManagerException(
                    "Failed to set readonly flag on repo: %s. Reason given was: %s",
                    ex,
                    key,
                    ex.getMessage());
        }
    }

    /**
     * Promote the build output to the consolidated build repo (using path promotion, where the build repo contents are
     * added to the repo's contents) and marks the build output as readonly.
     *
     * @param uploads artifacts to be promoted
     * @throws RepositoryManagerException when the repository client API throws an exception due to something unexpected
     *         in transport
     * @throws PromotionValidationException when the promotion process results in an error due to validation failure
     */
    public void promoteToBuildContentSet(List<String> uploads)
            throws RepositoryManagerException, PromotionValidationException {
        userLog.info("Validating and promoting built artifacts");

        StoreKey source = new StoreKey(packageType, StoreType.hosted, buildContentId);
        StoreKey target = new StoreKey(packageType, StoreType.hosted, buildPromotionTarget);

        PathsPromoteRequest request = new PathsPromoteRequest(source, target, new HashSet<>(uploads));

        doPromoteByPath(request, !isTempBuild, false);
    }

    /**
     * Computes error message from a failed promotion result. It means either error must not be empty or validations
     * need to contain at least 1 validation error.
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
        if ((validations != null) && (validations.getRuleSet() != null)) {
            sb.append("One or more validation rules failed in rule-set ")
                    .append(validations.getRuleSet())
                    .append(":\n");

            if (validations.getValidatorErrors().isEmpty()) {
                sb.append("(no validation errors received)");
            } else {
                validations.getValidatorErrors()
                        .forEach(
                                (rule, error) -> sb.append("- ")
                                        .append(rule)
                                        .append(":\n")
                                        .append(error)
                                        .append("\n\n"));
            }
        }
        if (sb.length() == 0) {
            sb.append("(no error message received)");
        }
        return sb.toString();
    }

    private RepositoryType toRepoType(String packageType) {
        switch (packageType) {
            case MAVEN_PKG_KEY:
                return RepositoryType.MAVEN;
            case NPM_PKG_KEY:
                return RepositoryType.NPM;
            case GENERIC_PKG_KEY:
                return RepositoryType.GENERIC_PROXY;
            default:
                return RepositoryType.GENERIC_PROXY;
        }
    }

    private ArtifactQuality getArtifactQuality(boolean isTempBuild) {
        if (isTempBuild) {
            return ArtifactQuality.TEMPORARY;
        } else {
            return ArtifactQuality.NEW;
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(indy);
        IOUtils.closeQuietly(serviceAccountIndy);
    }

    private class Uploads {

        /** List of artifacts to be stored in DB. */
        private List<Artifact> data;

        /** List of paths to be promoted. */
        private List<String> promotion;

        private Uploads(List<Artifact> data, List<String> promotion) {
            this.data = data;
            this.promotion = promotion;
        }

        /**
         * Gets the list of uploaded artifacts to be stored in DB.
         *
         * @return the list
         */
        public List<Artifact> getData() {
            return data;
        }

        /**
         * Gets the list of paths for promotion.
         *
         * @return the list
         */
        public List<String> getPromotion() {
            return promotion;
        }

    }
}
