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

package org.jboss.pnc.core.builder.datastore;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.ProductVersion;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationUtils {

    private BuildConfigurationUtils() {}

    /**
     * Get the first product version (if any) associated with this build config.
     * @param buildConfig The build configuration to check
     * @return The firstProduct version, or null if there is none
     */
    public static ProductVersion getFirstProductVersion(BuildConfiguration buildConfig) {
        if(buildConfig.getProductVersions() == null) {
            return null;
        }
        return buildConfig.getProductVersions().stream().findFirst().orElse(null);
    }
}
