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

package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildDriverResult")
public class BuildDriverResultRest implements BuildDriverResult {

    private String buildLog;
    private BuildDriverStatus buildDriverStatus;

    public BuildDriverResultRest() {}

    public BuildDriverResultRest(BuildDriverResult buildDriverResult) throws BuildDriverException {
        this.buildLog = buildDriverResult.getBuildLog();
        this.buildDriverStatus = buildDriverResult.getBuildDriverStatus();
    }

    @Override
    public String getBuildLog() throws BuildDriverException {
        return buildLog;
    }

    @Override
    public BuildDriverStatus getBuildDriverStatus() {
        return buildDriverStatus;
    }

    public void setBuildDriverStatus(BuildDriverStatus buildDriverStatus) {
        this.buildDriverStatus = buildDriverStatus;
    }

    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }
}
