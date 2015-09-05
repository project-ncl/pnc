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
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.predicates.UserPredicates;
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

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withBuildRecordSetIdInSet;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;

@Stateless
public class DefaultDatastore implements Datastore {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject BuildConfigurationRepository buildConfigurationRepository;

    @Inject BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

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
    public BuildRecord storeCompletedBuild(BuildRecord buildRecord, Set<Integer> buildRecordSetIds) {
        refreshBuildConfiguration(buildRecord);
        buildRecord = buildRecordRepository.save(buildRecord);
        for(BuildRecordSet buildRecordSet : buildRecordSetRepository.queryWithPredicates(withBuildRecordSetIdInSet(buildRecordSetIds))) {
            buildRecordSet.addBuildRecord(buildRecord);
            buildRecordSetRepository.save(buildRecordSet);
        }
        
        return buildRecord;
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


}
