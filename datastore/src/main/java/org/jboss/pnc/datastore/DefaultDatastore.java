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
package org.jboss.pnc.datastore;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.CollectionUtils.ofNullableCollection;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierInAndBuilt;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;

@Stateless
public class DefaultDatastore implements Datastore {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ArtifactRepository artifactRepository;

    private BuildRecordRepository buildRecordRepository;

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private UserRepository userRepository;

    private TargetRepositoryRepository targetRepositoryRepository;

    public DefaultDatastore() {
    }

    @Inject
    public DefaultDatastore(
            ArtifactRepository artifactRepository,
            BuildRecordRepository buildRecordRepository,
            BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            UserRepository userRepository,
            TargetRepositoryRepository targetRepositoryRepository) {
        this.artifactRepository = artifactRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.userRepository = userRepository;
        this.targetRepositoryRepository = targetRepositoryRepository;
    }

    private static final String ARTIFACT_ALREADY_BUILT_CONFLICT_MESSAGE = "This artifact was already built in build #";

    @Override
    public Map<Artifact, String> checkForBuiltArtifacts(Collection<Artifact> artifacts) {
        Map<RepositoryType, Map<String, Artifact>> repoTypes = new HashMap<>();
        for (Artifact artifact : artifacts) {
            RepositoryType repoType = artifact.getTargetRepository().getRepositoryType();
            if (!repoTypes.containsKey(repoType)) {
                repoTypes.put(repoType, new HashMap<>());
            }
            Map<String, Artifact> identifiers = repoTypes.get(repoType);
            identifiers.put(artifact.getIdentifier(), artifact);
        }

        Map<Artifact, String> conflicts = new HashMap<>();
        for (RepositoryType repoType : repoTypes.keySet()) {
            Map<String, Artifact> identifiers = repoTypes.get(repoType);
            List<Artifact> conflicting = artifactRepository
                    .queryWithPredicates(withIdentifierInAndBuilt(identifiers.keySet()));
            for (Artifact conflict : conflicting) {
                if (conflict.getTargetRepository().getRepositoryType() == repoType) {
                    Artifact artifact = identifiers.get(conflict.getIdentifier());
                    conflicts
                            .put(artifact, ARTIFACT_ALREADY_BUILT_CONFLICT_MESSAGE + conflict.getBuildRecord().getId());
                }
            }
        }
        return conflicts;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public BuildRecord storeCompletedBuild(
            BuildRecord.Builder buildRecordBuilder,
            List<Artifact> builtArtifacts,
            List<Artifact> dependencies) {
        BuildRecord buildRecord = buildRecordBuilder.build(true);
        logger.debug("Storing completed build {}.", buildRecord);
        BuildRecord previouslySavedBuild = buildRecordRepository.queryById(buildRecord.getId());
        if (previouslySavedBuild != null) {
            throw new IllegalStateException(
                    "When trying to save build " + buildRecord + " previously saved build with status "
                            + previouslySavedBuild.getStatus() + " was found.");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Build Log: {}.", buildRecord.getBuildLog());
        }

        Map<TargetRepository.IdentifierPath, TargetRepository> repositoriesCache = new HashMap<>();
        Map<Artifact.IdentifierSha256, Artifact> artifactCache = new HashMap<>();

        /**
         * Built artifacts must be saved before the dependencies. In case an artifact is built and the dependency
         * (re-downloaded), it must be linked to built artifacts repository.
         */
        logger.debug("Saving built artifacts ...");
        final Set<Artifact> savedBuiltArtifacts = saveArtifacts(builtArtifacts, repositoriesCache, artifactCache);

        logger.debug("Saving dependencies ...");
        buildRecord.setDependencies(saveArtifacts(dependencies, repositoriesCache, artifactCache));

        logger.debug("Done saving artifacts.");
        logger.trace("Saving build record {}.", buildRecord);
        buildRecord = buildRecordRepository.save(buildRecord);
        logger.debug("Build record {} saved.", buildRecord.getId());

        ArtifactQuality quality = buildRecord.isTemporaryBuild() ? ArtifactQuality.TEMPORARY : ArtifactQuality.NEW;
        logger.trace("Setting artifacts as built and their quality to {}.", quality);
        for (Artifact builtArtifact : savedBuiltArtifacts) {
            builtArtifact.setBuildRecord(buildRecord);
            builtArtifact.setArtifactQuality(quality);
        }

        return buildRecord;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public BuildRecord storeRecordForNoRebuild(BuildRecord buildRecord) {
        logger.debug("Storing record for not required build {}.", buildRecord);

        buildRecord = buildRecordRepository.save(buildRecord);
        logger.debug("Build record {} saved.", buildRecord.getId());

        return buildRecord;
    }

    /**
     * Checks the given list against the existing database and creates a new list containing artifacts which have been
     * saved to or loaded from the database.
     *
     * @param artifacts of in-memory artifacts to either insert to the database or find the matching record in the db
     * @param artifactCache
     * @return Set of up to date JPA artifact entities
     */
    private Set<Artifact> saveArtifacts(
            Collection<Artifact> artifacts,
            Map<TargetRepository.IdentifierPath, TargetRepository> storedTargetRepositories,
            Map<Artifact.IdentifierSha256, Artifact> artifactCache) {
        logger.debug("Saving {} artifacts.", artifacts.size());

        Set<Artifact> savedArtifacts = new HashSet<>();

        Set<Artifact.IdentifierSha256> artifactConstraints = new HashSet<>();
        for (Artifact artifact : artifacts) {
            if (!isGenericProxy(artifact)) { // There are thousands of duplicate artifacts that we don't want to cache
                artifactConstraints.add(new Artifact.IdentifierSha256(artifact.getIdentifier(), artifact.getSha256()));
            }
        }

        fetchOrSaveRequiredTargetRepositories(artifacts, storedTargetRepositories);

        Set<Artifact> artifactsInDb = null;
        if (artifactConstraints.size() > 0) {
            artifactsInDb = artifactRepository.withIdentifierAndSha256s(artifactConstraints);
        }

        if (artifactsInDb != null) {
            for (Artifact artifact : artifactsInDb) {
                logger.trace("Found in DB, adding to cache. Artifact {}", artifact);
                artifactCache.put(artifact.getIdentifierSha256(), artifact);
            }
        }

        for (Artifact artifact : artifacts) {
            // link managed targetRepository
            artifact.setTargetRepository(
                    storedTargetRepositories.get(artifact.getTargetRepository().getIdentifierPath()));

            Artifact artifactFromDb;
            if (isGenericProxy(artifact)) {
                artifactFromDb = saveHttpArtifact(artifact);
            } else {
                artifactFromDb = getOrSaveRepositoryArtifact(artifact, artifactCache);
            }

            savedArtifacts.add(artifactFromDb);
        }

        logger.debug("Artifacts saved: {}.", artifacts);
        return savedArtifacts;
    }

    private boolean isGenericProxy(Artifact artifact) {
        return RepositoryType.GENERIC_PROXY.equals(artifact.getTargetRepository().getRepositoryType());
    }

    private void fetchOrSaveRequiredTargetRepositories(
            Collection<Artifact> artifacts,
            Map<TargetRepository.IdentifierPath, TargetRepository> storedTargetRepositories) {

        Map<TargetRepository.IdentifierPath, TargetRepository> requiredTargetRepositories = new HashMap<>();
        for (Artifact artifact : artifacts) {
            TargetRepository targetRepository = artifact.getTargetRepository();
            logger.trace("Adding repository for artifact: {}.", artifact.toString());
            if (!storedTargetRepositories.containsKey(targetRepository.getIdentifierPath())) {
                requiredTargetRepositories.put(targetRepository.getIdentifierPath(), targetRepository);
            }
        }

        if (requiredTargetRepositories.size() > 0) {
            List<TargetRepository> targetRepositoriesInDB = targetRepositoryRepository
                    .queryByIdentifiersAndPaths(requiredTargetRepositories.keySet());

            for (TargetRepository targetRepository : targetRepositoriesInDB) {
                storedTargetRepositories.put(targetRepository.getIdentifierPath(), targetRepository);
                requiredTargetRepositories.remove(targetRepository.getIdentifierPath());
            }

            for (TargetRepository targetRepository : requiredTargetRepositories.values()) {
                // NCL-5474: This can potentionally cause unique constraint violation if two builds finish at the same
                // time, both with the same new target repository. This is unlikely to happen, so we take the risk.
                TargetRepository savedTargetRepository = targetRepositoryRepository.save(targetRepository);
                storedTargetRepositories.put(targetRepository.getIdentifierPath(), savedTargetRepository);
            }
        }
    }

    private Artifact getOrSaveRepositoryArtifact(
            Artifact artifact,
            Map<Artifact.IdentifierSha256, Artifact> artifactCache) {
        logger.trace("Saving repository artifact {}.", artifact);
        Artifact artifactFromDb = artifactCache.get(artifact.getIdentifierSha256());

        if (artifactFromDb == null) {
            logger.trace("Artifact is not in DB. Saving artifact {}.", artifact);

            // Relation owner (BuildRecord) must be saved first, the relation is saved when the BR is saved
            artifact.setDependantBuildRecords(Collections.emptySet());
            artifactFromDb = artifactRepository.save(artifact);

            logger.trace("Saved new artifact {}.", artifactFromDb);
        } else {
            logger.trace("Artifact already present in DB {}", artifactFromDb);
        }

        return artifactFromDb;
    }

    private Artifact saveHttpArtifact(Artifact artifact) {
        logger.trace("Saving http artifact {}.", artifact);
        // NONE OF THE ARTIFACTS CAN BE IN THE DB BECAUSE OF PER-BUILD REPOS
        logger.trace("Artifact is not in DB. Saving artifact {}.", artifact);

        // Relation owner (BuildRecord) must be saved first, the relation is saved when the BR is saved
        artifact.setDependantBuildRecords(Collections.emptySet());
        Artifact artifactFromDb = artifactRepository.save(artifact);

        logger.trace("Saved new artifact {}.", artifactFromDb);

        return artifactFromDb;
    }

    @Override
    public User retrieveUserByUsername(String username) {
        return userRepository.queryByPredicates(withUserName(username));
    }

    @Override
    public void createNewUser(User user) {
        userRepository.save(user);
    }

    /**
     * Save a build config set record to the db. This requires a new transaction to ensure that the record is
     * immediately committed to the database so that it's available to use by the foreign keys set in the individual
     * build records.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        return buildConfigSetRecordRepository.save(buildConfigSetRecord);
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     *
     * @param buildConfigurationId
     * @return The latest revision of the given build configuration
     */
    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigRev = buildConfigurationAuditedRepository
                .findLatestById(buildConfigurationId);
        if (buildConfigRev == null) {
            logger.error("Did not find any BuildConfiguration revisions.");
        }

        return buildConfigRev;
    }

    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAuditedLoadBCDependencies(
            Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigurationAudited = getLatestBuildConfigurationAudited(buildConfigurationId);
        buildConfigurationAudited.setBuildConfiguration(
                buildConfigurationRepository.queryById(buildConfigurationAudited.getBuildConfiguration().getId()));
        buildConfigurationAudited.getBuildConfiguration().getIndirectDependencies();

        return buildConfigurationAudited;
    }

    @Override
    public BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId) {
        return buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);
    }

    /**
     * Rebuild is required if Build Configuration has been modified or a dependency has been rebuilt since last
     * successful build.
     */
    @Override
    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return new HashSet<>(
                buildConfigurationRepository
                        .queryWithPredicates(withBuildConfigurationSetId(buildConfigurationSet.getId())));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache,
            Consumer<BuildRecord> nonRebuildCauseSetter) {

        IdRev idRev = buildConfigurationAudited.getIdRev();
        // Step 1 - Check the existence of a build with the same revision
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository
                .getAnyLatestSuccessfulBuildRecordWithRevision(idRev, temporaryBuild);
        if (latestSuccessfulBuildRecord == null) {
            logger.debug(
                    "Rebuild of buildConfiguration.idRev: {} required as there is no successful BuildRecord.",
                    idRev);
            return true;
        }
        // Step 2 - Check the existence of more recent builds with different revision
        if (!isLatestSuccessBRFromThisBCA(buildConfigurationAudited, temporaryBuild)) {
            return true;
        }
        // Step 3 - check implicit dependencies
        if (checkImplicitDependencies) {
            logger.debug("Checking if BCA: {} has implicit dependencies that need rebuild", idRev);
            boolean rebuild = hasARebuiltImplicitDependency(
                    latestSuccessfulBuildRecord,
                    temporaryBuild,
                    alignmentPreference,
                    processedDependenciesCache);
            logger.debug(
                    "Implicit dependency check for rebuild of buildConfiguration.idRev: {} required: {}.",
                    idRev,
                    rebuild);
            if (rebuild) {
                return true;
            }
        }
        // Step 4 - check explicit dependencies
        Set<BuildConfiguration> dependencies = buildConfigurationAudited.getBuildConfiguration().getDependencies();
        boolean rebuild = hasARebuiltExplicitDependency(
                latestSuccessfulBuildRecord,
                dependencies,
                temporaryBuild,
                alignmentPreference);
        logger.debug(
                "Explicit dependency check for rebuild of buildConfiguration.idRev: {} required: {}.",
                idRev,
                rebuild);
        if (!rebuild) {
            nonRebuildCauseSetter.accept(latestSuccessfulBuildRecord);
        }
        return rebuild;
    }

    @Deprecated
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean requiresRebuild(BuildTask task, Set<Integer> processedDependenciesCache) {
        return requiresRebuild(
                task.getBuildConfigurationAudited(),
                task.getBuildOptions().isImplicitDependenciesCheck(),
                task.getBuildOptions().isTemporaryBuild(),
                task.getBuildOptions().getAlignmentPreference(),
                processedDependenciesCache);
    }

    /**
     * @return true when the latest success {@link BuildRecord} of {@link BuildConfiguration} is build from this
     *         {@link BuildConfigurationAudited} if a build is persistent {@param temporaryBuild} , then it skips
     *         temporary build during evaluation
     */
    private boolean isLatestSuccessBRFromThisBCA(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean temporaryBuild) {
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository
                .getAnyLatestSuccessfulBuildRecordWithBuildConfig(buildConfigurationAudited.getId(), temporaryBuild);
        if (latestSuccessfulBuildRecord == null) {
            if (!temporaryBuild) { // When building temporary, there might be only persistent builds done before.
                logger.warn(
                        "The check should be done once it's known there is a successful BuildRecord. There is no"
                                + " successful BuildRecord for BuildConfigurationAudited {}.",
                        buildConfigurationAudited.getIdRev());
            }
            return false;
        }
        if (latestSuccessfulBuildRecord.getBuildConfigurationAuditedIdRev()
                .equals(buildConfigurationAudited.getIdRev())) {
            return true;
        } else {
            logger.debug(
                    "Last successful BuildRecord id {} is not from this BuildConfigurationAudited idRev {}.",
                    latestSuccessfulBuildRecord.getId(),
                    buildConfigurationAudited.getIdRev());
            return false;
        }
    }

    /**
     * Check is some of the dependencies from the previous build were rebuild. Checking is done based on captured
     * dependencies which are stored in the Build Record. Dependencies which have already been processed and are
     * contained in the provided cache processedDependenciesCache (if any), are not processed again
     */
    private boolean hasARebuiltImplicitDependency(
            BuildRecord latestSuccessfulBuildRecord,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache) {
        Collection<BuildRecord> lastBuiltFrom = getRecordsUsedFor(
                latestSuccessfulBuildRecord,
                processedDependenciesCache);
        return lastBuiltFrom.stream().anyMatch(br -> {
            if (hasNewerVersion(br, temporaryBuild, alignmentPreference)) {
                logger.debug(
                        "Latest successful BuildRecord: {} has implicitly dependent BR: {} that requires rebuild.",
                        latestSuccessfulBuildRecord.getId(),
                        br.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * Check is some of the dependencies defined on BuildConfiguration has newer version.
     */
    private boolean hasARebuiltExplicitDependency(
            BuildRecord latestSuccessfulBuildRecord,
            Set<BuildConfiguration> dependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        for (BuildConfiguration dependencyBuildConfiguration : dependencies) {
            BuildRecord dependencyLatestSuccessfulBuildRecord = buildRecordRepository
                    .getPreferredLatestSuccessfulBuildRecordWithBuildConfig(
                            dependencyBuildConfiguration.getId(),
                            temporaryBuild,
                            alignmentPreference);
            if (dependencyLatestSuccessfulBuildRecord == null) {
                return true;
            }
            boolean newer = dependencyLatestSuccessfulBuildRecord.getEndTime()
                    .after(latestSuccessfulBuildRecord.getEndTime());
            if (newer) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if there is newer successful BuildRecord for the same buildConfiguration.idRev
     */
    private boolean hasNewerVersion(
            BuildRecord buildRecord,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository
                .getPreferredLatestSuccessfulBuildRecordWithBuildConfig(
                        buildRecord.getBuildConfigurationId(),
                        temporaryBuild,
                        alignmentPreference);
        if (latestSuccessfulBuildRecord == null) {
            logger.error(
                    "Something went wrong, the buildRecord {} should be successful (to this latest or the BuildRecord that produced artifacts.).",
                    buildRecord.getId());
        }
        return !buildRecord.getId().equals(latestSuccessfulBuildRecord.getId());
    }

    /**
     * @return BuildRecords that produced captured dependencies artifacts
     */
    private Collection<BuildRecord> getRecordsUsedFor(BuildRecord record, Set<Integer> processedDependenciesCache) {
        Set<Integer> dependenciesId = ofNullableCollection(record.getDependencies()).stream()
                .map(Artifact::getId)
                .collect(Collectors.toSet());

        // If there are no dependencies to process, return
        if (dependenciesId.isEmpty()) {
            return Collections.emptyList();
        }

        logger.debug("Retrieved dependencies size: {}", dependenciesId.size());
        if (processedDependenciesCache != null) {
            // If the cache of already processed dependencies is not null, remove them from the list of dependencies
            // still to be processed to avoid multiple iterated checks on same items
            dependenciesId.removeAll(processedDependenciesCache);
            logger.debug(
                    "Retrieved dependencies after removal of already processed cache size: {}",
                    dependenciesId.size());

            // Populate the cache with the list of processed dependencies
            processedDependenciesCache.addAll(dependenciesId);
        }

        logger.debug("Finding built artifacts for dependencies: {}", dependenciesId);
        return dependenciesId.isEmpty() ? Collections.emptyList()
                : buildRecordRepository.findByBuiltArtifacts(dependenciesId);
    }

}
