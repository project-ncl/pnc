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
package org.jboss.pnc.facade.deliverables;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Artifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.ArtifactType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.BuildSystemType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.api.deliverablesanalyzer.dto.MavenArtifact;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.bpm.model.AnalyzeDeliverablesBpmRequest;
import org.jboss.pnc.bpm.task.AnalyzeDeliverablesTask;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.facade.deliverables.api.AnalysisResult;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.jboss.pnc.spi.exception.ProcessManagerException;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_MAVEN;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
@Slf4j
@PermitAll
public class DeliverableAnalyzerManagerImpl implements org.jboss.pnc.facade.DeliverableAnalyzerManager {
    private static final String KOJI_PATH_MAVEN_PREFIX = "/api/content/maven/remote/koji-";

    private static final Pattern NVR_PATTERN = Pattern.compile("(.+)-([^-]+)-([^-]+)");

    public static final String URL_PARAMETER_PREFIX = "url-";

    @Inject
    private ProductMilestoneRepository milestoneRepository;
    @Inject
    private ArtifactRepository artifactRepository;
    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;
    @Inject
    private DeliverableAnalyzerOperationRepository deliverableAnalyzerOperationRepository;
    @Inject
    private DeliverableArtifactRepository deliverableArtifactRepository;
    @Inject
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;
    @Inject
    private DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;
    @Inject
    private ArtifactMapper artifactMapper;
    @Inject
    private OperationsManager operationsManager;
    @Inject
    private UserService userService;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    @Inject
    private BpmModuleConfig bpmConfig;
    @Inject
    private GlobalModuleGroup globalConfig;
    @Inject
    private DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;
    @Inject
    private Event<DeliverableAnalysisStatusChangedEvent> analysisStatusChangedEventNotifier;
    @Inject
    private Connector connector;

