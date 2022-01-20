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

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildDriverResult implements BuildDriverResult {

    private String buildLog;
    private BuildStatus buildStatus;
    private Optional<String> outputChecksum;

    public DefaultBuildDriverResult(String buildLog, BuildStatus buildStatus, Optional<String> outputChecksum) {
        if (outputChecksum == null) {
            throw new NullPointerException("If there is no value Optional.empty have to be used.");
        }
        this.buildLog = buildLog;
        this.buildStatus = buildStatus;
        this.outputChecksum = outputChecksum;
    }

    @Override
    public String getBuildLog() {
        return buildLog;
    }

    @Override
    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    @Override
    public Optional<String> getOutputChecksum() {
        return outputChecksum;
    }
}
