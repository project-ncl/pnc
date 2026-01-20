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

import org.jboss.pnc.test.category.DebugTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * Simple test to verify Testcontainers can detect Docker. Run this test to see detailed Docker detection logs.
 */
@Category(DebugTest.class)
public class DockerDetectionTest {

    @Test
    public void testDockerDetection() {
        System.out.println("=== Testing Docker Detection ===");

        // Print Testcontainers configuration
        System.out.println("Testcontainers configuration:");
        System.out.println("  - Checks disabled: " + TestcontainersConfiguration.getInstance().isDisableChecks());

        // Print environment variables
        System.out.println("\nEnvironment variables:");
        System.out.println("  - DOCKER_HOST: " + System.getenv("DOCKER_HOST"));
        System.out.println(
                "  - TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE: " + System.getenv("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE"));
        System.out.println("  - TESTCONTAINERS_RYUK_DISABLED: " + System.getenv("TESTCONTAINERS_RYUK_DISABLED"));

        // Try to get Docker client - this will trigger detection and show detailed logs
        System.out.println("\nAttempting to get Docker client...");
        try {
            DockerClientFactory instance = DockerClientFactory.instance();
            System.out.println("Docker client factory obtained successfully");

            // This will actually test the connection
            instance.client();
            System.out.println("Docker client obtained successfully");

            // Get Docker info
            System.out.println("\nDocker info:");
            System.out.println("  - Docker version: " + instance.getActiveApiVersion());
            System.out.println("  - Docker host: " + instance.dockerHostIpAddress());

            System.out.println("\n=== Docker Detection SUCCESSFUL ===");
        } catch (Exception e) {
            System.err.println("\n=== Docker Detection FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
