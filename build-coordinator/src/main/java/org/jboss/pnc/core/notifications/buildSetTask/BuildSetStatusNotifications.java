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
package org.jboss.pnc.core.notifications.buildSetTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildSetStatusNotifications {
    private Set<BuildSetCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     *
     * @param buildSetCallBack
     */
    public void subscribe(BuildSetCallBack buildSetCallBack) {
        subscribers.add(buildSetCallBack);
    }

    public void observeEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        Predicate<BuildSetCallBack> filterSubscribersMatchingTaskId =
                (callBackUrl) -> callBackUrl.getBuildSetTaskId().equals(buildSetStatusChangedEvent.getBuildSetTaskId());

        Set<BuildSetCallBack> matchingTask = subscribers.stream().filter(filterSubscribersMatchingTaskId).collect(Collectors.toSet());

        matchingTask.forEach((buildSetCallBack) -> {
            removeListenersOfCompletedTasks(buildSetCallBack, buildSetStatusChangedEvent);
        });

        matchingTask.forEach((buildSetCallBack) -> {
            buildSetCallBack.callback(buildSetStatusChangedEvent);
        });
    }

    private void removeListenersOfCompletedTasks(BuildSetCallBack buildSetCallBack, BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (buildSetStatusChangedEvent.getNewStatus().isCompleted()) {
            subscribers.remove(buildSetCallBack);
        }
    }
}
