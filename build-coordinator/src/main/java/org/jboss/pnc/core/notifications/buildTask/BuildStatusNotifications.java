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
package org.jboss.pnc.core.notifications.buildTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildStatusNotifications {
    private Set<BuildCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     *
     * @param buildCallBack object which callback method will be called when its taskId matches
     */
    public void subscribe(BuildCallBack buildCallBack) {
        subscribers.add(buildCallBack);
    }

    public void observeEvent(@Observes BuildCoordinationStatusChangedEvent event) {
        BuildCoordinationStatusChangedEvent buildStatusChangedEvent = event; // Avoid CDI runtime issue issue NCL-1505
        Predicate<BuildCallBack> filterSubscribersMatchingTaskId =
                (callBackUrl) -> callBackUrl.getBuildTaskId().equals(buildStatusChangedEvent.getBuildTaskId());

        Set<BuildCallBack> matchingTasks = subscribers.stream().filter(filterSubscribersMatchingTaskId).collect(Collectors.toSet());

        matchingTasks.forEach((buildCallBack) -> {
            removeListenersOfCompletedTasks(buildCallBack, buildStatusChangedEvent);
        });
        matchingTasks.forEach((buildCallBack) -> {
            buildCallBack.callback(buildStatusChangedEvent);
        });
    }

    private void removeListenersOfCompletedTasks(BuildCallBack buildCallBack, BuildCoordinationStatusChangedEvent buildStatusChangedEvent) {
        if (buildStatusChangedEvent.getNewStatus().isCompleted()) {
            subscribers.remove(buildCallBack);
        }
    }
}
