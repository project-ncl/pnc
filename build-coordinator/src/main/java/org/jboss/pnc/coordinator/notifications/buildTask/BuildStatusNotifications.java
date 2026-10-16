/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.notifications.buildTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BuildStatusNotifications {

    private Logger log = LoggerFactory.getLogger(BuildStatusNotifications.class);

    private final Set<BuildCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     *
     * @param buildCallBack object which callback method will be called when its taskId matches
     */
    public void subscribe(BuildCallBack buildCallBack) {
        log.debug("Subscribing new status update listener {}.", buildCallBack);
        subscribers.add(buildCallBack);
    }

    public void observeEvent(@Observes BuildStatusChangedEvent event) {
        log.debug("Observed new status changed event {}.", event);
        BuildStatusChangedEvent buildStatusChangedEvent = event; // Avoid CDI runtime issue issue NCL-1505
        Predicate<BuildCallBack> filterSubscribersMatchingTaskId = (callBackUrl) -> callBackUrl.getBuildTaskId()
                .equals(buildStatusChangedEvent.getBuild().getId());

        Set<BuildCallBack> matchingTasks = subscribers.stream()
                .filter(filterSubscribersMatchingTaskId)
                .collect(Collectors.toSet());

        matchingTasks
                .forEach((buildCallBack) -> removeListenersOfCompletedTasks(buildCallBack, buildStatusChangedEvent));
        matchingTasks.forEach((buildCallBack) -> buildCallBack.callback(buildStatusChangedEvent));
        log.debug("Status changed event processed {}.", event);
    }

    private void removeListenersOfCompletedTasks(
            BuildCallBack buildCallBack,
            BuildStatusChangedEvent buildStatusChangedEvent) {
        if (buildStatusChangedEvent.getNewStatus().isFinal()) {
            log.debug("Subscribing new status update listener {}.", buildCallBack);
            subscribers.remove(buildCallBack);
        }
    }
}
