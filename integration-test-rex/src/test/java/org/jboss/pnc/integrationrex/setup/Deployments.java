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

import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.integrationrex.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.integrationrex.mock.client.KeycloakServiceClientMock;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    public static final String REST_WAR = "/rest.war";

    private static final PomEquippedResolveStage resolver = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importDependencies(ScopeType.TEST);
    private static final Map<String, File[]> instanceHolder = new ConcurrentHashMap<>();

    public static EnterpriseArchive testEar() {
        EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, getBaseEar());

        // WebArchive restWar = prepareRestArchive(ear);
        // ear.addAsModule(archiveToTest(restWar));

        addTestPersistenceXml(ear);
        ear.setApplicationXML("application.xml");

        addMocks(ear);

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

    private static WebArchive prepareRestArchive(EnterpriseArchive ear) {
        WebArchive restWar = ear.getAsType(WebArchive.class, REST_WAR);
        // restWar.addAsWebInfResource("WEB-INF/web.xml", "web.xml");
        // restWar.addAsWebInfResource("WEB-INF/jboss-web.xml");
        logger.info("REST archive listing: {}", restWar.toString(true));
        return restWar;
    }

    private static void addMocks(EnterpriseArchive enterpriseArchive) {
        JavaArchive authJar = enterpriseArchive.getAsType(JavaArchive.class, AUTH_JAR);

        authJar.deleteClass(DefaultKeycloakServiceClient.class);
        authJar.addClass(KeycloakServiceClientMock.class);

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, COORDINATOR_JAR);
        coordinatorJar.deleteClass(org.jboss.pnc.coordinator.maintenance.DefaultRemoteBuildsCleaner.class);
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        authJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        logger.info(authJar.toString(true));

        enterpriseArchive.addAsModule(authJar);
    }

}
