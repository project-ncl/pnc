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
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Artifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.BuildSystemType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.api.deliverablesanalyzer.dto.LicenseInfo;
import org.jboss.pnc.api.dto.ExceptionResolution;
import org.jboss.pnc.api.dto.OperationOutcome;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.dto.exception.ReasonedException;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.api.enums.LicenseSource;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dingroguclient.DingroguClient;
import org.jboss.pnc.dingroguclient.DingroguDeliverablesAnalysisDTO;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.facade.deliverables.api.AnalysisResult;
import org.jboss.pnc.mapper.ResultStatusMapper;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactLicenseInfo;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerDistributionRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactLicenseInfoRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.events.OperationChangedEvent;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
@Slf4j
@PermitAll
public class DeliverableAnalyzerManagerImpl implements org.jboss.pnc.facade.DeliverableAnalyzerManager {
    public static final String URL_PARAMETER_PREFIX = "url-";

    @Inject
    private ArtifactRepository artifactRepository;
    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;
    @Inject
    private DeliverableAnalyzerDistributionRepository deliverableAnalyzerDistributionRepository;
    @Inject
    private DeliverableAnalyzerOperationRepository deliverableAnalyzerOperationRepository;
    @Inject
    private DeliverableArtifactRepository deliverableArtifactRepository;
    @Inject
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;
    @Inject
    private DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;
    @Inject
    private DeliverableArtifactLicenseInfoRepository deliverableArtifactLicenseInfoRepository;
    @Inject
    private ArtifactMapper artifactMapper;
    @Inject
    private OperationsManager operationsManager;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    @Inject
    ResultStatusMapper resultStatusMapper;

    @Inject
    private BpmModuleConfig bpmConfig;
    @Inject
    private GlobalModuleGroup globalConfig;
    @Inject
    private DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;
    @Inject
    private Event<DeliverableAnalysisStatusChangedEvent> analysisStatusChangedEventNotifier;
    @Inject
    private DingroguClient dingroguClient;

    /**
     * Bean-managed transaction used to store a completed analysis. Storing a large analysis can take longer than the
     * default JTA transaction timeout (300s), so we drive the transaction manually with an extended timeout (see
     * {@link #COMPLETE_ANALYSIS_TX_TIMEOUT_SECONDS}) to prevent the transaction reaper from aborting it.
     */
    @Inject
    private UserTransaction userTransaction;