    @Override
    public DeliverableAnalyzerOperation analyzeDeliverables(
            String id,
            List<String> deliverablesUrls,
            boolean runAsScratchAnalysis) {
        int i = 1;
        Map<String, String> inputParams = new HashMap<>();
        for (String url : deliverablesUrls) {
            inputParams.put(URL_PARAMETER_PREFIX + (i++), url);
        }

        Base32LongID operationId = operationsManager.newDeliverableAnalyzerOperation(id, inputParams).getId();

        try {
            log.info("Starting analysis of deliverables for milestone {} from urls: {}.", id, deliverablesUrls);
            startAnalysis(id, deliverablesUrls, runAsScratchAnalysis, operationId);
            return deliverableAnalyzerOperationMapper.toDTO(
                    (org.jboss.pnc.model.DeliverableAnalyzerOperation) operationsManager
                            .updateProgress(operationId, ProgressStatus.IN_PROGRESS));
        } catch (RuntimeException ex) {
            operationsManager.setResult(operationId, OperationResult.SYSTEM_ERROR);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void completeAnalysis(AnalysisResult analysisResult) {
        log.info(
                "Processing deliverables of operation with id={} in {} results.",
                analysisResult.getDeliverableAnalyzerOperationId(),
                analysisResult.getResults().size());

        DeliverableAnalyzerReport report = createReportForCompletedAnalysis(
                analysisResult.getDeliverableAnalyzerOperationId(),
                analysisResult.isWasRunAsScratchAnalysis());
        for (FinderResult finderResult : analysisResult.getResults()) {
            processDeliverables(
                    report,
                    finderResult.getBuilds(),
                    finderResult.getUrl().toString(),
                    finderResult.getNotFoundArtifacts());
        }
    }

    private void processDeliverables(
            DeliverableAnalyzerReport report,
            Collection<Build> builds,
            String distributionUrl,
            Collection<Artifact> notFoundArtifacts) {

        log.debug("Processing deliverables in {} builds. Distribution URL: {}", builds.size(), distributionUrl);
        User user = report.getOperation().getUser();

        ArtifactStats stats = new ArtifactStats();
        ArtifactCache artifactCache = new ArtifactCache(builds, user);
        Set<Base32LongID> pncBuildIds = new HashSet<>();

        for (Build build : builds) {
            log.debug("Processing build {}", build);
            if (build.getBuildSystemType() == null) {
                throw new IllegalArgumentException("Build system type not set.");
            }

            Function<Artifact, org.jboss.pnc.model.Artifact> artifactParser;
            Consumer<Artifact> statCounter;
            switch (build.getBuildSystemType()) {
                case PNC:
                    statCounter = stats.pncCounter();
                    artifactParser = artifactCache::findPNCArtifact;
                    pncBuildIds.add(BuildMapper.idMapper.toEntity(build.getPncId()));
                    break;
                case BREW:
                    statCounter = stats.brewCounter();
                    TargetRepository brewRepository = artifactCache.findOrCreateTargetRepository(build);
                    artifactParser = art -> artifactCache
                            .findOrCreateBrewArtifact(art, brewRepository, build.getBrewNVR());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown build system type " + build.getBuildSystemType());
            }
            build.getArtifacts().stream().peek(statCounter).forEach(artifactDto -> {
                addDeliveredArtifact(
                        artifactParser.apply(artifactDto),
                        report,
                        artifactDto.isBuiltFromSource(),
                        build.getBrewId());
            });
        }

        if (!notFoundArtifacts.isEmpty()) {
            TargetRepository distributionRepository = getDistributionRepository(distributionUrl);
            notFoundArtifacts.stream()
                    .peek(stats.notFoundCounter())
                    .map(art -> findOrCreateNotFoundArtifact(art, distributionRepository, pncBuildIds, user))
                    .forEach(artifact -> addDeliveredArtifact(artifact, report, false, null));
        }
        stats.log(distributionUrl);
    }

    private void addDeliveredArtifact(
            org.jboss.pnc.model.Artifact artifact,
            DeliverableAnalyzerReport report,
            boolean builtFromSource,
            Long brewBuildId) {
        DeliverableArtifact deliverableArtifact = DeliverableArtifact.builder()
                .artifact(artifact)
                .report(report)
                .builtFromSource(builtFromSource)
                .brewBuildId(brewBuildId)
                .build();
        report.addDeliverableArtifact(deliverableArtifact);
        deliverableArtifactRepository.save(deliverableArtifact);
    }

    private DeliverableAnalyzerReport createReportForCompletedAnalysis(
            Base32LongID operationId,
            boolean wasRunAsScratchAnalysis) {

        var report = DeliverableAnalyzerReport.builder()
                .id(operationId)
                .operation(deliverableAnalyzerOperationRepository.queryById(operationId))
                .labels(getReportLabels(wasRunAsScratchAnalysis))
                .labelHistory(new ArrayList<>())
                .artifacts(new HashSet<>(Set.of()))
                .build();
        deliverableAnalyzerReportRepository.save(report);
        if (wasRunAsScratchAnalysis) {
            updateLabelHistoryWithScratchEntry(report);
        }
        return report;
    }

    private EnumSet<DeliverableAnalyzerReportLabel> getReportLabels(boolean wasRunAsScratchAnalysis) {
        return (wasRunAsScratchAnalysis) ? EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH)
                : EnumSet.noneOf(DeliverableAnalyzerReportLabel.class);
    }

    private void updateLabelHistoryWithScratchEntry(DeliverableAnalyzerReport report) {
        DeliverableAnalyzerLabelEntry labelHistoryEntry = DeliverableAnalyzerLabelEntry.builder()
                .report(report)
                .changeOrder(1)
                .entryTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .user(userService.currentUser())
                .reason("Analysis run as scratch.")
                .change(LabelOperation.ADDED)
                .label(DeliverableAnalyzerReportLabel.SCRATCH)
                .build();
        deliverableAnalyzerLabelEntryRepository.save(labelHistoryEntry);
        report.getLabelHistory().add(labelHistoryEntry);
    }

    public Consumer<org.jboss.pnc.model.Artifact> artifactUpdater(String message) {
        User user = userService.currentUser();
        return a -> {
            a.setQualityLevelReason(message);
            a.setModificationUser(user);
            a.setModificationTime(new Date());
        };
    }

    private Optional<org.jboss.pnc.model.Artifact> getBestMatchingArtifact(
            Collection<org.jboss.pnc.model.Artifact> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            return Optional.empty();
        }

        return artifacts.stream()
                .sorted(Comparator.comparing(DeliverableAnalyzerManagerImpl::getNotFoundArtifactRating).reversed())
                .findFirst();
    }

