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

package org.jboss.pnc.rest.trigger;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutorTriggerer { //TODO completely decouple datastore

    private final Logger log = Logger.getLogger(BuildExecutorTriggerer.class);

    private BuildExecutor buildExecutor;

    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BpmNotifier bpmNotifier;

    public BuildExecutorTriggerer(
            BuildExecutor buildExecutor,
            BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BpmNotifier bpmNotifier) {
        this.buildExecutor = buildExecutor;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.bpmNotifier = bpmNotifier;
    }

    public BuildExecutionSession executeBuild(
            Integer buildTaskId,
            Integer buildConfigurationId,
            Integer buildConfigurationRevision,
            User userTriggered,
            String callbackUrl) throws CoreException, ExecutorException {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(buildConfigurationId);
        IdRev idRev = new IdRev(buildConfigurationId, buildConfigurationRevision);
        log.debug("Querying for configurationAudited by idRev: " + idRev.toString());
        final BuildConfigurationAudited configurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        log.debug("Building configurationAudited " + configurationAudited.toString());
        log.debug("User triggered the process " + userTriggered.getUsername());

        String buildContentId = ContentIdentityManager.getBuildContentId(configuration.getName());

        BuildExecutionConfiguration buildExecutionConfig = new DefaultBuildExecutionConfiguration(
                buildTaskId,
                configuration,
                configurationAudited,
                buildContentId,
                userTriggered);

        Consumer<BuildExecutionStatusChangedEvent> onExecutionStatusChange = (statusChangedEvent) -> {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                statusChangedEvent.getBuildResult().ifPresent((buildResult) -> {
                    bpmNotifier.sendBuildExecutionCompleted(callbackUrl.toString(), buildResult);
                });
            }
        };
        BuildExecutionSession buildExecutionSession = buildExecutor.startBuilding(buildExecutionConfig, onExecutionStatusChange);

        return buildExecutionSession;
    }

}
