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
import org.jboss.pnc.api.deliverablesanalyzer.dto.ArtifactType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.api.deliverablesanalyzer.dto.MavenArtifact;
import org.jboss.pnc.api.dto.Request;
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
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.jboss.pnc.spi.exception.ProcessManagerException;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_MAVEN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;

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
    public DeliverableAnalyzerOperation analyzeDeliverables(String id, List<String> deliverablesUrls) {
        int i = 1;
        Map<String, String> inputParams = new HashMap<>();
        for (String url : deliverablesUrls) {
            inputParams.put(URL_PARAMETER_PREFIX + (i++), url);
        }

        Base32LongID operationId = operationsManager.newDeliverableAnalyzerOperation(id, inputParams).getId();

        try {
            log.info("Starting analysis of deliverables for milestone {} from urls: {}.", id, deliverablesUrls);
            startAnalysis(id, deliverablesUrls, operationId);
            return deliverableAnalyzerOperationMapper.toDTO(
                    (org.jboss.pnc.model.DeliverableAnalyzerOperation) operationsManager
                            .updateProgress(operationId, ProgressStatus.IN_PROGRESS));
        } catch (RuntimeException ex) {
            operationsManager.setResult(operationId, OperationResult.SYSTEM_ERROR);
            throw ex;
        }
    }

    private void processDeliverables(
            int milestoneId,
            Collection<Build> builds,
            String distributionUrl,
            Collection<Artifact> notFoundArtifacts) {
        log.debug(
                "Processing deliverables of milestone {} in {} builds. Distribution URL: {}",
                milestoneId,
                builds.size(),
                distributionUrl);
        ProductMilestone milestone = milestoneRepository.queryById(milestoneId);
        Consumer<org.jboss.pnc.model.Artifact> artifactUpdater = artifactUpdater(
                "Added as delivered artifact for milestone " + milestoneId);
        ArtifactStats stats = new ArtifactStats();
        for (Build build : builds) {
            log.debug("Processing build {}", build);
            Function<Artifact, org.jboss.pnc.model.Artifact> artifactParser;
            Consumer<Artifact> statCounter;
            if (build.getBuildSystemType() == null) {
                throw new IllegalArgumentException("Build system type not set.");
            }
            switch (build.getBuildSystemType()) {
                case PNC:
                    statCounter = stats.pncCounter();
                    artifactParser = this::getPncArtifact;
                    break;
                case BREW:
                    statCounter = stats.brewCounter();
                    TargetRepository brewRepository = getBrewRepository(build);
                    artifactParser = art -> findOrCreateBrewArtifact(
                            assertBrewArtifacts(art),
                            brewRepository,
                            build.getBrewNVR());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown build system type " + build.getBuildSystemType());
            }
            build.getArtifacts()
                    .stream()
                    .peek(statCounter)
                    .map(artifactParser)
                    .peek(artifactUpdater)
                    .forEach(milestone::addDeliveredArtifact);
        }
        if (!notFoundArtifacts.isEmpty()) {
            TargetRepository distributionRepository = getDistributionRepository(distributionUrl);
            stats.notFoundArtifactsCount = notFoundArtifacts.size();
            notFoundArtifacts.stream()
                    .map(art -> findOrCreateNotFoundArtifact(art, distributionRepository))
                    .peek(artifactUpdater)
                    .forEach(milestone::addDeliveredArtifact);
        }
        stats.log(distributionUrl);
        milestone.setDeliveredArtifactsImporter(userService.currentUser());
    }

    public Consumer<org.jboss.pnc.model.Artifact> artifactUpdater(String message) {
        User user = userService.currentUser();
        return a -> {
            a.setQualityLevelReason(message);
            a.setModificationUser(user);
            a.setModificationTime(new Date());
        };
    }

    @Override
    @Transactional
    public void completeAnalysis(int milestoneId, List<FinderResult> results) {
        log.info("Processing deliverables of milestone {} in {} results.", milestoneId, results.size());
        for (FinderResult finderResult : results) {
            processDeliverables(
                    milestoneId,
                    finderResult.getBuilds(),
                    finderResult.getUrl().toString(),
                    finderResult.getNotFoundArtifacts());
        }
    }

    @Override
    @RolesAllowed(USERS_ADMIN)
    @Transactional
    public void clear(int id) {
        ProductMilestone milestone = milestoneRepository.queryById(id);
        milestone.getDeliveredArtifacts().forEach(artifactUpdater("Removed from deliverables of milestone " + id));
        milestone.getDeliveredArtifacts().clear();
    }

    private org.jboss.pnc.model.Artifact findOrCreateBrewArtifact(
            Artifact artifact,
            TargetRepository targetRepo,
            String nvr) {
        return findOrCreateArtifact(mapBrewArtifact(artifact, nvr), targetRepo);
    }

    private org.jboss.pnc.model.Artifact findOrCreateNotFoundArtifact(Artifact artifact, TargetRepository targetRepo) {
        return findOrCreateArtifact(mapNotFoundArtifact(artifact), targetRepo);
    }

    private org.jboss.pnc.model.Artifact findOrCreateArtifact(
            org.jboss.pnc.model.Artifact artifact,
            TargetRepository targetRepo) {
        // find
        org.jboss.pnc.model.Artifact dbArtifact = artifactRepository.queryByPredicates(
                ArtifactPredicates.withIdentifierAndSha256(artifact.getIdentifier(), artifact.getSha256()),
                ArtifactPredicates.withTargetRepositoryId(targetRepo.getId()));
        if (dbArtifact != null) {
            return dbArtifact;
        }

        // create
        artifact.setTargetRepository(targetRepo);
        org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
        targetRepo.getArtifacts().add(savedArtifact);
        return savedArtifact;
    }

    private org.jboss.pnc.model.Artifact getPncArtifact(Artifact art) {
        org.jboss.pnc.model.Artifact artifact = artifactRepository
                .queryById(artifactMapper.getIdMapper().toEntity(art.getPncId()));
        if (artifact == null) {
            throw new IllegalArgumentException("PNC artifact with id " + art.getPncId() + " doesn't exist.");
        }
        return artifact;
    }

    private org.jboss.pnc.model.Artifact mapNotFoundArtifact(Artifact artifact) {
        org.jboss.pnc.model.Artifact.Builder builder = mapArtifact(artifact);
        Path path = Paths.get(artifact.getFilename());
        builder.filename(path.getFileName().toString());
        builder.identifier(artifact.getFilename());
        Path directory = path.getParent();
        builder.deployPath(directory == null ? null : directory.toString());

        return builder.build();
    }

    private org.jboss.pnc.model.Artifact mapBrewArtifact(Artifact artifact, String nvr) {
        if (artifact.getArtifactType() != ArtifactType.MAVEN) {
            throw new UnsupportedOperationException("Brew artifact " + artifact + " is not Maven!");
        }
        MavenArtifact mavenArtifact = (MavenArtifact) artifact;

        org.jboss.pnc.model.Artifact.Builder builder = mapArtifact(mavenArtifact);
        builder.identifier(createIdentifier(mavenArtifact));
        builder.filename(createFileName(mavenArtifact));
        builder.deployPath(createDeployPath(mavenArtifact));
        builder.originUrl(createBrewOriginURL(mavenArtifact, nvr));
        builder.purl(createPURL(mavenArtifact));

        return builder.build();
    }

    private org.jboss.pnc.model.Artifact.Builder mapArtifact(Artifact artifact) {
        org.jboss.pnc.model.Artifact.Builder builder = org.jboss.pnc.model.Artifact.builder();
        builder.md5(artifact.getMd5());
        builder.sha1(artifact.getSha1());
        builder.sha256(artifact.getSha256());
        builder.size(artifact.getSize());
        builder.importDate(new Date());
        builder.creationUser(userService.currentUser());
        builder.creationTime(new Date());

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
        if (Strings.isEmpty(mavenArt.getClassifier())) {
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

    private TargetRepository getBrewRepository(Build build) {
        String path = KOJI_PATH_MAVEN_PREFIX + build.getBrewNVR();
        TargetRepository tr = targetRepositoryRepository.queryByIdentifierAndPath(INDY_MAVEN, path);
        if (tr == null) {
            tr = createRepository(path, INDY_MAVEN, RepositoryType.MAVEN);
        }
        return tr;
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
                .build();
        return targetRepositoryRepository.save(tr);
    }

    private Artifact assertBrewArtifacts(Artifact artifact) {
        if (!(artifact.getArtifactType() == null || artifact.getArtifactType() == ArtifactType.MAVEN)) {
            throw new IllegalArgumentException(
                    "Brew artifacts are expected to be either MAVEN or unknown, artifact " + artifact + " is "
                            + artifact.getArtifactType());
        }
        return artifact;
    }

    private void startAnalysis(String milestoneId, List<String> deliverablesUrls, Base32LongID operationId) {
        Request callback = operationsManager.getOperationCallback(operationId);
        String id = operationId.getId();
        try {
            AnalyzeDeliverablesBpmRequest bpmRequest = new AnalyzeDeliverablesBpmRequest(
                    id,
                    milestoneId,
                    deliverablesUrls);
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

        public void log(String distributionUrl) {
            log.info("Processed {} artifacts from deliverables at {}: ", totalArtifacts, distributionUrl);
            log.info(
                    "  PNC artifacts: {} ({} artifacts not built from source), BREW artifacts: {} ({} artifacts not built from source), not found artifacts: {} ",
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
}