    private static int getNotFoundArtifactRating(org.jboss.pnc.model.Artifact artifact) {
        ArtifactQuality quality = artifact.getArtifactQuality();

        switch (quality) {
            case NEW:
                return 1;
            case VERIFIED:
                return 2;
            case TESTED:
                return 3;
            case IMPORTED:
                return 4;
            case DEPRECATED:
                return -1;
            case BLACKLISTED:
                return -2;
            case TEMPORARY:
                return -3;
            case DELETED:
                return -4;
            default:
                log.warn("Unsupported ArtifactQuality! Got: {}", quality);
                return -100;
        }
    }

    private org.jboss.pnc.model.Artifact findOrCreateNotFoundArtifact(
            Artifact artifact,
            TargetRepository targetRepo,
            Set<Base32LongID> pncBuiltRecordIds,
            User user) {

        // The artifact was not built from source, but could already be present as a dependency recorded in PNC system.
        // To avoid unnecessary artifact duplication (see NCLSUP-990), we will search for a best matching artifact with
        // some priority checks.

        List<org.jboss.pnc.model.Artifact> artifacts;
        Optional<org.jboss.pnc.model.Artifact> bestMatch;

        // 1) We will see if there are artifacts with the same SHA-256 which are dependencies of one of the found
        // PNC builds (if any) in the current delivered analysis.
        // If more than one artifact is found, find a best match.
        if (!pncBuiltRecordIds.isEmpty()) {
            artifacts = artifactRepository
                    .withSha256AndDependantBuildRecordIdIn(artifact.getSha256(), pncBuiltRecordIds);
            bestMatch = getBestMatchingArtifact(artifacts);
            if (bestMatch.isPresent()) {
                return bestMatch.get();
            }
        }

        // 2) We will see if there is an artifact with the same SHA-256 and identifier.
        // If more than one artifact is found, find a best match.
        if (artifact.getArtifactType() == ArtifactType.MAVEN) {
            String identifier = createIdentifier((MavenArtifact) artifact);
            artifacts = artifactRepository.withIdentifierAndSha256(identifier, artifact.getSha256());
            bestMatch = getBestMatchingArtifact(artifacts);
            if (bestMatch.isPresent()) {
                return bestMatch.get();
            }
        } else {
            artifacts = artifactRepository.withIdentifierAndSha256(artifact.getFilename(), artifact.getSha256());
            bestMatch = getBestMatchingArtifact(artifacts);
            if (bestMatch.isPresent()) {
                return bestMatch.get();
            }
        }

        // 3) We will see if there is an artifact just with the same SHA-256.
        // If more than one artifact is found, find a best match.
        artifacts = artifactRepository.withSha256In(Collections.singleton(artifact.getSha256()));
        bestMatch = getBestMatchingArtifact(artifacts);
        if (bestMatch.isPresent()) {
            return bestMatch.get();
        }

        // Finally, there was no artifact found with the same SHA-56. Create a new one
        return createArtifact(mapNotFoundArtifact(artifact, user), targetRepo);
    }

    private org.jboss.pnc.model.Artifact createArtifact(
            org.jboss.pnc.model.Artifact artifact,
            TargetRepository targetRepo) {

        artifact.setTargetRepository(targetRepo);
        artifact.setPurl(
                createGenericPurl(
                        targetRepo.getRepositoryPath(),
                        artifact.getFilename().toString(),
                        artifact.getSha256()));
        org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
        targetRepo.getArtifacts().add(savedArtifact);
        return savedArtifact;
    }

