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
package org.jboss.pnc.datastore;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils.addManifestDependencies;

public class DeploymentFactory {

    private static Logger logger = LoggerFactory.getLogger(DeploymentFactory.class);

    public static Archive<?> createDatastoreDeployment() {

        JavaArchive datastoreJar = ShrinkWrap.create(JavaArchive.class, "datastore.jar")
                .addPackages(true, "org.jboss.pnc.datastore")
                .addAsManifestResource("test-persistence.xml", "persistence.xml")
                .addAsManifestResource("logback.xml");

        logger.info("Deployment datastoreJar: {}", datastoreJar.toString(true));

        File[] dependencies = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importDependencies(
                        ScopeType.RUNTIME,
                        ScopeType.COMPILE,
                        ScopeType.TEST,
                        ScopeType.IMPORT,
                        ScopeType.SYSTEM)
                .resolve()
                .withTransitivity()
                .asFile();

        // remove "model-<version>.jar" from the archive and add it as "model.jar" so we can reference it in the
        // test-persistence.xml
        ObjectWrapper<File> modelJarWrapper = new ObjectWrapper();
        List<File> dependenciesFiltered = Arrays.stream(dependencies)
                .filter(jar -> extractModelJar(jar, modelJarWrapper))
                .filter(jar -> !mockJarMatches(jar))
                .collect(Collectors.toList());
        File modelJar = modelJarWrapper.get();
        if (modelJar == null) {
            throw new RuntimeException(new DeploymentException("Cannot find model*.jar"));
        }

        Optional<File> mockJarOptional = Arrays.stream(dependencies)
                .filter(DeploymentFactory::mockJarMatches)
                .findAny();
        if (!mockJarOptional.isPresent()) {
            throw new RuntimeException(new DeploymentException("Cannot find mock*.jar"));
        }

        JavaArchive mockJar = ShrinkWrap.createFromZipFile(JavaArchive.class, mockJarOptional.get());
        mockJar.addAsManifestResource("mock-beans.xml", "beans.xml");

        logger.info("Deployment mockJar: {}", mockJar.toString(true));

        EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class, "datastore-test.ear")
                .addAsModule(datastoreJar)
                .addAsLibraries(dependenciesFiltered.toArray(new File[dependenciesFiltered.size()]))
                .addAsLibrary(mockJar)
                .addAsLibrary(modelJar, "model.jar");

        addManifestDependencies(enterpriseArchive, "com.google.guava  export meta-inf");

        logger.info("Deployment: {}", enterpriseArchive.toString(true));

        return enterpriseArchive;
    }

    private static boolean mockJarMatches(File jar) {
        return jar.getName().matches("pnc-mock.*\\.jar");
    }

    private static boolean extractModelJar(File jar, ObjectWrapper<File> modelJar) {
        if (jar.getName().matches("model.*\\.jar")
                && (jar.getAbsolutePath().contains("org/jboss/pnc/model") || /* if it comes from local repo */
                        jar.getAbsolutePath().contains("model/target/model"))) /* if it comes from full build */ {
            modelJar.set(jar);
            return false;
        } else {
            return true;
        }
    }

}
