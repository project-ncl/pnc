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
package org.jboss.pnc.integrationrex;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class GetFreePort {

    public static final String KEYCLOAK_PORT = "keycloakPort";
    // public static final String REX_PORT = "rexPort";

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            throw new RuntimeException("Required first argument of directory where to create properties.");
        }
        String propertiesFilePath;
        if (args[0].endsWith("/")) {
            propertiesFilePath = args[0] + "test.properties";
        } else {
            propertiesFilePath = args[0] + "/test.properties";
        }

        Properties properties = new Properties();
        properties.put(KEYCLOAK_PORT, Objects.toString(getFreeHostPort()));
        // properties.put(REX_PORT, Objects.toString(getFreeHostPort()));
        try (OutputStream outputstream = new FileOutputStream(propertiesFilePath)) {
            properties.store(outputstream, "File Updated");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return free port on a host network
     */
    public static int getFreeHostPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            if (serverSocket == null) {
                throw new RuntimeException("Cannot open socket.");
            }
            if (serverSocket.getLocalPort() <= 0) {
                throw new RuntimeException("Cannot get port.");
            }
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
