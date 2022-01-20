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

package org.jboss.pnc.bpm.model;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildDriverResult")
public class BuildDriverResultRest implements BuildDriverResult {

    private String buildLog;
    private BuildStatus buildStatus;
    Optional<String> outputChecksum;

    public BuildDriverResultRest() {
        outputChecksum = Optional.empty();
    }

    public BuildDriverResultRest(BuildDriverResult buildDriverResult) {
        this.buildLog = buildDriverResult.getBuildLog();
        this.buildStatus = buildDriverResult.getBuildStatus();
        this.outputChecksum = buildDriverResult.getOutputChecksum();
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

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }

    @Override
    public String toString() {
        return "BuildDriverResultRest{" + "buildLog='" + buildLog + '\'' + ", buildStatus=" + buildStatus + '}';
    }

    public String toStringLimited() {
        return "BuildDriverResultRest{" + ", buildStatus=" + buildStatus + '}';
    }
}
