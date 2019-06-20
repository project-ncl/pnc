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
package org.jboss.pnc.coordinator.maintenance;


import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Schedules deletion of builds without a transaction context
 *
 * @author Jakub Bartecek
 */
@Stateless
public class TemporaryBuildsCleanupScheduleWorker {
    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanupScheduleWorker.class);

    private final int DEFAULT_LIFESPAN = 14;

    private final int TEMPORARY_BUILD_LIFESPAN;

    private BuildRecordRepository buildRecordRepository;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private TemporaryBuildsCleaner temporaryBuildsCleaner;

    private KeycloakServiceClient serviceClient;

    @Deprecated
    public TemporaryBuildsCleanupScheduleWorker() {
        log.warn("Deprecated constructor used to init TemporaryBuildsCleanupScheduleWorker. Default values will be used.");
        TEMPORARY_BUILD_LIFESPAN = DEFAULT_LIFESPAN;
    }

    @Inject
    public TemporaryBuildsCleanupScheduleWorker(
            Configuration configuration,
            BuildRecordRepository buildRecordRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            TemporaryBuildsCleaner temporaryBuildsCleaner,
            KeycloakServiceClient serviceClient) {

        int temporaryBuildLifeSpan;
        try {
            SystemConfig systemConfig = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
            temporaryBuildLifeSpan = systemConfig.getTemporaryBuildsLifeSpan();
        } catch (ConfigurationParseException e) {
            log.warn("TemporaryBuildsCleanupScheduler initialization from config file failed! Default values will be used.");
            temporaryBuildLifeSpan = DEFAULT_LIFESPAN;
        }

        this.TEMPORARY_BUILD_LIFESPAN = temporaryBuildLifeSpan;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.temporaryBuildsCleaner = temporaryBuildsCleaner;
        this.serviceClient = serviceClient;
    }

    /**
     * Cleanup old temporary builds
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void cleanupExpiredTemporaryBuilds() throws ValidationException {
        log.info("Regular cleanup of expired temporary builds started. Removing builds older than " + TEMPORARY_BUILD_LIFESPAN
                + " days.");
        Date expirationThreshold = TimeUtils.getDateXDaysAgo(TEMPORARY_BUILD_LIFESPAN);

        String authToken = serviceClient.getAuthToken();
        deleteExpiredBuildConfigSetRecords(expirationThreshold, authToken);
        deleteExpiredBuildRecords(expirationThreshold, authToken);

        log.info("Regular cleanup of expired temporary builds finished.");
    }

    private void deleteExpiredBuildConfigSetRecords(Date expirationThreshold, String authToken) throws ValidationException {
        List<BuildConfigSetRecord> expiredBCSRecords = buildConfigSetRecordRepository.findTemporaryBuildConfigSetRecordsOlderThan(expirationThreshold);
        for (BuildConfigSetRecord bcsr : expiredBCSRecords) {
            temporaryBuildsCleaner.deleteTemporaryBuildConfigSetRecord(bcsr.getId(), authToken);
        }
    }

    private void deleteExpiredBuildRecords(Date expirationThreshold, String authToken) throws ValidationException {
        List<BuildRecord> expiredBuilds = buildRecordRepository.findTemporaryBuildsOlderThan(expirationThreshold);
        for (BuildRecord br : expiredBuilds) {
            temporaryBuildsCleaner.deleteTemporaryBuild(br.getId(), authToken);
        }
    }
}
