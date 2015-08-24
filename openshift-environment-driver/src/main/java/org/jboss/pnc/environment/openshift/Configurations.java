/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
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

import org.apache.commons.io.IOUtils;
import org.jboss.pnc.common.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public enum Configurations {

    V1_PNC_BUILDER_POD("v1_pnc-builder-pod.json");

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String CONFIGURATIONS_FOLDER = "/openshift.configurations/";

    private String filePath;

    Configurations(String fileName) {
        this.filePath = CONFIGURATIONS_FOLDER + fileName;
    }

    public String getContentAsString() {
        String content = null;
        try {
            content = IoUtils.readResource(filePath, Configurations.class.getClassLoader());
        } catch (IOException e) {
            logger.error("Cannot read configuration file " + filePath, e);
            throw new RuntimeException("Could not read configuration file " + filePath + ": " + e.getMessage());
        }
        return content;
    }
}
