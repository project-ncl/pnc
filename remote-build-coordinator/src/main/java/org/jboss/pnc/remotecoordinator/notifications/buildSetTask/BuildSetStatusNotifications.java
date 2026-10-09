/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.notifications.buildSetTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// mstodo remove in the future and verify not used anymore
@ApplicationScoped
public class BuildSetStatusNotifications {

    private static final Logger log = LoggerFactory.getLogger(BuildSetStatusNotifications.class);

    private final Set<BuildSetCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     *
     * @param buildSetCallBack object which callback method will be called when its id matches
     */
    public void subscribe(BuildSetCallBack buildSetCallBack) {
        log.debug(
                "Registering new subscriber for buildSetConfigurationId {}.",
                buildSetCallBack.getBuildSetConfigurationId());
        subscribers.add(buildSetCallBack);
    }

    public void observeEvent(@Observes BuildSetStatusChangedEvent event) {
        log.debug("Observed BuildSetStatusChangedEvent {}.", event);
        BuildSetStatusChangedEvent buildSetStatusChangedEvent = event; // Avoid CDI runtime issue NCL-1505
        Predicate<BuildSetCallBack> filterSubscribersMatchingTaskId = (callBackUrl) -> callBackUrl
                .getBuildSetConfigurationId()
                .equals(Integer.valueOf(buildSetStatusChangedEvent.getBuildSetConfigurationId()));

        Set<BuildSetCallBack> matchingTask = subscribers.stream()
                .filter(filterSubscribersMatchingTaskId)
                .collect(Collectors.toSet());

        log.debug(
                "Notifying {} of {} total subscribers with event {}.",
                matchingTask.size(),
                subscribers.size(),
                buildSetStatusChangedEvent);

        matchingTask.forEach((buildSetCallBack) -> {
            log.trace(
                    "Executing buildSetCallBack for buildSetConfigurationId {} with {}.",
                    buildSetCallBack.getBuildSetConfigurationId(),
                    buildSetStatusChangedEvent);
            buildSetCallBack.callback(buildSetStatusChangedEvent);
        });

        matchingTask.forEach(
                (buildSetCallBack) -> removeListenersOfCompletedTasks(buildSetCallBack, buildSetStatusChangedEvent));
    }

    private void removeListenersOfCompletedTasks(
            BuildSetCallBack buildSetCallBack,
            BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (buildSetStatusChangedEvent.getNewBuildStatus().isFinal()) {
            log.debug(
                    "Removing subscriber for buildSetConfigurationId {}.",
                    buildSetCallBack.getBuildSetConfigurationId());
            subscribers.remove(buildSetCallBack);
        }
    }
}
