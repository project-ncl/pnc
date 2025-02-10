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

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.integrationrex.setup.Deployments;
import org.jboss.pnc.integrationrex.setup.arquillian.AfterDeploy;
import org.jboss.pnc.integrationrex.setup.arquillian.AfterUnDeploy;
import org.jboss.pnc.integrationrex.setup.arquillian.BeforeDeploy;
import org.jboss.pnc.integrationrex.testcontainers.InfinispanContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.util.StringPropertyReplacer;
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

    protected static String authServerUrl;

    protected static String keycloakRealm = "newcastle-testcontainer";

    private static final KeycloakContainer keycloakContainer;
    private static final Properties testProperties;

    private static GenericContainer rexContainer;
    private static InfinispanContainer ispnContainer;

    private static final Network containerNetwork = Network.newNetwork();

    static {
        testProperties = initTestProperties();

        // REUSE Keycloak container throughout all tests
        keycloakContainer = createKeycloakContainer();
        keycloakContainer.start();

        authServerUrl = keycloakContainer.getAuthServerUrl();
    }

    @BeforeDeploy
    public static void startContainers() throws IOException {
        logger.info("Starting containers ...");
        System.out.println("Starting containers");
        startRemoteServices();
    }

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() throws IOException {
        return Deployments.testEar();
    }

    @AfterDeploy
    public static void exposeHostPorts() {

        // 8080 IS JBOSS CONTAINER
        // 8088 IS BPM WIREMOCK MOCK
        Testcontainers.exposeHostPorts(8080, 8088);
        logger.info("Containers started.");
    }

    @AfterUnDeploy
    public static void stopContainers() throws InterruptedException {
        logger.info("Stopping containers ...");
        ispnContainer.stop();
        rexContainer.stop();
        logger.info("Containers stopped.");
    }

    private static Properties initTestProperties() {
        // properties to share port numbers
        Properties properties = new Properties();
        try (InputStream propFile = new FileInputStream("target/test.properties")) {
            properties.load(propFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static KeycloakContainer createKeycloakContainer() {
        Consumer<OutputFrame> keycloakLogConsumer = frame -> logger
                .debug("KEYCLOAK >>" + frame.getUtf8StringWithoutLineEnding());

        String keycloakHostPort = testProperties.getProperty(GetFreePort.KEYCLOAK_PORT);
        String keycloakPortBinding = keycloakHostPort + ":" + 8080; // 8080 is in-container port
        KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:21.1.0")
                .withNetwork(containerNetwork)
                .withLogConsumer(keycloakLogConsumer)
                .withNetworkAliases("keycloak")
                .withRealmImportFile("keycloak-realm-export.json")
                .withAccessToHost(true)
                .withStartupAttempts(5);

        // Force JWT issuer field to this URL to get through issuer verification for tokens originating in REX
        keycloak.withEnv("KC_HOSTNAME_URL", "http://localhost:" + keycloakHostPort + "/");

        keycloak.setPortBindings(List.of(keycloakPortBinding));
        logger.info("Starting keycloak and binding it to port {}.", keycloakHostPort);

        return keycloak;
    }

    protected static void startRemoteServices() throws IOException {
        // int rexHostPort = GetFreePort.getFreeHostPort();
        int rexHostPort = 5679;
        logger.info("Rex container will bind to host port: {}.", rexHostPort);
        String rexPortBinding = rexHostPort + ":" + 8080; // 8080 is in-container port

        ispnContainer = createInfinispanContainer();

        rexContainer = createRexContainer(ispnContainer, rexPortBinding);

        ispnContainer.start();
        rexContainer.start();

        String rexHost = rexContainer.getHost();
        logger.info("Rex host: {}", rexHost);
        logger.info("Rex port: {}", rexHostPort);
        System.setProperty(SCHEDULER_URL_KEY, "http://" + rexHost + ":" + rexHostPort);

        updatePncConfigJson(rexHostPort);
    }

    private static void updatePncConfigJson(int rexHostPort) throws IOException {
        Path configFile = Path.of(System.getProperty("pnc-config-path"));
        logger.info("Updating config file {}.", configFile);

        String config = Files.readString(configFile);
        Properties properties = new Properties();
        properties.put("PNC_SCHEDULER_BASE_URL", "http://localhost:" + rexHostPort);
        properties.put("PNC_UI_KEYCLOAK_URL", BuildTest.authServerUrl);
        String replacedConfig = StringPropertyReplacer.replaceProperties(config, properties);
        Files.writeString(configFile, replacedConfig);
    }

    private static InfinispanContainer createInfinispanContainer() {
        return new InfinispanContainer(false).withNetwork(containerNetwork)
                .withNetworkAliases("infinispan")
                .withStartupAttempts(5);
    }

    private static GenericContainer createRexContainer(InfinispanContainer ispn, String portBinding) {
        Consumer<OutputFrame> rexLogConsumer = frame -> logger.debug("REX >>" + frame.getUtf8StringWithoutLineEnding());

        GenericContainer rex = new GenericContainer(DockerImageName.parse("quay.io/rh-newcastle-devel/rex:latest"))
                // DockerImageName.parse("localhost/<<your-name>>/rex:1.0.2-SNAPSHOT"))
                .withNetwork(containerNetwork)
                .withNetworkAliases("rex")
                .withAccessToHost(true)
                .withLogConsumer(rexLogConsumer)
                .withClasspathResourceMapping(
                        "rex-application.yaml",
                        "/home/jboss/config/application.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(Wait.forLogMessage(".*Installed features:.*", 1))
                .withStartupAttempts(5)
                .dependsOn(ispn, keycloakContainer);

        rex.setPortBindings(List.of(portBinding));
        return rex;
    }
}
