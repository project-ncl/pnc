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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_MAVEN;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;

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

        ArtifactCache artifactCache = new ArtifactCache(builds);
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
                    artifactParser = artifactCache::findPNCArtifact;
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
            build.getArtifacts()
                    .stream()
                    .peek(statCounter)
                    .map(artifactParser)
                    .peek(artifactUpdater)
                    .forEach(milestone::addDeliveredArtifact);
        }
        if (!notFoundArtifacts.isEmpty()) {
            TargetRepository distributionRepository = getDistributionRepository(distributionUrl);
            notFoundArtifacts.stream()
                    .peek(stats.notFoundCounter())
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
    @RolesAllowed(SYSTEM_USER)
    @Transactional
    public void clear(int id) {
        ProductMilestone milestone = milestoneRepository.queryById(id);
        milestone.getDeliveredArtifacts().forEach(artifactUpdater("Removed from deliverables of milestone " + id));
        milestone.getDeliveredArtifacts().clear();
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
        artifact.setPurl(createGenericPurl(targetRepo.getRepositoryPath(), artifact.getFilename().toString(), artifact.getSha256()));
        org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
        targetRepo.getArtifacts().add(savedArtifact);
        return savedArtifact;
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

    private org.jboss.pnc.model.Artifact mapBrewArtifact(
            Artifact artifact,
            String nvr,
            TargetRepository targetRepository) {
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
        builder.targetRepository(targetRepository);

        return builder.build();
    }

    private org.jboss.pnc.model.Artifact.Builder mapArtifact(Artifact artifact) {
        Date now = new Date();
        org.jboss.pnc.model.Artifact.Builder builder = org.jboss.pnc.model.Artifact.builder();
        builder.md5(artifact.getMd5());
        builder.sha1(artifact.getSha1());
        builder.sha256(artifact.getSha256());
        builder.size(artifact.getSize());
        builder.importDate(now);
        builder.creationUser(userService.currentUser());
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

    /**
     * Compute the purl string for a generic download, that does not match package type specific files structure. See
     * https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst#generic
     *
     * @param downloadUrl url where the artifact was downloaded from
     * @param filename the artifact filename
     * @param sha256 the SHA-256 of the artifact
     * @return the generated purl
     * @throws MalformedPackageURLException
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
                    milestoneId,
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

        public ArtifactCache(Collection<Build> builds) {
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
                        .map(a -> mapBrewArtifact(a, build.getBrewNVR(), targetRepository))
                        .map(a -> new IdentifierShaRepo(a.getIdentifierSha256(), targetRepository));
            }
        }

        private String getKojiPath(Build build) {
            return KOJI_PATH_MAVEN_PREFIX + build.getBrewNVR();
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
            return findOrCreateBrewArtifact(mapBrewArtifact(artifact, nvr, targetRepository));
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
