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

import com.github.dockerjava.api.command.InspectContainerResponse;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.SneakyThrows;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CustomKeycloakContainer extends KeycloakContainer {

    public CustomKeycloakContainer() {
        super("quay.io/keycloak/keycloak:21.1.0");
    }

    public CustomKeycloakContainer(String imageName) {
        super(imageName);
    }

    @SneakyThrows
    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo, boolean reused) {
        if (reused) {
            logger().info("This container is being reused, so we're skipping the realm import.");
            return;
        }

        InputStream realmStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("keycloak-realm-export.json");

        RealmRepresentation realmRepresentation = JsonOutputConverterMapper
                .readValue(realmStream, RealmRepresentation.class);

        List<RoleRepresentation> defaultRoles = realmRepresentation.getRoles()
                .getRealm()
                .stream()
                .filter(rl -> rl.getName().startsWith("pnc-"))
                .collect(Collectors.toList());

        getKeycloakAdminClient().realm("newcastle-testcontainer")
                .roles()
                .get("default-roles-newcastle-testcontainer")
                .addComposites(defaultRoles);

    }

}
