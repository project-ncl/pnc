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

package org.jboss.pnc.environment.openshift;

import org.jboss.pnc.common.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Configurations {

    private static final String DEFAULT_V1_PNC_BUILDER_POD = "v1_pnc-builder-pod.json";
    private static final String DEFAULT_V1_PNC_BUILDER_SERVICE_FILE = "v1_pnc-builder-service.json";
    private static final String DEFAULT_V1_PNC_BUILDER_ROUTE_FILE = "v1_pnc-builder-route.json";
    private static final String DEFAULT_V1_PNC_BUILDER_SSH_SERVICE_FILE = "v1_pnc-builder-ssh-service.json";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String CONFIGURATIONS_FOLDER = "openshift.configurations/";

    private static final String CUSTOM_V1_PNC_BUILDER_POD_PROPERTY = "v1_pnc-builder-pod-file";
    private static final String CUSTOM_V1_PNC_BUILDER_SERVICE_PROPERTY = "v1_pnc-builder-service-file";
    private static final String CUSTOM_V1_PNC_BUILDER_ROUTE_PROPERTY = "v1_pnc-builder-route-file";
    private static final String CUSTOM_V1_PNC_BUILDER_SSH_SERVICE_PROPERTY = "v1_pnc-builder-ssh-service-file";

    public static String get_V1_PNC_BUILDER_POD_Content() {
        return getContentAsString(CUSTOM_V1_PNC_BUILDER_POD_PROPERTY, DEFAULT_V1_PNC_BUILDER_POD);
    }

    public static String get_V1_PNC_BUILDER_SERVICE_Content() {
        return getContentAsString(CUSTOM_V1_PNC_BUILDER_SERVICE_PROPERTY, DEFAULT_V1_PNC_BUILDER_SERVICE_FILE);
    }

    public static String get_V1_PNC_BUILDER_ROUTE_Content() {
        return getContentAsString(CUSTOM_V1_PNC_BUILDER_ROUTE_PROPERTY, DEFAULT_V1_PNC_BUILDER_ROUTE_FILE);
    }

    public static String get_V1_PNC_BUILDER_SSH_SERVICE_Content() {
        return getContentAsString(CUSTOM_V1_PNC_BUILDER_SSH_SERVICE_PROPERTY, DEFAULT_V1_PNC_BUILDER_SSH_SERVICE_FILE);
    }

    private static String getContentAsString(String customPropertyName, String defaultResourceFile) {
        String filePath = CONFIGURATIONS_FOLDER + defaultResourceFile;
        String content;
        try {
            content = IoUtils.readFileOrResource(customPropertyName, filePath, Configurations.class.getClassLoader());
        } catch (IOException e) {
            String fileCannotRead = filePath;
            if (System.getProperty(customPropertyName) != null) {
                fileCannotRead = System.getProperty(customPropertyName);
            }
            throw new RuntimeException("Could not read configuration file " + fileCannotRead + ": " + e.getMessage());
        }
        return content;
    }
}
