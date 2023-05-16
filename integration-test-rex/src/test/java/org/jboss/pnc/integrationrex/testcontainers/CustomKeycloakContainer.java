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
package org.jboss.pnc.integrationrex.testcontainers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.command.InspectContainerResponse;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;

public class CustomKeycloakContainer extends KeycloakContainer {

    private final Set<String> importFiles = new HashSet<>();
    private ObjectMapper objectMapper;

    public CustomKeycloakContainer() {
        super("quay.io/keycloak/keycloak:21.1.0");
        initObjectMapper();
    }

    public CustomKeycloakContainer(String imageName) {
        super(imageName);
        initObjectMapper();
    }

    /**
     * Customize objectMapper to accept exported realm file
     */
    private void initObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public KeycloakContainer withRealmImportFile(String importFile) {
        // Parent importFiles are not accessible.
        this.importFiles.add(importFile);
        return self();
    }

    /**
     * Use customized objectMapper.
     */
    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo, boolean reused) {
        if (reused) {
            logger().info("This container is being reused, so we're skipping the realm import.");
            return;
        }
        if (!importFiles.isEmpty()) {
            logger().info("Connect to Keycloak container to import given realm files.");
            Keycloak kcAdmin = getKeycloakAdminClient();
            try {
                for (String importFile : importFiles) {
                    logger().info("Importing realm from file {}", importFile);
                    InputStream resourceStream = this.getClass().getResourceAsStream(importFile);
                    // this is a dirty hack, but in certain cases, we need to obtain the resource stream from the
                    // current thread context classloader
                    // as soon as the auto-import of realm files returns again (approx. KC 18), this complete method is
                    // removed anyway.
                    if (resourceStream == null) {
                        resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(importFile);
                    }
                    kcAdmin.realms().create(objectMapper.readValue(resourceStream, RealmRepresentation.class));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
