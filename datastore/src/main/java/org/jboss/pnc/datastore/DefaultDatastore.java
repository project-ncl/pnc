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

import org.jboss.pnc.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.BuiltArtifact;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.predicates.UserPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierAndChecksum;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withBuildRecordSetIdInSet;

@Stateless
public class DefaultDatastore implements Datastore {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    ArtifactRepository artifactRepository;

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    BuildRecordSetRepository buildRecordSetRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public BuildRecord storeCompletedBuild(BuildRecord.Builder buildRecordBuilder, Set<Integer> buildRecordSetIds) {
        BuildRecord buildRecord = buildRecordBuilder.build();
        refreshBuildConfiguration(buildRecord);
        buildRecord.setDependencies(saveArtifacts(buildRecord.getDependencies()));
        buildRecord.setBuiltArtifacts(saveBuiltArtifacts(buildRecord.getBuiltArtifacts()));
        buildRecord = buildRecordRepository.save(buildRecord);
        for(BuildRecordSet buildRecordSet : buildRecordSetRepository.queryWithPredicates(withBuildRecordSetIdInSet(buildRecordSetIds))) {
            buildRecordSet.addBuildRecord(buildRecord);
            buildRecordSetRepository.save(buildRecordSet);
        }

        return buildRecord;
    }

    /**
     * Save the unsaved artifacts in the list to the database.  For each artifact in the given list,
     * check if the artifact exists in the database.  If it already exists, replace it with the JPA
     * entity loaded from the database.  If it does not exist, add it to the database.
     * 
     * @param List of in-memory artifacts to either insert to the database or find the matching record in the db
     * @return List of up to date JPA artifact entities
     */
    private List<Artifact> saveArtifacts(List<Artifact> artifacts) {
        List<Artifact> savedArtifacts = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            Artifact artifactFromDb = artifactRepository
                    .queryByPredicates(withIdentifierAndChecksum(artifact.getIdentifier(), artifact.getChecksum()));
            if (artifactFromDb == null) {
                artifactFromDb = artifactRepository.save(artifact);
            }
            savedArtifacts.add(artifactFromDb);
        }
        return savedArtifacts;
    }

    /**
     * Match the built artifacts in the list to existing artifacts in the database
     * If no matching artifact exists in the db, the artifact is kept as-is
     * 
     * @param artifacts to store to the db
     * @return List of artifacts stored in db
     */
    private List<BuiltArtifact> saveBuiltArtifacts(List<BuiltArtifact> builtArtifacts) {
        List<BuiltArtifact> savedArtifacts = new ArrayList<>();
        for (BuiltArtifact builtArtifact : builtArtifacts) {
            Artifact artifactFromDb = artifactRepository
                    .queryByPredicates(withIdentifierAndChecksum(builtArtifact.getIdentifier(), builtArtifact.getChecksum()));
            if (artifactFromDb == null) {
                BuiltArtifact buildArtifactFromDb = (BuiltArtifact)artifactRepository.save(builtArtifact);
                savedArtifacts.add(buildArtifactFromDb);
            } else if (artifactFromDb instanceof BuiltArtifact) {
                savedArtifacts.add((BuiltArtifact)artifactFromDb);
            } else {
                // TODO: how should we handle when a built artifact matches an existing imported artifact?
            }
        }
        return savedArtifacts;
    }

    @Override
    public User retrieveUserByUsername(String username) {
        return userRepository.queryByPredicates(UserPredicates.withUserName(username));
    }

    private void refreshBuildConfiguration(BuildRecord buildRecord) {
        if (buildRecord.getLatestBuildConfiguration() != null) {
            BuildConfiguration configurationFromDB = buildConfigurationRepository.queryById(buildRecord
                    .getLatestBuildConfiguration().getId());
            buildRecord.setLatestBuildConfiguration(configurationFromDB);
        }
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
        if ( buildConfigRevs.isEmpty() ) {
            // TODO should we throw an exception?  In theory, this should never happen.
            return null;
        }
        return buildConfigRevs.get(0);
    }

    @Override
    public BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId) {
        return buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);
    }

    @Override
    public boolean hasSuccessfulBuildRecord(BuildConfiguration buildConfiguration) {
        BuildConfigurationAudited auditedConfiguration = getLatestBuildConfigurationAudited(buildConfiguration.getId());
        if(auditedConfiguration != null) {
            return auditedConfiguration.getBuildRecords().stream()
                    .map(br -> br.getStatus())
                    .filter(status -> status == BuildStatus.SUCCESS)
                    .count() > 0;
        }
        return false;
    }

}
