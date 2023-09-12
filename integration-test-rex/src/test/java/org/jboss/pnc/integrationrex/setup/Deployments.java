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
package org.jboss.pnc.integrationrex.setup;

import org.jboss.pnc.integrationrex.mock.RemoteBuildsCleanerMock;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Deployments {

    public static final Logger logger = LoggerFactory.getLogger(Deployments.class);

    public static final String AUTH_JAR = "/auth.jar";
    public static final String COORDINATOR_JAR = "/build-coordinator.jar";

    private static final PomEquippedResolveStage resolver = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importDependencies(ScopeType.TEST);
    private static final Map<String, File[]> instanceHolder = new ConcurrentHashMap<>();

    public static EnterpriseArchive testEar() {
        EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, getBaseEar());

        addTestPersistenceXml(ear);
        ear.setApplicationXML("application.xml");

        addMocks(ear);
        setupAuthentication();

        logger.info("Ear archive listing: {}", ear.toString(true));

        return ear;
    }

    private static File getBaseEar() {
        File[] files = instanceHolder.computeIfAbsent(
                "EAR",
                (k) -> resolver.resolve("org.jboss.pnc:ear-package:ear:?").withoutTransitivity().asFile());
        return Arrays.stream(files)
                .filter(f -> f.getName().contains("ear-package"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("ear-package archive not found."));
    }

    private static void addTestPersistenceXml(EnterpriseArchive enterpriseArchive) {
        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addAsManifestResource("test-ds.xml", "persistence.xml");
    }

    private static void addMocks(EnterpriseArchive enterpriseArchive) {
        JavaArchive authJar = enterpriseArchive.getAsType(JavaArchive.class, AUTH_JAR);

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, COORDINATOR_JAR);
        coordinatorJar.deleteClass(org.jboss.pnc.coordinator.maintenance.DefaultRemoteBuildsCleaner.class);
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        authJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        logger.info(authJar.toString(true));

        enterpriseArchive.addAsModule(authJar);
    }

    private static void setupAuthentication() {
        // This secret is set in the keycloak-realm-export.json
        // The path of the secret file is also set in pnc-config.json for the service account
        try {
            Files.write(
                    Paths.get("/tmp/integration-test-rex-sa-secret"),
                    "gatp28CxLfslmahNqbqm8o3BMR7NPRye".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
