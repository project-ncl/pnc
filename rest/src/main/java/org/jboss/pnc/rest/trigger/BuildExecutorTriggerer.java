/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.rest.utils.BpmNotifier;
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
public class BuildExecutorTriggerer {

    private final Logger log = Logger.getLogger(BuildExecutorTriggerer.class);

    private BuildExecutor buildExecutor;

    private BpmNotifier bpmNotifier;

    @Deprecated //CDI workaround
    public BuildExecutorTriggerer() {}

    public BuildExecutorTriggerer(
            BuildExecutor buildExecutor,
            BpmNotifier bpmNotifier) {
        this.buildExecutor = buildExecutor;
        this.bpmNotifier = bpmNotifier;
    }

    public BuildExecutionSession executeBuild(BuildExecutionConfiguration buildExecutionConfig, String callbackUrl) throws CoreException, ExecutorException {

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
