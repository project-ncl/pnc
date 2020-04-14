/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new.setup;

import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.executor.DefaultBuildExecutor;
import org.jboss.pnc.mock.auth.KeycloakServiceClientMock;
import org.jboss.pnc.mock.builddriver.BuildDriverResultMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerResultMock;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.arquillian.container.test.api.Testable.archiveToTest;
import static org.jboss.pnc.AbstractTest.AUTH_JAR;
import static org.jboss.pnc.AbstractTest.EXECUTOR_JAR;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Deployments {

    public static final Logger logger = LoggerFactory.getLogger(Deployments.class);

    private static final PomEquippedResolveStage resolver = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importDependencies(ScopeType.TEST);
    private static final Map<String, File[]> instanceHolder = new ConcurrentHashMap<>();

    private static File getBaseEar() {
        File[] files = instanceHolder.computeIfAbsent(
                "EAR",
                (k) -> resolver.resolve("org.jboss.pnc:ear-package:ear:?").withoutTransitivity().asFile());
        return Arrays.stream(files)
                .filter(f -> f.getName().contains("ear-package"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("ear-package archive not found."));
    }

    private static File getTestCommon() {
        File[] files = instanceHolder.computeIfAbsent("TEST-COMMON", (k) -> {
            logger.info("Resolving org.jboss.pnc:test-common.");
            return resolver.resolve("org.jboss.pnc:test-common:?").withoutTransitivity().asFile();
        });
        return Arrays.stream(files)
                .filter(f -> f.getName().contains("test-common"))
                .findAny()
                .orElseThrow(() -> new RuntimeException("test-common archive not found."));
    }

    public static EnterpriseArchive testEar() {
        EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, getBaseEar());

        WebArchive restWar = prepareRestArchive(ear);
        ear.addAsModule(archiveToTest(restWar));
        // remove the old rest
        ear.delete("rest.war");

        addTestPersistenceXml(ear);
        ear.setApplicationXML("application-new.xml");

        addKeycloakServiceClientMock(ear);

        logger.info("Ear archive listing: {}", ear.toString(true));

        return ear;
    }

    public static EnterpriseArchive testEarForInContainerTest() {
        EnterpriseArchive ear = testEar();
        ear.addAsLibraries(getTestCommon());
        return ear;
    }

    /**
     *
     * @param classes to add to the deployment
     * @return
     */
    public static EnterpriseArchive testEarForInContainerTest(Class<?>... classes) {
        EnterpriseArchive ear = testEarForInContainerTest();
        WebArchive restWar = ear.getAsType(WebArchive.class, "/rest-new.war");
        restWar.addClasses(classes);
        return ear;
    }

    /**
     * @param packages to add
     * @params packagesRecursive to add packages recursively
     * @param classes to add to the deployment
     *
     * @return
     */
    public static EnterpriseArchive testEarForInContainerTest(
            List<Package> packages,
            List<Package> packagesRecursive,
            Class<?>... classes) {
        EnterpriseArchive ear = testEarForInContainerTest();
        WebArchive restWar = ear.getAsType(WebArchive.class, "/rest-new.war");
        restWar.addClasses(classes);

        if (packages != null) {
            for (Package pkg : packages) {
                restWar.addPackage(pkg);
            }
        }
        if (packagesRecursive != null) {
            for (Package pkg : packagesRecursive) {
                restWar.addPackages(true, pkg);
            }
        }
        return ear;
    }

    private static void addTestPersistenceXml(EnterpriseArchive enterpriseArchive) {
        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addAsManifestResource("test-ds.xml", "persistence.xml");
    }

    private static WebArchive prepareRestArchive(EnterpriseArchive ear) {
        WebArchive restWar = ear.getAsType(WebArchive.class, "/rest-new.war");
        restWar.addAsWebInfResource("WEB-INF/web-new.xml", "web.xml");
        restWar.addAsWebInfResource("WEB-INF/jboss-web.xml");
        logger.info("REST archive listing: {}", restWar.toString(true));
        return restWar;
    }

    private static void addKeycloakServiceClientMock(EnterpriseArchive enterpriseArchive) {
        JavaArchive jar = enterpriseArchive.getAsType(JavaArchive.class, AUTH_JAR);

        jar.deleteClass(DefaultKeycloakServiceClient.class);
        jar.addClass(KeycloakServiceClientMock.class);

        jar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        logger.info(jar.toString(true));

        enterpriseArchive.addAsModule(jar);
    }

    public static void addBuildExecutorMock(EnterpriseArchive enterpriseArchive) {
        JavaArchive jar = enterpriseArchive.getAsType(JavaArchive.class, EXECUTOR_JAR);

        jar.deleteClass(DefaultBuildExecutor.class);

        jar.addPackage(BuildExecutorMock.class.getPackage());
        jar.addClass(BuildDriverResultMock.class);
        jar.addClass(RepositoryManagerResultMock.class);
        jar.addClass(ArtifactBuilder.class);

        jar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        logger.info(jar.toString(true));

        enterpriseArchive.addAsModule(jar);

    }
}