    private org.jboss.pnc.model.Artifact mapNotFoundArtifact(Artifact artifact, User user) {
        org.jboss.pnc.model.Artifact.Builder builder = mapArtifact(artifact, user);
        Path path = Paths.get(artifact.getFilename());
        builder.filename(path.getFileName().toString());
        builder.identifier(artifact.getFilename());
        Path directory = path.getParent();
        builder.deployPath(directory == null ? null : directory.toString());

        return builder.build();
    }

    private org.jboss.pnc.model.Artifact mapBrewArtifact(
            Artifact artifact,
            String nvr,
            TargetRepository targetRepository,
            User user) {
        if (artifact.getArtifactType() != ArtifactType.MAVEN) {
            throw new UnsupportedOperationException("Brew artifact " + artifact + " is not Maven!");
        }
        MavenArtifact mavenArtifact = (MavenArtifact) artifact;

        org.jboss.pnc.model.Artifact.Builder builder = mapArtifact(mavenArtifact, user);
        builder.identifier(createIdentifier(mavenArtifact));
        builder.filename(createFileName(mavenArtifact));
        builder.deployPath(createDeployPath(mavenArtifact));
        builder.originUrl(createBrewOriginURL(mavenArtifact, nvr));
        builder.purl(createPURL(mavenArtifact));
        builder.targetRepository(targetRepository);

        return builder.build();
    }

    private org.jboss.pnc.model.Artifact.Builder mapArtifact(Artifact artifact, User user) {
        Date now = new Date();
        org.jboss.pnc.model.Artifact.Builder builder = org.jboss.pnc.model.Artifact.builder();
        builder.md5(artifact.getMd5());
        builder.sha1(artifact.getSha1());
        builder.sha256(artifact.getSha256());
        builder.size(artifact.getSize());
        builder.importDate(now);
        builder.creationUser(user);
        builder.creationTime(now);

        if (artifact.isBuiltFromSource()) {
            builder.artifactQuality(ArtifactQuality.NEW);
        } else {
            builder.artifactQuality(ArtifactQuality.IMPORTED);
        }

        return builder;
    }

    private static String createDeployPath(MavenArtifact mavenArt) {
        String filename = createFileName(mavenArt);
        String deployPath = "/" + mavenArt.getGroupId().replace('.', '/') + "/" + mavenArt.getArtifactId() + "/"
                + mavenArt.getVersion() + "/" + filename;
        return deployPath;
    }

    private static String createFileName(MavenArtifact mavenArt) {
        String filename = mavenArt.getArtifactId() + "-" + mavenArt.getVersion();
        if (!Strings.isEmpty(mavenArt.getClassifier())) {
            filename += "-" + mavenArt.getClassifier();
        }
        filename += "." + mavenArt.getType();
        return filename;
    }

    private String createBrewOriginURL(MavenArtifact mavenArt, String nvr) {
        String brewContentUrl = globalConfig.getBrewContentUrl();

        Matcher matcher = NVR_PATTERN.matcher(nvr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("NVR " + nvr + " does not match expected format.");
        }
        String name = matcher.group(1);
        String version = matcher.group(2);
        String release = matcher.group(3);

        return brewContentUrl + "/" + name + "/" + version + "/" + release + "/maven" + createDeployPath(mavenArt);
    }