    /**
     * Transaction timeout, in seconds, for storing a completed deliverable analysis. Set generously above the default
     * 300s because a single analysis may contain a very large number of artifacts.
     */
    static final int COMPLETE_ANALYSIS_TX_TIMEOUT_SECONDS = (int) Duration.ofMinutes(30).getSeconds();

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
            MDCUtils.addProcessContext(operationId.getId());
            log.info("Starting analysis of deliverables for milestone {} from urls: {}.", id, deliverablesUrls);
            startAnalysis(id, deliverablesUrls, runAsScratchAnalysis, operationId);
            return deliverableAnalyzerOperationMapper.toDTO(
                    (org.jboss.pnc.model.DeliverableAnalyzerOperation) operationsManager
                            .updateProgress(operationId, ProgressStatus.IN_PROGRESS));
        } catch (ReasonedException e) {
            operationsManager.setResult(
                    operationId,
                    OperationOutcome
                            .process(resultStatusMapper.toOperationResult(e.getResult()), e.getExceptionResolution()));
            log.error(
                    "ErrorId={} Analysis of deliverables with ID {} failed: {}",
                    e.getErrorId(),
                    id,
                    e.getMessage() == null ? e.toString() : e.getMessage(),
                    e.getCause());
            throw e;
        } catch (RuntimeException e) {
            final String errorId = UUID.randomUUID().toString();
            final String errorReason = String.format(
                    "Analysis with ID %s failed: %s",
                    id,
                    e.getMessage() == null ? e.toString() : e.getMessage());
            final String errorProposal = String.format(
                    "There is an internal system error (ID: %s), please contact PNC team at #forum-pnc-users",
                    errorId);
            final ExceptionResolution exceptionResolution = ExceptionResolution.builder()
                    .reason(errorReason)
                    .proposal(errorProposal)
                    .build();
            operationsManager.setResult(operationId, OperationOutcome.systemError(exceptionResolution));
            log.error(
                    "ErrorId={} Analysis of deliverables with ID {} failed. {}",
                    errorId,
                    id,
                    e.getMessage() == null ? "" : e.getMessage(),
                    e);
            throw e;
        } finally {
            MDCUtils.removeProcessContext();
        }
    }

    @Override
    public void completeAnalysis(AnalysisResult analysisResult) {
        Base32LongID operationId = analysisResult.getDeliverableAnalyzerOperationId();
        log.info(
                "Processing deliverables of operation with id={} in {} results.",
                operationId,
                analysisResult.getResults().size());

        // Store the whole analysis atomically in a single bean-managed transaction with an extended timeout, so
        // that large analyses are not aborted by the transaction reaper at the default 300s JTA timeout.
        runInTransactionWithExtendedTimeout(() -> {
            DeliverableAnalyzerReport report = createReportForCompletedAnalysis(
                    operationId,
                    analysisResult.isWasRunAsScratchAnalysis());
            for (FinderResult finderResult : analysisResult.getResults()) {
                processDeliverables(
                        report,
                        finderResult.getBuilds(),
                        finderResult.getUrl(),
                        finderResult.getNotFoundArtifacts());
            }
        });
    }

    /**
     * Runs the given work in a bean-managed JTA transaction using {@link #COMPLETE_ANALYSIS_TX_TIMEOUT_SECONDS} as the
     * timeout instead of the container default. The timeout is reset afterwards because the executing (pooled) thread
     * may be reused for other work.
     */
    private void runInTransactionWithExtendedTimeout(Runnable work) {
        // The outer finally always resets the timeout, even if begin() fails, so the elevated timeout is never
        // leaked onto the (pooled, reused) executor thread.
        try {
            try {
                userTransaction.setTransactionTimeout(COMPLETE_ANALYSIS_TX_TIMEOUT_SECONDS);
                userTransaction.begin();
            } catch (NotSupportedException | SystemException e) {
                throw new RuntimeException("Failed to begin transaction for storing deliverable analysis.", e);
            }
            boolean committed = false;
            try {
                work.run();
                userTransaction.commit();
                committed = true;
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SystemException e) {
                // Only commit's checked exceptions are wrapped here; RuntimeExceptions and Errors thrown by the
                // work propagate unchanged (an Error must not be swallowed into a RuntimeException).
                throw new RuntimeException("Failed to commit transaction for storing deliverable analysis.", e);
            } finally {
                if (!committed) {
                    rollbackQuietly();
                }
            }
        } finally {
            try {
                userTransaction.setTransactionTimeout(0); // restore the container default for this thread
            } catch (SystemException e) {
                log.warn("Failed to reset transaction timeout after storing deliverable analysis.", e);
            }
        }
    }

    private void rollbackQuietly() {
        try {
            int status = userTransaction.getStatus();
            if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                userTransaction.rollback();
            }
        } catch (SystemException e) {
            log.error("Failed to roll back transaction after error storing deliverable analysis.", e);
        }
    }

    private Artifact findDistributionUrlAssociatedArtifact(
            URL distributionUrl,
            Collection<Build> builds,
            Collection<Artifact> notFoundArtifacts) {
        // Find the url filename
        String urlFilename = Paths.get(distributionUrl.getPath()).getFileName().toString();

        /* Loop in the builds to find the artifact associated with the url */

        // [NCLSUP-1114] Handle the case where the file referenced in `distributionUrl` has been renamed from the
        // original filename built in PNC. In this case, the `artifact.filename` contains the original name
        // built in PNC (e.g. "my-product-dist-1.0.0.redhat-00001.zip") and the `artifact.archiveFilenames`
        // contains the renamed filename (e.g. "my-product-1.0.0.zip" if the `distributionUrl` is
        // "https://download.com/my-product-1.0.0.zip"). So let's search `artifact.archiveFilenames` first.
        Optional<Artifact> distributionArtifact = builds.stream()
                .flatMap(b -> b.getArtifacts().stream())
                .filter(a -> a.getArchiveFilenames() != null && a.getArchiveFilenames().contains(urlFilename))
                .findFirst();
        if (distributionArtifact.isPresent()) {
            return distributionArtifact.get();
        }

        // If not found, let's search by `artifact.filename` which contains the original filename built in PNC
        distributionArtifact = builds.stream()
                .flatMap(b -> b.getArtifacts().stream())
                .filter(a -> a.getFilename().equals(urlFilename))
                .findFirst();
        if (distributionArtifact.isPresent()) {
            return distributionArtifact.get();
        }

        // If the artifact is still not found among the matched builds then search in the not found artifacts list (this
        // means the zip was built in e.g. Jenkins)
        return notFoundArtifacts.stream().filter(a -> a.getFilename().equals(urlFilename)).findFirst().orElse(null);
    }

    private void processDeliverables(
            DeliverableAnalyzerReport report,
            Collection<Build> builds,
            URL distributionUrl,
            Collection<Artifact> notFoundArtifacts) {

        log.debug("Processing deliverables in {} builds. Distribution URL: {}", builds.size(), distributionUrl);
        User user = report.getOperation().getUser();

        ArtifactStats stats = new ArtifactStats();
        ArtifactCache artifactCache = new ArtifactCache(builds, user);

        // Find the artifact associated with the deliverable URL
        Artifact urlAssociatedArtifact = findDistributionUrlAssociatedArtifact(
                distributionUrl,
                builds,
                notFoundArtifacts);
        if (urlAssociatedArtifact == null) {
            log.warn("The local archive associated with the deliverableUrl was not found!");
        }

        DeliverableAnalyzerDistribution distribution = getDistribution(
                distributionUrl.toString(),
                urlAssociatedArtifact);

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
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown build system type " + build.getBuildSystemType());
            }
            build.getArtifacts().stream().peek(statCounter).forEach(artifactDto -> {
                addDeliveredArtifact(
                        artifactParser.apply(artifactDto),
                        report,
                        artifactDto.isBuiltFromSource(),
                        artifactDto.getArchiveFilenames(),
                        artifactDto.getArchiveUnmatchedFilenames(),
                        artifactDto.getLicenses(),
                        distribution);
            });
        }

        /*
         * Not found artifacts are artifacts which were not built in PNC
         */
        if (!notFoundArtifacts.isEmpty()) {
            TargetRepository distributionRepository = getDistributionRepository(distributionUrl.toString());
            // Prefetch all existing artifacts matching the not-found SHA-256s in a single query, instead of
            // querying the DB once per artifact (the N+1 that drove this transaction over the timeout). The map
            // is kept up to date with artifacts created below so duplicates within this deliverable are still
            // de-duplicated (see NCL-8718); duplicates across deliverables are handled by each deliverable's
            // transaction re-running this prefetch.
            Set<String> notFoundSha256s = notFoundArtifacts.stream()
                    .map(Artifact::getSha256)
                    .collect(Collectors.toSet());
            Map<String, List<org.jboss.pnc.model.Artifact>> existingArtifactsBySha256 = artifactRepository
                    .withSha256In(notFoundSha256s)
                    .stream()
                    .collect(Collectors.groupingBy(org.jboss.pnc.model.Artifact::getSha256));
            Iterator<Artifact> iterator = notFoundArtifacts.iterator();
            while (iterator.hasNext()) {
                Artifact art = iterator.next();
                stats.notFoundCounter().accept(art);
                org.jboss.pnc.model.Artifact artifact = findOrCreateNotFoundArtifact(
                        art,
                        distributionRepository,
                        user,
                        existingArtifactsBySha256);
                addDeliveredArtifact(
                        artifact,
                        report,
                        false,
                        art.getArchiveFilenames(),
                        art.getArchiveUnmatchedFilenames(),
                        art.getLicenses(),
                        distribution);
            }
        }

        stats.log(distributionUrl.toString());
    }

    private void addDeliveredArtifact(
            org.jboss.pnc.model.Artifact artifact,
            DeliverableAnalyzerReport report,
            boolean builtFromSource,
            Collection<String> archiveFilenames,
            Collection<String> archiveUnmatchedFilenames,
            Collection<LicenseInfo> licenseInfo,
            DeliverableAnalyzerDistribution distribution) {

        DeliverableArtifact deliverableArtifact = DeliverableArtifact.builder()
                .artifact(artifact)
                .report(report)
                .builtFromSource(builtFromSource)
                .archiveFilenames(StringUtils.joinArray(archiveFilenames))
                .archiveUnmatchedFilenames(StringUtils.joinArray(archiveUnmatchedFilenames))
                .distribution(distribution)
                .build();
        report.addDeliverableArtifact(deliverableArtifact);
        // distribution.addDeliverableArtifact(deliverableArtifact);

        deliverableArtifactRepository.save(deliverableArtifact);

        Set<DeliverableArtifactLicenseInfo> licenses = Optional.ofNullable(licenseInfo)
                .orElse(Collections.emptySet())
                .stream()
                .map(license -> {
                    return toEntity(license, deliverableArtifact);
                })
                .collect(Collectors.toSet());

        for (DeliverableArtifactLicenseInfo licenseEntity : licenses) {
            deliverableArtifactLicenseInfoRepository.save(licenseEntity);
            deliverableArtifact.addDeliverableArtifactLicenseInfo(licenseEntity);
        }

        log.debug("Added delivered artifact {}", deliverableArtifact);
    }

    private static DeliverableArtifactLicenseInfo toEntity(
            LicenseInfo license,
            DeliverableArtifact deliverableArtifact) {
        return DeliverableArtifactLicenseInfo.builder()
                .id(new Base32LongID(Sequence.nextBase32Id()))
                .comments(license.getComments())
                .distribution(license.getDistribution())
                .name(license.getName())
                .spdxLicenseId(license.getSpdxLicenseId())
                .url(license.getUrl())
                .sourceUrl(license.getSourceUrl())
                .source(LicenseSource.UNKNOWN) // set default value (it was replaced by sourceUrl)
                .artifact(deliverableArtifact)
                .build();
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
                .user(report.getOperation().getUser())
                .reason("Analysis run as scratch.")
                .change(LabelOperation.ADDED)
                .label(DeliverableAnalyzerReportLabel.SCRATCH)
                .build();
        deliverableAnalyzerLabelEntryRepository.save(labelHistoryEntry);
        report.getLabelHistory().add(labelHistoryEntry);
    }

    private org.jboss.pnc.model.Artifact findOrCreateNotFoundArtifact(
            Artifact artifact,
            TargetRepository targetRepo,
            User user,
            Map<String, List<org.jboss.pnc.model.Artifact>> existingArtifactsBySha256) {

        // The artifact was not built from source, but could already be present as a dependency recorded in PNC system.
        // To avoid unnecessary artifact duplication (see NCLSUP-990), we will search for a best matching artifact.
        // PLEASE NOTE that we don't know much about such artifacts, for example whether they are Maven based. We really
        // know just their SHA and their filename. Their identifier is their filename.
        Path path = Paths.get(artifact.getFilename());
        String filename = path.getFileName().toString();

        // Use the prefetched artifacts with the same SHA-256 and filter for its name. If no matches are found, create
        // the artifact. Yes, if an artifact was renamed in the ZIP, we will create a new entry in the DB.
        List<org.jboss.pnc.model.Artifact> artifacts = existingArtifactsBySha256
                .getOrDefault(artifact.getSha256(), Collections.emptyList())
                .stream()
                .filter(art -> art.getFilename().equals(filename))
                .collect(Collectors.toList());
        if (artifacts.size() == 1) {
            return artifacts.iterator().next();
        }

        // There can be multiple artifacts found with same sha256 and filename (e.g. same "pom.xml" in different jar,
        // sources.jar, test-sources.jar), let's keep filtering to avoid unique constraint errors (NCL-8718).
        // We will filter by same target repository (same distributionUrl in this case of not found artifacts), and
        // finally by same identifier
        artifacts = artifacts.stream()
                .filter(art -> art.getTargetRepository().equals(targetRepo))
                .collect(Collectors.toList());
        if (artifacts.size() == 1) {
            return artifacts.iterator().next();
        }
        artifacts = artifacts.stream()
                .filter(art -> art.getIdentifier().equals(artifact.getFilename()))
                .collect(Collectors.toList());
        if (artifacts.size() == 1) {
            return artifacts.iterator().next();
        }

        // There was no artifact found with the same SHA-256, filename, target repo and identifier. We can create a new
        // one. Register it so subsequent not-found artifacts in this deliverable with the same SHA-256 reuse it
        // instead of creating a duplicate (the DB is no longer queried per artifact).
        org.jboss.pnc.model.Artifact created = createArtifact(mapNotFoundArtifact(artifact, user), targetRepo);
        existingArtifactsBySha256.computeIfAbsent(artifact.getSha256(), k -> new ArrayList<>()).add(created);
        return created;
    }

    private org.jboss.pnc.model.Artifact createArtifact(
            org.jboss.pnc.model.Artifact artifact,
            TargetRepository targetRepo) {
        artifact.setTargetRepository(targetRepo);
        artifact.setPurl(createGenericPurl(artifact.getFilename(), artifact.getSha256()));
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

    /**
     * Compute the purl string for a generic download, that does not match package type specific files structure. See
     * <a href=
     * "https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#generic">https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#generic</a>.
     *
     * @param filename the artifact filename
     * @param sha256 the SHA-256 of the artifact
     * @return the generated purl
     */
    private String createGenericPurl(String filename, String sha256) {
        try {
            PackageURLBuilder purlBuilder = PackageURLBuilder.aPackageURL()
                    .withType(PackageURL.StandardTypes.GENERIC)
                    .withName(filename)
                    .withQualifier("checksum", "sha256:" + sha256);
            return purlBuilder.build().toString();
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
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

    private DeliverableAnalyzerDistribution getDistribution(String distURL, Artifact artifact) {
        DeliverableAnalyzerDistribution distribution = (artifact == null)
                ? deliverableAnalyzerDistributionRepository.queryByUrl(distURL)
                : deliverableAnalyzerDistributionRepository.queryByUrlAndSha256(distURL, artifact.getSha256());
        if (distribution == null) {
            distribution = createDistribution(distURL, artifact);
        }
        return distribution;
    }

    private DeliverableAnalyzerDistribution createDistribution(String url, Artifact artifact) {
        DeliverableAnalyzerDistribution distro = DeliverableAnalyzerDistribution.builder()
                .distributionUrl(url)
                .artifacts(new HashSet<>())
                .md5(artifact != null ? artifact.getMd5() : null)
                .sha1(artifact != null ? artifact.getSha1() : null)
                .sha256(artifact != null ? artifact.getSha256() : null)
                .build();
        return deliverableAnalyzerDistributionRepository.save(distro);
    }

    private void startAnalysis(
            String milestoneId,
            List<String> deliverablesUrls,
            boolean runAsScratchAnalysis,
            Base32LongID operationId) {
        Request callback = operationsManager.getOperationCallback(operationId);
        String id = operationId.getId();

        DingroguDeliverablesAnalysisDTO dto = DingroguDeliverablesAnalysisDTO.builder()
                .operationId(id)
                .urls(deliverablesUrls)
                .callback(callback)
                .deliverablesAnalyzerUrl(globalConfig.getExternalDeliverablesAnalyzerUrl())
                .orchUrl(globalConfig.getPncUrl())
                .scratch(runAsScratchAnalysis)
                .build();
        dingroguClient.submitDeliverablesAnalysis(dto);
        DeliverableAnalysisStatusChangedEvent analysisStatusChanged = DefaultDeliverableAnalysisStatusChangedEvent
                .started(id, milestoneId, deliverablesUrls);
        analysisStatusChangedEventNotifier.fireAsync(analysisStatusChanged);
    }

    public void observeEvent(@ObservesAsync OperationChangedEvent event) {
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
        analysisStatusChangedEventNotifier.fireAsync(analysisStatusChanged);
    }

    private class ArtifactStats {
        int totalArtifacts = 0;
        int pncArtifactsCount = 0;
        int pncNotBuiltArtifactsCount = 0;
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

        public Consumer<Artifact> notFoundCounter() {
            return a -> {
                totalArtifacts++;
                notFoundArtifactsCount++;
            };
        }

        public void log(String distributionUrl) {
            log.info("Processed {} artifacts from deliverables at {}: ", totalArtifacts, distributionUrl);
            log.info(
                    "  PNC artifacts: {} ({} artifacts not built from source), other artifacts not built from source: {} ",
                    pncArtifactsCount,
                    pncNotBuiltArtifactsCount,
                    notFoundArtifactsCount);
            int totalNotBuild = pncNotBuiltArtifactsCount + notFoundArtifactsCount;
            if (totalNotBuild > 0) {
                log.info("  There are total {} artifacts not built from source!", totalNotBuild);
            }
        }
    }

    private class ArtifactCache {

        private Map<Integer, org.jboss.pnc.model.Artifact> pncCache = new HashMap<>();

        private User user;

        public ArtifactCache(Collection<Build> builds, User user) {
            this.user = user;
            prefetchPNCArtifacts(builds);
        }

        private void prefetchPNCArtifacts(Collection<Build> builds) {
            log.debug("Preloading PNC artifacts...");

            Set<Integer> ids = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.PNC)
                    .flatMap(b -> b.getArtifacts().stream())
                    .map(Artifact::getPncId)
                    .map(artifactMapper.getIdMapper()::toEntity)
                    .collect(Collectors.toSet());

            if (!ids.isEmpty()) {
                pncCache = artifactRepository.queryWithPredicates(ArtifactPredicates.withIds(ids))
                        .stream()
                        .collect(Collectors.toMap(org.jboss.pnc.model.Artifact::getId, Function.identity()));
            }
            log.debug("Preloaded {} PNC artifacts to cache.", pncCache.size());
        }

        public org.jboss.pnc.model.Artifact findPNCArtifact(Artifact art) {
            org.jboss.pnc.model.Artifact artifact = pncCache.get(artifactMapper.getIdMapper().toEntity(art.getPncId()));
            if (artifact == null) {
                throw new IllegalArgumentException("PNC artifact with id " + art.getPncId() + " doesn't exist.");
            }
            return artifact;
        }
    }

}
