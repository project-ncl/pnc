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
package org.jboss.pnc.datastore;

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
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
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
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.CollectionUtils.ofNullableCollection;
import static org.jboss.pnc.common.util.StreamCollectors.toFlatList;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withOriginUrl;
import static org.jboss.pnc.spi.datastore.predicates.UserPredicates.withUserName;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;

@Stateless
public class DefaultDatastore implements Datastore {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ArtifactRepository artifactRepository;

    private BuildRecordRepository buildRecordRepository;

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private UserRepository userRepository;

    private SequenceHandlerRepository sequenceHandlerRepository;

    private TargetRepositoryRepository targetRepositoryRepository;

    public DefaultDatastore() {
    }

    @Inject
    public DefaultDatastore(ArtifactRepository artifactRepository,
                            BuildRecordRepository buildRecordRepository,
                            BuildConfigurationRepository buildConfigurationRepository,
                            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
                            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
                            UserRepository userRepository,
                            SequenceHandlerRepository sequenceHandlerRepository,
                            TargetRepositoryRepository targetRepositoryRepository) {
        this.artifactRepository = artifactRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.userRepository = userRepository;
        this.sequenceHandlerRepository = sequenceHandlerRepository;
        this.targetRepositoryRepository = targetRepositoryRepository;
    }

    private static final String ARITFACT_ORIGIN_URL_IDENTIFIER_CONFLICT_MESSAGE = "Another artifact with the same originUrl but a different identifier already exists";
    private static final String ARITFACT_ORIGIN_URL_CHECKSUM_CONFLICT_MESSAGE = "Another artifact with the same originUrl but a different checksum already exists";

