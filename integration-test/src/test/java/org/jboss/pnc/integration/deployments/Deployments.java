/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.deployments;

import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import java.io.File;
import java.util.stream.Stream;

public class Deployments {

    public static EnterpriseArchive baseEar() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class);
        PomEquippedResolveStage mavenResolver = Maven.resolver().loadPomFromFile(new File("pom.xml"));

        addEar(ear, mavenResolver);
        addTestCommonWithoutTransitives(ear, mavenResolver);

        setTestableWar(ear);

        addTestPersistenceXml(ear);

        return ear;
    }

    public static EnterpriseArchive baseEarWithTestDependencies() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class);
        PomEquippedResolveStage mavenResolver = Maven.resolver().loadPomFromFile(new File("pom.xml"));

        addEar(ear, mavenResolver);
        addTestCommonWithTransitives(ear, mavenResolver);

        setTestableWar(ear);

        addTestPersistenceXml(ear);

        return ear;
    }

    private static void setTestableWar(EnterpriseArchive ear) {
        WebArchive restWar = ear.getAsType(WebArchive.class, "/pnc-rest.war");
        ear.addAsModule(Testable.archiveToTest(restWar));
    }

    private static void addEar(Archive<?> webArchive, PomEquippedResolveStage mavenResolver) {
        File[] manuallyAddedLibs = mavenResolver.resolve("org.jboss.pnc:ear-package:ear:?").withoutTransitivity().asFile();
        Stream.of(manuallyAddedLibs).forEach(lib -> webArchive.merge(ShrinkWrap.create(ZipImporter.class).
                importFrom(lib).as(GenericArchive.class)));
    }

    private static void addTestCommonWithTransitives(EnterpriseArchive webArchive, PomEquippedResolveStage mavenResolver) {
        File[] manuallyAddedLibs = mavenResolver.resolve("org.jboss.pnc:test-common").withTransitivity().asFile();
        webArchive.addAsLibraries(manuallyAddedLibs);
    }

    private static void addTestCommonWithoutTransitives(EnterpriseArchive webArchive, PomEquippedResolveStage mavenResolver) {
        File[] manuallyAddedLibs = mavenResolver.resolve("org.jboss.pnc:test-common").withoutTransitivity().asFile();
        webArchive.addAsLibraries(manuallyAddedLibs);
    }

    private static void addTestPersistenceXml(EnterpriseArchive enterpriseArchive) {
        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addAsManifestResource("test-ds.xml", "persistence.xml");
    }
}
