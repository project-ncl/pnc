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
import org.jboss.pnc.api.deliverablesanalyzer.dto.LicenseInfo;
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
import org.jboss.pnc.common.concurrent.Sequence;
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
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactLicenseInfo;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.Artifact.IdentifierSha256;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerDistributionRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.jboss.pnc.spi.exception.ProcessManagerException;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.net.URL;
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
import java.util.Iterator;
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
                    finderResult.getUrl(),
                    finderResult.getNotFoundArtifacts());
        }
    }

    private Artifact findDistributionUrlAssociatedArtifact(
            URL distributionUrl,
            Collection<Build> builds,
            Collection<Artifact> notFoundArtifacts) {
        // Find the url filename
        String urlFilename = Paths.get(distributionUrl.getPath()).getFileName().toString();

        // Loop in the builds to find the artifact associated with the url
        Optional<Artifact> distributionArtifact = builds.stream()
                .flatMap(b -> b.getArtifacts().stream())
                .filter(a -> a.getFilename().equals(urlFilename))
                .findFirst();

        if (distributionArtifact.isPresent()) {
            return distributionArtifact.get();
        }

        // If the artifact was not found among the matched builds (meaning the zip was built in PNC or Brew), then look
        // in the not found artifacts list (meaning the zip was built in e.g. Jenkins)
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
                case BREW:
                    statCounter = stats.brewCounter();
                    // The artifact comes from a Brew build (either a proper build or an import). We need to search
                    // among existing PNC artifacts similarly to what we do with not found artifacts, to match the
                    // best existing ones (if any). If the Brew build is a proper build (not an import), it might be
                    // that we already have a PNC dependency from MRRC which is also found in Brew. In this case, we
                    // want to reuse the existing MRRC artifact. If the Brew build is an import, we want to still
                    // pioritize the existing PNC dependencies.
                    artifactParser = art -> findOrCreateBrewArtifact(art, user, artifactCache, build);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown build system type " + build.getBuildSystemType());
            }
            build.getArtifacts().stream().peek(statCounter).forEach(artifactDto -> {
                addDeliveredArtifact(
                        artifactParser.apply(artifactDto),
                        report,
                        artifactDto.isBuiltFromSource(),
                        build.getBrewId(),
                        artifactDto.getArchiveFilenames(),
                        artifactDto.getArchiveUnmatchedFilenames(),
                        artifactDto.getLicenses(),
                        distribution);
            });
        }

        /*
         * Not found artifacts are artifacts which were not built in PNC and not built in Brew (either properly built or
         * imported)
         */
        if (!notFoundArtifacts.isEmpty()) {
            TargetRepository distributionRepository = getDistributionRepository(distributionUrl.toString());
            Iterator<Artifact> iterator = notFoundArtifacts.iterator();
            while (iterator.hasNext()) {
                Artifact art = iterator.next();
                stats.notFoundCounter().accept(art);
                org.jboss.pnc.model.Artifact artifact = findOrCreateNotFoundArtifact(art, distributionRepository, user);
                addDeliveredArtifact(
                        artifact,
                        report,
                        false,
                        null,
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
            Long brewBuildId,
            Collection<String> archiveFilenames,
            Collection<String> archiveUnmatchedFilenames,
            Collection<LicenseInfo> licenseInfo,
            DeliverableAnalyzerDistribution distribution) {

        DeliverableArtifact deliverableArtifact = DeliverableArtifact.builder()
                .artifact(artifact)
                .report(report)
                .builtFromSource(builtFromSource)
                .brewBuildId(brewBuildId)
                .archiveFilenames(StringUtils.joinArray(archiveFilenames))
                .archiveUnmatchedFilenames(StringUtils.joinArray(archiveUnmatchedFilenames))
                .distribution(distribution)
                .build();
        report.addDeliverableArtifact(deliverableArtifact);
        // distribution.addDeliverableArtifact(deliverableArtifact);

        Set<DeliverableArtifactLicenseInfo> licenses = Optional.ofNullable(licenseInfo)
                .orElse(Collections.emptySet())
                .stream()
                .map(license -> {
                    return toEntity(license, deliverableArtifact);
                })
                .collect(Collectors.toSet());

        deliverableArtifact.setLicenses(licenses);
        deliverableArtifactRepository.save(deliverableArtifact);
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
                .source(license.getSource())
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
            Collection<org.jboss.pnc.model.Artifact> artifacts,
            boolean isImport) {
        if (artifacts == null || artifacts.isEmpty()) {
            return Optional.empty();
        }

        Function<org.jboss.pnc.model.Artifact, Integer> artifactRatingFunction = isImport
                ? DeliverableAnalyzerManagerImpl::getNotBuiltArtifactRating
                : DeliverableAnalyzerManagerImpl::getBuiltArtifactRating;

        return artifacts.stream().sorted(Comparator.comparing(artifactRatingFunction).reversed()).findFirst();
    }

    private static Integer getNotBuiltArtifactRating(org.jboss.pnc.model.Artifact artifact) {
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
                log.warn("Unsupported ArtifactQuality! Got: {} for artifact: {}", quality, artifact);
                return -100;
        }
    }

    private static Integer getBuiltArtifactRating(org.jboss.pnc.model.Artifact artifact) {
        ArtifactQuality quality = artifact.getArtifactQuality();

        switch (quality) {
            case NEW:
                return 1;
            case VERIFIED:
                return 2;
            case TESTED:
                return 3;
            case DEPRECATED:
                return -1;
            case BLACKLISTED:
                return -2;
            case TEMPORARY:
                return -3;
            case DELETED:
                return -4;
            default:
                log.warn("Unsupported ArtifactQuality! Got: {} for artifact: {}", quality, artifact);
                return -100;
        }
    }

    private org.jboss.pnc.model.Artifact findOrCreateBrewArtifact(
            Artifact artifact,
            User user,
            ArtifactCache artifactCache,
            Build build) {

        // This is a Brew artifact, so let's convert it to get the identifier, filename etc initialized.
        // The target repository can be left null for now, in case we need to create the artifact we will deal with it
        org.jboss.pnc.model.Artifact brewArtifact = mapBrewArtifact(artifact, build.getBrewNVR(), null, user);
        return artifactCache.findOrCreateBrewArtifact(brewArtifact, build);
    }

    private org.jboss.pnc.model.Artifact findOrCreateNotFoundArtifact(
            Artifact artifact,
            TargetRepository targetRepo,
            User user) {

        // The artifact was not built from source, but could already be present as a dependency recorded in PNC system.
        // To avoid unnecessary artifact duplication (see NCLSUP-990), we will search for a best matching artifact.
        // PLEASE NOTE that we don't know much about such artifacts, for example whether they are Maven based. We really
        // know just their SHA and their filename. Their identifier is their filename.
        Path path = Paths.get(artifact.getFilename());
        String filename = path.getFileName().toString();

        // Search for artifacts with the same SHA-256 and filter for its name. If no matches are found, create the
        // artifact. Yes, if an artifact was renamed in the ZIP, we will create a new entry in the DB.
        List<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                .withSha256In(Collections.singleton(artifact.getSha256()));
        artifacts = artifacts.stream().filter(art -> art.getFilename().equals(filename)).collect(Collectors.toList());
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
        // one.
        return createArtifact(mapNotFoundArtifact(artifact, user), targetRepo);
    }

    private org.jboss.pnc.model.Artifact createArtifact(
            org.jboss.pnc.model.Artifact artifact,
            TargetRepository targetRepo) {

        artifact.setTargetRepository(targetRepo);
        artifact.setPurl(createGenericPurl(artifact.getFilename().toString(), artifact.getSha256()));
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

        private Map<IdentifierSha256, org.jboss.pnc.model.Artifact> brewCache = new HashMap<>();
        private Map<String, TargetRepository> targetRepositoryCache = new HashMap<>();

        private User user;

        public ArtifactCache(Collection<Build> builds, User user) {
            this.user = user;
            prefetchPNCArtifacts(builds);
            prefetchTargetRepos(builds);
            prefetchBrewArtifacts(builds);
            prefetchBrewImportedArtifacts(builds);
        }

        private void prefetchPNCArtifacts(Collection<Build> builds) {
            log.debug("Preloading PNC artifacts...");

            Set<Integer> ids = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.PNC)
                    .flatMap(b -> b.getArtifacts().stream())
                    .map(a -> a.getPncId())
                    .map(artifactMapper.getIdMapper()::toEntity)
                    .collect(Collectors.toSet());

            if (!ids.isEmpty()) {
                pncCache = artifactRepository.queryWithPredicates(ArtifactPredicates.withIds(ids))
                        .stream()
                        .collect(Collectors.toMap(org.jboss.pnc.model.Artifact::getId, Function.identity()));
            }
            log.debug("Preloaded {} PNC artifacts to cache.", pncCache.size());
        }

        private void prefetchTargetRepos(Collection<Build> builds) {
            log.debug("Preloading target repos...");

            Set<TargetRepository.IdentifierPath> queries = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.BREW)
                    .map(this::getKojiPath)
                    .map(path -> new TargetRepository.IdentifierPath(INDY_MAVEN, path))
                    .collect(Collectors.toSet());

            if (!queries.isEmpty()) {
                List<TargetRepository> targetRepositories = targetRepositoryRepository
                        .queryByIdentifiersAndPaths(queries);
                for (TargetRepository targetRepository : targetRepositories) {
                    targetRepositoryCache.put(targetRepository.getRepositoryPath(), targetRepository);
                }
            }
            log.debug("Preloaded {} target repos to cache.", targetRepositoryCache.size());
        }

        private void prefetchBrewArtifacts(Collection<Build> builds) {
            log.debug("Preloading brew artifacts...");
            int initialCacheSize = brewCache.size();

            Set<IdentifierSha256> identifierSha256Set = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.BREW)
                    .filter(b -> !b.isImport())
                    .flatMap(this::prefetchBrewBuild)
                    .collect(Collectors.toSet());

            if (!identifierSha256Set.isEmpty()) {
                Set<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                        .withIdentifierAndSha256(identifierSha256Set);
                // Search for all artifacts with the provided SHA-256 and identifiers.
                // If more than one artifact is found for the same SHA-256 and identifier (should not happen for Maven
                // artifacts!), find a best match.
                Map<IdentifierSha256, List<org.jboss.pnc.model.Artifact>> groupedByIdentifierSha256 = artifacts.stream()
                        .collect(Collectors.groupingBy(org.jboss.pnc.model.Artifact::getIdentifierSha256));
                groupedByIdentifierSha256.forEach(
                        (key, matchedArtifacts) -> brewCache
                                .put(key, getBestMatchingArtifact(matchedArtifacts, false).get()));
            }
            log.debug(
                    "Preloaded {} brew artifacts to cache, total cache size: {}.",
                    brewCache.size() - initialCacheSize,
                    brewCache.size());
        }

        private void prefetchBrewImportedArtifacts(Collection<Build> builds) {
            log.debug("Preloading brew imported artifacts...");
            int initialCacheSize = brewCache.size();

            Set<IdentifierSha256> identifierSha256Set = builds.stream()
                    .filter(b -> b.getBuildSystemType() == BuildSystemType.BREW)
                    .filter(b -> b.isImport())
                    .flatMap(this::prefetchBrewBuild)
                    .collect(Collectors.toSet());

            if (!identifierSha256Set.isEmpty()) {
                Set<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                        .withIdentifierAndSha256(identifierSha256Set);
                // Search for all artifacts with the provided SHA-256 and identifiers.
                // If more than one artifact is found for the same SHA-256 and identifier (should not happen for Maven
                // artifacts!), find a best match.
                Map<IdentifierSha256, List<org.jboss.pnc.model.Artifact>> groupedByIdentifierSha256 = artifacts.stream()
                        .collect(Collectors.groupingBy(org.jboss.pnc.model.Artifact::getIdentifierSha256));
                groupedByIdentifierSha256.forEach(
                        (key, matchedArtifacts) -> brewCache
                                .put(key, getBestMatchingArtifact(matchedArtifacts, true).get()));
            }
            log.debug(
                    "Preloaded {} brew imported artifacts to cache, total cache size: {}.",
                    brewCache.size() - initialCacheSize,
                    brewCache.size());
        }

        public Stream<IdentifierSha256> prefetchBrewBuild(Build build) {
            return build.getArtifacts()
                    .stream()
                    .peek(this::assertBrewArtifacts)
                    .map(a -> mapBrewArtifact(a, build.getBrewNVR(), null, user))
                    .map(a -> a.getIdentifierSha256());
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

        private org.jboss.pnc.model.Artifact findOrCreateBrewArtifact(
                org.jboss.pnc.model.Artifact artifact,
                Build build) {
            org.jboss.pnc.model.Artifact cachedArtifact = brewCache.get(artifact.getIdentifierSha256());
            if (cachedArtifact != null) {
                return cachedArtifact;
            }

            // Otherwise, we need to create this artifact.
            TargetRepository brewRepository = findOrCreateTargetRepository(build);
            artifact.setTargetRepository(brewRepository);
            org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
            brewRepository.getArtifacts().add(savedArtifact);
            brewCache.put(artifact.getIdentifierSha256(), savedArtifact);
            return savedArtifact;
        }
    }

}
