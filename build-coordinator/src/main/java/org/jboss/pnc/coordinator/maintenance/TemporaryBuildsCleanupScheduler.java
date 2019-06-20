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

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Executes regular cleanup of old temporary builds after expiration.
 *
 * @author Jakub Bartecek
 */
@Singleton
public class TemporaryBuildsCleanupScheduler {
    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanupScheduler.class);

    private TemporaryBuildsCleanupScheduleWorker temporaryBuildsCleanupScheduleWorker;

    @Deprecated
    public TemporaryBuildsCleanupScheduler() {
    }

    @Inject
    public TemporaryBuildsCleanupScheduler(TemporaryBuildsCleanupScheduleWorker temporaryBuildsCleanupScheduleWorker) {
        this.temporaryBuildsCleanupScheduleWorker = temporaryBuildsCleanupScheduleWorker;
    }

    /**
     * Cleanup old temporary builds every midnight
     */
    @Schedule
    public void cleanupExpiredTemporaryBuilds() throws ValidationException {
        log.info("Regular deletion of temporary builds triggered by clock.");
        temporaryBuildsCleanupScheduleWorker.cleanupExpiredTemporaryBuilds();
        log.info("Regular deletion of temporary builds successfully initiated.");
    }
}
