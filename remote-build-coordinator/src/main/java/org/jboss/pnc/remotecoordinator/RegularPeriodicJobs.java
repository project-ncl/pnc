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
package org.jboss.pnc.remotecoordinator;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.remotecoordinator.builder.SetRecordTasks;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class RegularPeriodicJobs {
    private SetRecordTasks setRecordUpdateService;
    private SystemConfig config;
    private ManagedScheduledExecutorService service;

    @Deprecated // CDI
    public RegularPeriodicJobs() {
    }

    @Inject
    public RegularPeriodicJobs(
            SetRecordTasks setRecordUpdateService,
            SystemConfig config,
            ManagedScheduledExecutorService service) {
        this.setRecordUpdateService = setRecordUpdateService;
        this.config = config;
        this.service = service;
    }

    @PostConstruct
    void initJobs() {
        // Start Periodic Job only if we use Rex-based scheduler
        if (!config.isLegacyBuildCoordinator() && config.isRecordUpdateJobEnabled()) {
            service.scheduleWithFixedDelay(
                    this::pokeSetRecordTask,
                    0,
                    config.getRecordUpdateJobMillisDelay(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private void pokeSetRecordTask() {
        try {
            setRecordUpdateService.updateConfigSetRecordsStatuses();
        } catch (Exception e) { // Fail silently and continue with the job
            log.error("Exception happened while checking unfinished set records", e);

            // In case of exception, sleep for a little so that the same tasks in cluster get disjointed
            sleepFor(500);
        }
    }

    private static void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
