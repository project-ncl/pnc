/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.common;

import java.io.IOException;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.ServiceAccountClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.util.IoUtils;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class SystemConfigMock {

    public static SystemConfig withKeycloakServiceAccount() throws IOException {
        String configJson = IoUtils.readResource("keycloakClientConfig.json", SystemConfigMock.class.getClassLoader());
        KeycloakClientConfig keycloakClientConfig = JsonOutputConverterMapper
                .readValue(configJson, KeycloakClientConfig.class);

        ServiceAccountClientConfig serviceAccountClientConfig = new ServiceAccountClientConfig(
                ServiceAccountClientConfig.Mode.KEYCLOAK);

        return new SystemConfig(
                null,
                null,
                null,
                "10",
                keycloakClientConfig,
                null,
                serviceAccountClientConfig,
                null,
                null,
                "",
                "10",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "false",
                null,
                "false",
                null,
                null,
                null);
    }
}