    private String createPURL(MavenArtifact mavenArtifact) {
        try {
            PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                    .withType(PackageURL.StandardTypes.MAVEN)
                    .withNamespace(mavenArtifact.getGroupId())
                    .withName(mavenArtifact.getArtifactId())
                    .withVersion(mavenArtifact.getVersion())
                    .withQualifier(
                            "type",
                            StringUtils.isEmpty(mavenArtifact.getType()) ? "jar" : mavenArtifact.getType());

            if (!StringUtils.isEmpty(mavenArtifact.getClassifier())) {
                purlBuilder.withQualifier("classifier", mavenArtifact.getClassifier());
            }
            return purlBuilder.build().toString();
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compute the purl string for a generic download, that does not match package type specific files structure. See
     * https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#generic
     *
     * @param downloadUrl url where the artifact was downloaded from
     * @param filename the artifact filename
     * @param sha256 the SHA-256 of the artifact
     * @return the generated purl
     */
    private String createGenericPurl(String downloadUrl, String filename, String sha256) {
        try {
            PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                    .withType(PackageURL.StandardTypes.GENERIC)
                    .withName(filename)
                    .withQualifier("download_url", downloadUrl)
                    .withQualifier("checksum", "sha256:" + sha256);
            return purlBuilder.build().toString();
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createIdentifier(MavenArtifact mavenArtifact) {
        return Arrays
                .asList(
                        mavenArtifact.getGroupId(),
                        mavenArtifact.getArtifactId(),
                        mavenArtifact.getType(),
                        mavenArtifact.getVersion(),
                        mavenArtifact.getClassifier())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }

    private TargetRepository getDistributionRepository(String distURL) {
        TargetRepository tr = targetRepositoryRepository.queryByIdentifierAndPath(DISTRIBUTION_ARCHIVE, distURL);
        if (tr == null) {
            tr = createRepository(distURL, DISTRIBUTION_ARCHIVE, RepositoryType.DISTRIBUTION_ARCHIVE);
        }
        return tr;
    }

    private TargetRepository createRepository(String path, String identifier, RepositoryType type) {
        TargetRepository tr = TargetRepository.newBuilder()
                .temporaryRepo(false)
                .identifier(identifier)
                .repositoryPath(path)
                .repositoryType(type)
                .artifacts(new HashSet<>())
                .build();
        return targetRepositoryRepository.save(tr);
    }

    private void startAnalysis(
            String milestoneId,
            List<String> deliverablesUrls,
            boolean runAsScratchAnalysis,
            Base32LongID operationId) {
        Request callback = operationsManager.getOperationCallback(operationId);
        String id = operationId.getId();
        try {
            AnalyzeDeliverablesBpmRequest bpmRequest = new AnalyzeDeliverablesBpmRequest(
                    id,
                    deliverablesUrls,
                    runAsScratchAnalysis);
            AnalyzeDeliverablesTask analyzeTask = new AnalyzeDeliverablesTask(bpmRequest, callback);

            connector.startProcess(
                    bpmConfig.getAnalyzeDeliverablesBpmProcessId(),
                    analyzeTask,
                    id,
                    keycloakServiceClient.getAuthToken());

            DeliverableAnalysisStatusChangedEvent analysisStatusChanged = DefaultDeliverableAnalysisStatusChangedEvent
                    .started(id, milestoneId, deliverablesUrls);
            analysisStatusChangedEventNotifier.fire(analysisStatusChanged);
        } catch (ProcessManagerException e) {
            log.error("Error trying to start analysis of deliverables task for milestone: {}", milestoneId, e);
            throw new RuntimeException(e);
        }
    }

    public void observeEvent(@Observes OperationChangedEvent event) {
        if (event.getOperationClass() != org.jboss.pnc.model.DeliverableAnalyzerOperation.class) {
            return;
        }
        log.debug("Observed deliverable analysis operation status changed event {}.", event);
        if (event.getStatus() == ProgressStatus.FINISHED && event.getPreviousStatus() != ProgressStatus.FINISHED) {
            org.jboss.pnc.model.DeliverableAnalyzerOperation operation = deliverableAnalyzerOperationRepository
                    .queryById(event.getId());
            onDeliverableAnalysisFinished(operation);
        }
    }

    private void onDeliverableAnalysisFinished(org.jboss.pnc.model.DeliverableAnalyzerOperation operation) {
        List<String> deliverablesUrls = operation.getOperationParameters()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(URL_PARAMETER_PREFIX))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        DeliverableAnalysisStatusChangedEvent analysisStatusChanged = DefaultDeliverableAnalysisStatusChangedEvent
                .finished(
                        operation.getId().getId(),
                        operation.getProductMilestone().getId().toString(),
                        operation.getResult(),
                        deliverablesUrls);
        analysisStatusChangedEventNotifier.fire(analysisStatusChanged);
    }

    private class ArtifactStats {
        int totalArtifacts = 0;
        int pncArtifactsCount = 0;
        int pncNotBuiltArtifactsCount = 0;
        int brewArtifactsCount = 0;
        int brewNotBuiltArtifactsCount = 0;
        int notFoundArtifactsCount = 0;

        public Consumer<Artifact> pncCounter() {
            return a -> {
                totalArtifacts++;
                pncArtifactsCount++;
                if (!a.isBuiltFromSource()) {
                    pncNotBuiltArtifactsCount++;
                }
            };
        }

        public Consumer<Artifact> brewCounter() {
            return a -> {
                totalArtifacts++;
                brewArtifactsCount++;
                if (!a.isBuiltFromSource()) {
                    brewNotBuiltArtifactsCount++;
                }
            };
        }

        public Consumer<Artifact> notFoundCounter() {
            return a -> {
                totalArtifacts++;
                notFoundArtifactsCount++;
            };
        }

        public void log(String distributionUrl) {
            log.info("Processed {} artifacts from deliverables at {}: ", totalArtifacts, distributionUrl);
            log.info(
                    "  PNC artifacts: {} ({} artifacts not built from source), BREW artifacts: {} ({} artifacts not built from source), other artifacts not built from source: {} ",
                    pncArtifactsCount,
                    pncNotBuiltArtifactsCount,
                    brewArtifactsCount,
                    brewNotBuiltArtifactsCount,
                    notFoundArtifactsCount);
            int totalNotBuild = pncNotBuiltArtifactsCount + brewNotBuiltArtifactsCount + notFoundArtifactsCount;
            if (totalNotBuild > 0) {
                log.info("  There are total {} artifacts not built from source!", totalNotBuild);
            }
        }
    }

    private class ArtifactCache {

        private Map<Integer, org.jboss.pnc.model.Artifact> pncCache = new HashMap<>();

        private Map<IdentifierShaRepo, org.jboss.pnc.model.Artifact> brewCache = new HashMap<>();
        private Map<String, TargetRepository> targetRepositoryCache = new HashMap<>();

        private User user;

        public ArtifactCache(Collection<Build> builds, User user) {
            this.user = user;
            prefetchPNCArtifacts(builds);
            prefetchTargetRepos(builds);
            prefetchBrewArtifacts(builds);
        }

        private void prefetchPNCArtifacts(Collection<Build> builds) {
            log.debug("Preloading PNC artifacts");
            Set<Integer> ids = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.PNC)
                    .flatMap(b -> b.getArtifacts().stream())
                    .map(a -> a.getPncId())
                    .map(artifactMapper.getIdMapper()::toEntity)
                    .collect(Collectors.toSet());

            pncCache = artifactRepository.queryWithPredicates(ArtifactPredicates.withIds(ids))
                    .stream()
                    .collect(Collectors.toMap(org.jboss.pnc.model.Artifact::getId, Function.identity()));
            log.debug("Preloaded {} artifacts to cache.", pncCache.size());
        }

        private void prefetchTargetRepos(Collection<Build> builds) {
            Set<TargetRepository.IdentifierPath> queries = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.BREW)
                    .map(this::getKojiPath)
                    .map(path -> new TargetRepository.IdentifierPath(INDY_MAVEN, path))
                    .collect(Collectors.toSet());

            List<TargetRepository> targetRepositories = targetRepositoryRepository.queryByIdentifiersAndPaths(queries);
            for (TargetRepository targetRepository : targetRepositories) {
                targetRepositoryCache.put(targetRepository.getRepositoryPath(), targetRepository);
            }
            log.debug("Preloaded {} target repos.", targetRepositoryCache.size());
        }

        private void prefetchBrewArtifacts(Collection<Build> builds) {
            Set<IdentifierShaRepo> queries = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.BREW)
                    .flatMap(this::prefetchBrewBuild)
                    .collect(Collectors.toSet());

            Set<org.jboss.pnc.model.Artifact.IdentifierSha256> identifierSha256Set = queries.stream()
                    .map(IdentifierShaRepo::getIdentifierSha256)
                    .collect(Collectors.toSet());
            Set<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                    .withIdentifierAndSha256(identifierSha256Set);
            for (org.jboss.pnc.model.Artifact artifact : artifacts) {
                IdentifierShaRepo key = new IdentifierShaRepo(
                        artifact.getIdentifierSha256(),
                        artifact.getTargetRepository());
                if (queries.contains(key)) {
                    brewCache.put(key, artifact);
                }
            }
            log.debug("Preloaded {} brew artifacts to cache.", targetRepositoryCache.size(), brewCache.size());
        }

        public Stream<IdentifierShaRepo> prefetchBrewBuild(Build build) {
            String path = getKojiPath(build);
            TargetRepository targetRepository = targetRepositoryCache.get(path);
            if (targetRepository == null) {
                return Stream.empty();
            } else {
                return build.getArtifacts()
                        .stream()
                        .peek(this::assertBrewArtifacts)
                        .map(a -> mapBrewArtifact(a, build.getBrewNVR(), targetRepository, user))
                        .map(a -> new IdentifierShaRepo(a.getIdentifierSha256(), targetRepository));
            }
        }

        private String getKojiPath(Build build) {
            return KOJI_PATH_MAVEN_PREFIX + build.getBrewNVR() + '/';
        }

        private void assertBrewArtifacts(Artifact artifact) {
            if (!(artifact.getArtifactType() == null || artifact.getArtifactType() == ArtifactType.MAVEN)) {
                throw new IllegalArgumentException(
                        "Brew artifacts are expected to be either MAVEN or unknown, artifact " + artifact + " is "
                                + artifact.getArtifactType());
            }
        }

        public org.jboss.pnc.model.Artifact findPNCArtifact(Artifact art) {
            org.jboss.pnc.model.Artifact artifact = pncCache.get(artifactMapper.getIdMapper().toEntity(art.getPncId()));
            if (artifact == null) {
                throw new IllegalArgumentException("PNC artifact with id " + art.getPncId() + " doesn't exist.");
            }
            return artifact;
        }

        public TargetRepository findOrCreateTargetRepository(Build build) {
            String path = getKojiPath(build);
            TargetRepository tr = targetRepositoryCache.get(path);
            if (tr == null) {
                tr = createRepository(path, INDY_MAVEN, RepositoryType.MAVEN);
                targetRepositoryCache.put(path, tr);
            }
            return tr;
        }

        public org.jboss.pnc.model.Artifact findOrCreateBrewArtifact(
                Artifact artifact,
                TargetRepository targetRepository,
                String nvr) {
            return findOrCreateBrewArtifact(mapBrewArtifact(artifact, nvr, targetRepository, user));
        }

        private org.jboss.pnc.model.Artifact findOrCreateBrewArtifact(org.jboss.pnc.model.Artifact artifact) {
            TargetRepository targetRepository = artifact.getTargetRepository();
            IdentifierShaRepo key = new IdentifierShaRepo(artifact.getIdentifierSha256(), targetRepository);
            org.jboss.pnc.model.Artifact cachedArtifact = brewCache.get(key);
            if (cachedArtifact != null) {
                return cachedArtifact;
            }
            org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
            targetRepository.getArtifacts().add(savedArtifact);
            brewCache.put(key, savedArtifact);
            return savedArtifact;
        }
    }

    @Value
    private static class IdentifierShaRepo {
        org.jboss.pnc.model.Artifact.IdentifierSha256 identifierSha256;
        TargetRepository targetRepository;
    }
}
