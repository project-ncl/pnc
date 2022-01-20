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
package org.jboss.pnc.environment.openshift;

import org.jboss.pnc.common.util.IoUtils;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public enum Resource {

    PNC_BUILDER_POD("pnc-builder-pod.json"),
    PNC_BUILDER_SERVICE("pnc-builder-service.json"),
    PNC_BUILDER_ROUTE("pnc-builder-route.json"),
    PNC_BUILDER_SSH_SERVICE("pnc-builder-ssh-service.json");

    private static final String CONFIGURATIONS_FOLDER = "openshift.configurations/";

    private final String filePath;

    Resource(String fileName) {
        this.filePath = CONFIGURATIONS_FOLDER + fileName;
    }

    /**
     * @return Default built-in configuration from this jar.
     */
    public String getDefaultConfiguration() {
        try {
            return IoUtils.readResource(filePath, Configurations.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file " + filePath + ".", e);
        }
    }
}
