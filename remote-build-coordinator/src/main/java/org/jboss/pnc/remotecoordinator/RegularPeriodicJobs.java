/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.remotecoordinator.builder.SetRecordTasks;

import lombok.extern.slf4j.Slf4j;

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
