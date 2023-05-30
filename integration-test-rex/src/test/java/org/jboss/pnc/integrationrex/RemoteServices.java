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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.integrationrex.mock.BPMWireMock;
import org.jboss.pnc.integrationrex.setup.Deployments;
import org.jboss.pnc.integrationrex.testcontainers.CustomKeycloakContainer;
import org.jboss.pnc.integrationrex.testcontainers.InfinispanContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.util.StringPropertyReplacer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static org.jboss.pnc.common.json.moduleconfig.microprofile.SchedulerMicroprofileConfig.SCHEDULER_URL_KEY;

public class RemoteServices {

    private static final Logger logger = LoggerFactory.getLogger(RemoteServices.class);

    @Rule
    public BPMWireMock bpm = new BPMWireMock(8088);

    protected static String authServerUrl;

    protected static String keycloakRealm = "newcastle-testcontainer";

    private static dasniko.testcontainers.keycloak.KeycloakContainer keycloakContainer;
    private static GenericContainer rexContainer;

    @BeforeClass
    public static void beforeAll() throws IOException {
        logger.info("Starting containers ...");
        startRemoteServices();

        // 8080 IS JBOSS CONTAINER
        // 8088 IS BPM WIREMOCK MOCK
        Testcontainers.exposeHostPorts(8080, 8088);
        logger.info("Containers started.");
    }

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() throws IOException {
        return Deployments.testEar();
    }

    @AfterClass
    public static void afterAll() throws InterruptedException {
        logger.info("Stopping containers ...");
        keycloakContainer.stop();
        rexContainer.stop();
        logger.info("Containers stopped.");
        Thread.sleep(1000L); // make sure all resources are released
    }

    protected static void startRemoteServices() throws IOException {
        // properties to share port numbers
        Properties testProperties = new Properties();
        try (InputStream propFile = new FileInputStream("target/test.properties")) {
            testProperties.load(propFile);
        }

        Network network = Network.newNetwork();

        Consumer<OutputFrame> keycloakLogConsumer = frame -> logger.debug("KEYCOAK >>" + frame.getUtf8String());

        String keycloakHostPort = testProperties.getProperty(GetFreePort.KEYCLOAK_PORT);
        String keycloakPortBinding = keycloakHostPort + ":" + 8080; // 8080 is in-container port
        keycloakContainer = new CustomKeycloakContainer("quay.io/keycloak/keycloak:21.1.0").withNetwork(network)
                .withLogConsumer(keycloakLogConsumer)
                .withNetworkAliases("keycloak")
                .withRealmImportFile("keycloak-realm-export.json")
                .withAccessToHost(true)
                .withStartupAttempts(5);
        // .withReuse(true);
        keycloakContainer.setPortBindings(List.of(keycloakPortBinding));
        logger.info("Starting keycloak and binding it to port {}.", keycloakHostPort);
        keycloakContainer.start();

        BuildTest.authServerUrl = keycloakContainer.getAuthServerUrl();

        // int rexHostPort = GetFreePort.getFreeHostPort();
        int rexHostPort = 5679;
        logger.info("Rex container will bind to host port: {}.", rexHostPort);
        String rexPortBinding = rexHostPort + ":" + 8080; // 8080 is in-container port

        Consumer<OutputFrame> rexLogConsumer = frame -> logger.debug("REX >>" + frame.getUtf8String());

        rexContainer = new GenericContainer(DockerImageName.parse("quay.io/rh-newcastle/rex:latest"))
                // DockerImageName.parse("localhost/-your-name/rex-core:1.0.0-SNAPSHOT"))
                .withAccessToHost(true)
                .withNetwork(network)
                .withNetworkAliases("rex")
                .withAccessToHost(true)
                .withLogConsumer(rexLogConsumer)
                .withClasspathResourceMapping(
                        "rex-application.yaml",
                        "/home/jboss/config/application.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(Wait.forLogMessage(".*Installed features:.*", 1))
                .withStartupAttempts(5);
        // .withReuse(true);

        InfinispanContainer ispn = new InfinispanContainer(false).withNetwork(network)
                .withNetworkAliases("infinispan")
                .withStartupAttempts(5);
        ispn.start();

        rexContainer.setPortBindings(List.of(rexPortBinding));

        rexContainer.start();

        String rexHost = rexContainer.getHost();
        logger.info("Rex host: {}", rexHost);
        logger.info("Rex port: {}", rexHostPort);
        System.setProperty(SCHEDULER_URL_KEY, "http://" + rexHost + ":" + rexHostPort);

        Path configFile = Path.of(System.getProperty("pnc-config-path"));
        logger.info("Updating config file {}.", configFile);
        String config = Files.readString(configFile);
        Properties properties = new Properties();
        properties.put("PNC_SCHEDULER_BASE_URL", "http://localhost:" + rexHostPort);
        properties.put("PNC_UI_KEYCLOAK_URL", BuildTest.authServerUrl);
        String replacedConfig = StringPropertyReplacer.replaceProperties(config, properties);
        Files.writeString(configFile, replacedConfig);
    }
}