    @Override
    public Map<Artifact, String> checkForConflictingArtifacts(Collection<Artifact> artifacts) {
        Map<Artifact, String> conflicts = new HashMap<>();
        for (Artifact artifact : artifacts) {
            // Check for matching URL with different identifier or checksum
            if (artifact.getOriginUrl() != null) {
                Artifact artifactFromDb = artifactRepository.queryByPredicates(withOriginUrl(artifact.getOriginUrl()));
                if (artifactFromDb.getIdentifier().equals(artifact.getIdentifier())) {
                    conflicts.put(artifact, ARITFACT_ORIGIN_URL_IDENTIFIER_CONFLICT_MESSAGE);
                }
                if (artifactFromDb.getSha256().equals(artifact.getSha256())) {
                    conflicts.put(artifact, ARITFACT_ORIGIN_URL_CHECKSUM_CONFLICT_MESSAGE);
                }
            }
        }
        return conflicts;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public BuildRecord storeCompletedBuild(BuildRecord.Builder buildRecordBuilder) {
        BuildRecord buildRecord = buildRecordBuilder.build(true);
        logger.debug("Storing completed build {}.", buildRecord);
        if (logger.isTraceEnabled()) {
            logger.trace("Build Log: {}.", buildRecord.getBuildLog());
        }

        Map<TargetRepository.IdentifierPath, TargetRepository> repositoriesCache = new HashMap<>();
        Map<Artifact.IdentifierSha256, Artifact> artifactCache = new HashMap<>();

        /** Built artifacts must be saved before the dependencies.
         *  In case an artifact is built and the dependency (re-downloaded),
         *  it must be linked to built artifacts repository.
         */
        logger.debug("Saving built artifacts ...");
        buildRecord.setBuiltArtifacts(saveArtifacts(buildRecord.getBuiltArtifacts(), repositoriesCache, artifactCache));

        logger.debug("Saving dependencies ...");
        buildRecord.setDependencies(saveArtifacts(buildRecord.getDependencies(), repositoriesCache, artifactCache));

        logger.debug("Done saving artifacts.");
        logger.trace("Saving build record {}.", buildRecord);
        buildRecord = buildRecordRepository.save(buildRecord);
        logger.debug("Build record {} saved.", buildRecord.getId());

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
     * Checks the given list against the existing database and creates a new list containing
     * artifacts which have been saved to or loaded from the database.
     *
     * @param artifacts of in-memory artifacts to either insert to the database or find the matching record in the db
     * @param artifactCache
     * @return Set of up to date JPA artifact entities
     */
    private Set<Artifact> saveArtifacts(Collection<Artifact> artifacts,
            Map<TargetRepository.IdentifierPath, TargetRepository> storedTargetRepositories,
            Map<Artifact.IdentifierSha256, Artifact> artifactCache) {
        logger.debug("Saving {} artifacts.", artifacts.size());

        Set<Artifact> savedArtifacts = new HashSet<>();

        Set<Artifact.IdentifierSha256> artifactConstraints = new HashSet<>();
        for (Artifact artifact : artifacts) {
            artifactConstraints.add(new Artifact.IdentifierSha256(artifact.getIdentifier(), artifact.getSha256()));
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
            //link manged targetRepository
            artifact.setTargetRepository(storedTargetRepositories.get(artifact.getTargetRepository().getIdentifierPath()));

            Artifact artifactFromDb;
            if (TargetRepository.Type.GENERIC_PROXY.equals(artifact.getTargetRepository().getRepositoryType())) {
                artifactFromDb = saveHttpArtifact(artifact);
            } else {
                artifactFromDb = getOrSaveRepositoryArtifact(artifact, artifactCache);
            }

            savedArtifacts.add(artifactFromDb);
        }

        logger.debug("Artifacts saved: {}.", artifacts);
        return savedArtifacts;
    }

    private void fetchOrSaveRequiredTargetRepositories(
            Collection<Artifact> artifacts,
            Map<TargetRepository.IdentifierPath, TargetRepository> storedTargetRepositories) {

        Map<TargetRepository.IdentifierPath, TargetRepository> requiredTargetRepositories = new HashMap<>();
        for (Artifact artifact : artifacts) {
            TargetRepository targetRepository = artifact.getTargetRepository();
            requiredTargetRepositories.put(targetRepository.getIdentifierPath(), targetRepository);
        }

        List<TargetRepository> targetRepositoriesInDB = targetRepositoryRepository.queryByIdentifiersAndPaths(
                requiredTargetRepositories.keySet());
        storedTargetRepositories.putAll(targetRepositoriesInDB.stream()
                .collect(Collectors.toMap(TargetRepository::getIdentifierPath, tr -> tr)));

        for (Map.Entry<TargetRepository.IdentifierPath, TargetRepository> entry : requiredTargetRepositories.entrySet()) {
           TargetRepository.IdentifierPath identifierPath = entry.getKey();
           TargetRepository targetRepository = entry.getValue();
           storedTargetRepositories.computeIfAbsent(identifierPath, ip -> targetRepositoryRepository.save(targetRepository));
       }
    }

    private TargetRepository getOrSaveTargetRepository(TargetRepository targetRepository) {
        logger.trace("Saving target repository {}.", targetRepository);
        TargetRepository targetRepositoryFromDb = targetRepositoryRepository
                .queryByIdentifierAndPath(targetRepository.getIdentifier(), targetRepository.getRepositoryPath());

        if (targetRepositoryFromDb == null) {
            logger.trace("Target repository is not in DB. Saving target repository {}.", targetRepository);
            targetRepositoryFromDb = targetRepositoryRepository.save(targetRepository);
            logger.debug("Target repository saved {}.", targetRepositoryFromDb);
        } else {
            logger.debug("Target repository already present in DB {}.", targetRepositoryFromDb);
        }

        return targetRepositoryFromDb;
    }

    private Artifact getOrSaveRepositoryArtifact(Artifact artifact, Map<Artifact.IdentifierSha256, Artifact> artifactCache) {
        logger.trace("Saving repository artifact {}.", artifact);
        Artifact artifactFromDb = artifactCache.get(artifact.getIdentifierSha256()); //TODO http artifacts

        if (artifactFromDb == null) {
            logger.trace("Artifact is not in DB. Saving artifact {}.", artifact);

            //Relation owner (BuildRecord) must be saved first, the relation is saved when the BR is saved
            artifact.setBuildRecords(Collections.emptySet());
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

        //Relation owner (BuildRecord) must be saved first, the relation is saved when the BR is saved
        artifact.setBuildRecords(Collections.emptySet());
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

    @Override
    public int getNextBuildRecordId() {

        Long nextId = sequenceHandlerRepository.getNextID(BuildRecord.SEQUENCE_NAME);
        logger.debug("Build Record nextId: {}", nextId);

        return nextId.intValue();
    }

    /**
     * Save a build config set record to the db.  This requires a new transaction to ensure that
     * the record is immediately committed to the database so that it's available to use by the
     * foreign keys set in the individual build records.
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
        List<BuildConfigurationAudited> buildConfigRevs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(buildConfigurationId);
        if (buildConfigRevs.isEmpty()) {
            logger.error("Did not find any BuildConfiguration revisions.");
            return null;
        }

        return buildConfigRevs.get(0);
    }


    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAuditedLoadBCDependencies(Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigurationAudited = getLatestBuildConfigurationAudited(buildConfigurationId);
        buildConfigurationAudited.setBuildConfiguration(buildConfigurationRepository.queryById(buildConfigurationAudited.getBuildConfiguration().getId()));
        buildConfigurationAudited.getBuildConfiguration().getIndirectDependencies();

        return buildConfigurationAudited;
    }

    @Override
    public BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId) {
        return buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);
    }

    /**
     * Rebuild is required if Build Configuration has been modified or a dependency has been rebuilt since last successful build.
     */
    @Override
    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return new HashSet<>(buildConfigurationRepository.queryWithPredicates(withBuildConfigurationSetId(buildConfigurationSet.getId())));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean requiresRebuild(BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild) {
        IdRev idRev = buildConfigurationAudited.getIdRev();
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository.getLatestSuccessfulBuildRecord(idRev, temporaryBuild);
        if (latestSuccessfulBuildRecord == null) {
            logger.debug("Rebuild of buildConfiguration.idRev: {} required as there is no successful BuildRecord.", idRev);
            return true;
        }
        if (!isLatestSuccessBRFromThisBCA(buildConfigurationAudited, temporaryBuild)) {
            return true;
        }
        if (checkImplicitDependencies) {
            boolean rebuild = hasARebuiltImplicitDependency(latestSuccessfulBuildRecord, temporaryBuild);
            logger.debug("Implicit dependency check for rebuild of buildConfiguration.idRev: {} required: {}.", idRev, rebuild);
            if (rebuild) {
                return rebuild;
            }
        }
        // check explicit dependencies
        Set<BuildConfiguration> dependencies = buildConfigurationAudited.getBuildConfiguration().getDependencies();
        boolean rebuild = hasARebuiltExplicitDependency(latestSuccessfulBuildRecord, dependencies, temporaryBuild);
        logger.debug("Explicit dependency check for rebuild of buildConfiguration.idRev: {} required: {}.", idRev, rebuild);
        return rebuild;
    }

    @Deprecated
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean requiresRebuild(BuildTask task) {
        return requiresRebuild(task.getBuildConfigurationAudited(),
                task.getBuildOptions().isImplicitDependenciesCheck(),
                task.getBuildOptions().isTemporaryBuild());
    }

    /**
     * @return true when the latest success {@link BuildRecord} of {@link BuildConfiguration} is build from this {@link BuildConfigurationAudited}
     * if a build is persistent {@param temporaryBuild} , then it skips temporary build during evaluation
     */
    private boolean isLatestSuccessBRFromThisBCA(BuildConfigurationAudited buildConfigurationAudited, boolean temporaryBuild) {
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository.getLatestSuccessfulBuildRecord(buildConfigurationAudited.getId(), temporaryBuild);
        if (latestSuccessfulBuildRecord == null) {
            logger.warn("The check should be done once it's known there is a successful BuildRecord. There is no successful BuildRecord for BuildConfigurationAudited {}.", buildConfigurationAudited.getIdRev());
        }
        if (latestSuccessfulBuildRecord.getBuildConfigurationAuditedIdRev().equals(buildConfigurationAudited.getIdRev())) {
            return true;
        } else {
            logger.debug("Last successful BuildRecord id {} is not from this BuildConfigurationAudited idRev {}.", latestSuccessfulBuildRecord.getId(), buildConfigurationAudited.getIdRev());
            return false;
        }
    }

    /**
     * Check is some of the dependencies from the previous build were rebuild.
     * Checking is done based on captured dependencies which are stored in the Build Record.
     */
    private boolean hasARebuiltImplicitDependency(BuildRecord latestSuccessfulBuildRecord, boolean temporaryBuild) {
        Collection<BuildRecord> lastBuiltFrom = getRecordsUsedFor(latestSuccessfulBuildRecord);
        return lastBuiltFrom.stream()
                .anyMatch(br -> hasNewerVersion(br, temporaryBuild));
    }

    /**
     * Check is some of the dependencies defined on BuildConfiguration has newer version.
     */
    private boolean hasARebuiltExplicitDependency(BuildRecord latestSuccessfulBuildRecord, Set<BuildConfiguration> dependencies, boolean temporaryBuild) {
        for (BuildConfiguration dependencyBuildConfiguration : dependencies) {
            BuildRecord dependencyLatestSuccessfulBuildRecord = buildRecordRepository.getLatestSuccessfulBuildRecord(dependencyBuildConfiguration.getId(), temporaryBuild);
            if (dependencyLatestSuccessfulBuildRecord == null) {
                return true;
            }
            boolean newer = dependencyLatestSuccessfulBuildRecord.getEndTime().after(latestSuccessfulBuildRecord.getEndTime());
            if (newer) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if there is newer successful BuildRecord for the same buildConfiguration.idRev
     */
    private boolean hasNewerVersion(BuildRecord buildRecord, boolean temporaryBuild) {
        BuildRecord latestSuccessfulBuildRecord = buildRecordRepository.getLatestSuccessfulBuildRecord(buildRecord.getBuildConfigurationAuditedIdRev(), temporaryBuild);
        if (latestSuccessfulBuildRecord == null) {
            logger.error("Something went wrong, the buildRecord should be successful (ot this lattest or the BuildRecord that produced artifacts.).");
        }
        return !buildRecord.getId().equals(latestSuccessfulBuildRecord.getId());
    }

    /**
     * @return BuildRecords that produced captured dependencies artifacts
     */
    private Collection<BuildRecord> getRecordsUsedFor(BuildRecord record) {
        return ofNullableCollection(record.getDependencies())
                .stream()
                .map(Artifact::getBuildRecords)
                .collect(toFlatList());
    }

}
