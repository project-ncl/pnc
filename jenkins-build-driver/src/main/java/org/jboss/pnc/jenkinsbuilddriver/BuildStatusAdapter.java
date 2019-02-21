/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.model.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-01.
*/
class BuildStatusAdapter {
    private BuildResult buildResult;

    BuildStatusAdapter(BuildResult buildResult) {
        this.buildResult = buildResult;
    }

    BuildDriverStatus getBuildStatus() {
        switch (buildResult) {
            case FAILURE:
                return BuildDriverStatus.FAILED;
            case UNSTABLE:
                return BuildDriverStatus.UNSTABLE;
            case REBUILDING:
            case BUILDING:
                return BuildDriverStatus.BUILDING;
            case ABORTED:
                return BuildDriverStatus.CANCELED;
            case SUCCESS:
                return BuildDriverStatus.SUCCESS;
            default:
                return BuildDriverStatus.UNKNOWN;
        }
    }
}
