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

package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultCompletedBuild implements CompletedBuild {

    private RunningEnvironment runningEnvironment;
    private BuildStatus buildStatus;
    private Optional<String> outputChecksum;

    private String buildLog;

    public DefaultCompletedBuild(
            RunningEnvironment runningEnvironment,
            BuildStatus buildStatus,
            Optional<String> outputChecksum,
            String buildLog) {
        this.runningEnvironment = runningEnvironment;
        this.buildStatus = buildStatus;
        this.outputChecksum = outputChecksum;
        this.buildLog = buildLog;
    }

    @Override
    public BuildDriverResult getBuildResult() throws BuildDriverException {
        return new DefaultBuildDriverResult(buildLog, buildStatus, outputChecksum);
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }
}
