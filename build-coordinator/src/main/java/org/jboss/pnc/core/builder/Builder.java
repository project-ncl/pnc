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

package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Builder triggers individuals builds without dependency coordination.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Builder {

    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private DatastoreAdapter datastoreAdapter;
    private BuildExecutor buildExecutor;

    @Deprecated
    public Builder() { //CDI workaround
    }

    @Inject
    public Builder(Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier, DatastoreAdapter datastoreAdapter, BuildExecutor buildExecutor) {
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.datastoreAdapter = datastoreAdapter;
        this.buildExecutor = buildExecutor;
    }

    public BuildTask build(BuildConfiguration buildConfiguration, User user, int buildTaskId, Consumer<BuildStatus> onComplete) throws BuildConflictException, CoreException {

        BuildConfigurationAudited buildConfigAudited = datastoreAdapter.getLatestBuildConfigurationAudited(buildConfiguration.getId());

        BuildTask buildTask = BuildTask.build(
                buildConfiguration,
                buildConfigAudited,
                user,
                buildStatusChangedEventNotifier,
                (bt) -> {},
                buildTaskId,
                null,
                new Date());

        //TODO recollect to running instances in case of system failure
        buildExecutor.startBuilding(buildTask, onComplete);

        return buildTask;
    }

}
