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

import java.lang.invoke.MethodHandles;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.jboss.pnc.datastore.predicates.UserPredicates;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DefaultDatastore implements Datastore {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void storeCompletedBuild(BuildRecord buildRecord) {
        storeBuildConfiguration(buildRecord);
        buildRecordRepository.save(buildRecord);
    }

    @Override
    public User retrieveUserByUsername(String username) {
        return userRepository.findOne(UserPredicates.withUsername(username));
    }

    private void storeBuildConfiguration(BuildRecord buildRecord) {
        if (buildRecord.getLatestBuildConfiguration() != null) {
            BuildConfiguration configurationFromDB = buildConfigurationRepository.findOne(buildRecord
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
        logger.info("Build Record nextId: {}", nextId);

        return nextId.intValue();
    }

    @Override
    public int getNextBuildConfigSetRecordId() {

        Long nextId = sequenceHandlerRepository.getNextID(BuildConfigSetRecord.SEQUENCE_NAME);
        logger.info("Build Configuration Set Record nextId: {}", nextId);

        return nextId.intValue();
    }

}
