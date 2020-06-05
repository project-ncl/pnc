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
package org.jboss.pnc.integration.deployments;

import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.executor.DefaultBuildExecutor;
import org.jboss.pnc.integration.client.RestClient;
import org.jboss.pnc.integration.env.IntegrationTestEnv;
import org.jboss.pnc.mock.builddriver.BuildDriverResultMock;
import org.jboss.pnc.mock.client.KeycloakServiceClientMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerResultMock;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ArchiveImportException;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.jboss.arquillian.container.test.api.Testable.archiveToTest;
import static org.jboss.pnc.AbstractTest.AUTH_JAR;
import static org.jboss.pnc.AbstractTest.EXECUTOR_JAR;
import static org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils.addManifestDependencies;

public class Deployments {
    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

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

    private static File[] getTestCommon() {
        return instanceHolder.computeIfAbsent("TEST-COMMON", (k) -> {
            logger.info("Resolving org.jboss.pnc:test-common.");
            return resolver.resolve("org.jboss.pnc:test-common:?").withTransitivity().asFile();
        });
    }

    private static File[] getMockito() {
        return instanceHolder.computeIfAbsent(
                "MOCKITO",
                (k) -> resolver.resolve("org.mockito:mockito-core").withTransitivity().asFile());
    }

    public static EnterpriseArchive baseEar() {
        ArquillianDeploymentFactory arquillianDeploymentFactory = new ArquillianDeploymentFactory();
        if (useTargetBuilds()) {
            logger.info("Create test ear from build target");
            return arquillianDeploymentFactory.createDeployment();
        }

        EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, getBaseEar());
        ear.addAsLibraries(getTestCommon());

        setTestableWar(ear);

        addTestPersistenceXml(ear);
        addTestApplicaitonXml(ear);

        addKeycloakServiceClientMock(ear);

        logger.info("Ear archive listing: {}", ear.toString(true));

        return ear;
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

    public static void addKeycloakServiceClientMock(EnterpriseArchive enterpriseArchive) {
        JavaArchive jar = enterpriseArchive.getAsType(JavaArchive.class, AUTH_JAR);

        jar.deleteClass(DefaultKeycloakServiceClient.class);
        jar.addClass(KeycloakServiceClientMock.class);

        jar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        logger.info(jar.toString(true));

        enterpriseArchive.addAsModule(jar);

    }

    public static EnterpriseArchive baseEarWithTestDependencies() {
        ArquillianDeploymentFactory arquillianDeploymentFactory = new ArquillianDeploymentFactory();
        if (useTargetBuilds()) {
            logger.info("Create test ear with test dependencies from build target");
            return arquillianDeploymentFactory.createDeploymentWithTestDependencies();
        }

        EnterpriseArchive ear = ShrinkWrap.createFromZipFile(EnterpriseArchive.class, getBaseEar());
        ear.addAsLibraries(getTestCommon());

        setTestableWar(ear);

        addTestPersistenceXml(ear);

        ear.addAsLibraries(getMockito());

        addTestApplicaitonXml(ear);

        if (arquillianDeploymentFactory.isCreateArchiveCopy()) {
            arquillianDeploymentFactory.writeArchiveToFile(
                    ear,
                    new File("target", ArquillianDeploymentFactory.INTEGRATION_TEST_MODULE_DIR + ".ear"));
        }

        addKeycloakServiceClientMock(ear);
        /** jdk.unsupported is required by Mockito to run with Java 11. */
        addManifestDependencies(ear, "jdk.unsupported");

        logger.info("Ear archive listing: {}", ear.toString(true));

        return ear;
    }

    /**
     * Use application.xml without messaging module.
     * 
     * @param ear
     */
    private static void addTestApplicaitonXml(EnterpriseArchive ear) {
        ear.setApplicationXML("application.xml");
    }

    private static boolean useTargetBuilds() {
        return System.getProperty("useTargetBuilds") != null;
    }

    private static void setTestableWar(EnterpriseArchive ear) {
        WebArchive restWar = ear.getAsType(WebArchive.class, "/rest.war");
        restWar.addAsWebInfResource("WEB-INF/web.xml");
        restWar.addAsWebInfResource("WEB-INF/jboss-web.xml");
        logger.info("REST archive listing: {}", restWar.toString(true));
        ear.addAsModule(archiveToTest(restWar));
    }

    private static void addTestCommonWithTransitives(
            EnterpriseArchive webArchive,
            PomEquippedResolveStage mavenResolver) {
        File[] manuallyAddedLibs = mavenResolver.resolve("org.jboss.pnc:test-common").withTransitivity().asFile();
        webArchive.addAsLibraries(manuallyAddedLibs);
    }

    private static void addTestPersistenceXml(EnterpriseArchive enterpriseArchive) {
        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addAsManifestResource("test-ds.xml", "persistence.xml");
    }

    public static void addRestClients(EnterpriseArchive enterpriseArchive) {
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(IntegrationTestEnv.class);
        war.addPackages(true, RestClient.class.getPackage());
    }

    static class ArquillianDeploymentFactory {
        private static final String TARGET_DIR = "target";
        private static final String DEPLOYMENT_NAME = "integration-test";
        private static final String TEST_EAR = DEPLOYMENT_NAME + ".ear";
        private static final String TEST_COMMON_MODULE_DIR = "test-common";
        private static final String TEST_COMMON_JAR = TEST_COMMON_MODULE_DIR + ".jar";
        private static final String EAR_PACKAGE_MODULE_DIR = "ear-package";
        private static final String EAR_PACKAGE_EAR = EAR_PACKAGE_MODULE_DIR + ".ear";
        private static final String INTEGRATION_TEST_MODULE_DIR = "integration-test";
        private static final String INTEGRATION_TEST_JAR = INTEGRATION_TEST_MODULE_DIR + ".jar";
        private static final String REST_WAR = "rest.war";
        private static final String DATASTORE_JAR = "datastore.jar";
        private static final String TEST_PERSISTENCE_XML = "test-ds.xml";
        private static final String PERSISTENCE_XML = "persistence.xml";

        public EnterpriseArchive createDeployment() {
            EnterpriseArchive ear = createEar();
            ear.addAsModule(archiveToTest(createRestWar(ear)));
            ear.addAsLibrary(createTestCommonJar());
            addTestPersistenceXml(ear);
            if (isCreateArchiveCopy()) {
                writeArchiveToFile(ear, new File(ear.getName()));
            }
            return ear;
        }

        private EnterpriseArchive createEar() {
            File earFile = findArchiveFileInModuleTarget(EAR_PACKAGE_MODULE_DIR, EAR_PACKAGE_EAR);
            return createFromZipFile(EnterpriseArchive.class, earFile, TEST_EAR);
        }

        public EnterpriseArchive createDeploymentWithTestDependencies() {
            EnterpriseArchive ear = createEar();
            ear.addAsModule(archiveToTest(createRestWar(ear)));
            ear.addAsLibrary(createTestCommonJar());
            addDependenciesOfCommonsTestJar(ear);
            addTestPersistenceXml(ear);
            if (isCreateArchiveCopy()) {
                writeArchiveToFile(ear, new File(ear.getName()));
            }
            return ear;
        }

        private File findArchiveFileInModuleTarget(String moduleDir, String archiveName) {
            File earProjectBuildDir = new File(createModuleDir(moduleDir), TARGET_DIR);
            return new File(earProjectBuildDir, archiveName);
        }

        private File createModuleDir(String moduleDir) {
            return new File(getProjectTopLevelDir(), moduleDir);
        }

        private WebArchive createRestWar(EnterpriseArchive ear) {
            return ear.getAsType(WebArchive.class, "/" + REST_WAR);
        }

        private void addDependenciesOfCommonsTestJar(EnterpriseArchive ear) {
            File jarFileWithLibs = findArchiveFileInModuleTarget(INTEGRATION_TEST_MODULE_DIR, INTEGRATION_TEST_JAR);
            WebArchive war = ShrinkWrap.createFromZipFile(WebArchive.class, jarFileWithLibs);
            addLibrariesFromJarToEar(war, ear);
        }

        private JavaArchive createTestCommonJar() {
            File jarFile = findArchiveFileInModuleTarget(TEST_COMMON_MODULE_DIR, TEST_COMMON_JAR);
            return ShrinkWrap.createFromZipFile(JavaArchive.class, jarFile);
        }

        private void addLibrariesFromJarToEar(WebArchive war, EnterpriseArchive ear) {
            Filter<ArchivePath> filter = new Filter<ArchivePath>() {
                @Override
                public boolean include(ArchivePath path) {
                    return path.get().startsWith("/lib");
                }
            };
            // add libraries to ear and remove them from jar file
            Map<ArchivePath, Node> content = war.getContent(filter);
            for (Map.Entry<ArchivePath, Node> item : content.entrySet()) {
                Asset asset = item.getValue().getAsset();
                if (asset != null) {
                    ArchivePath archivePath = item.getKey();
                    ear.add(asset, archivePath);
                }
            }
        }

        private void addTestPersistenceXml(EnterpriseArchive ear) {
            JavaArchive datastoreJar = ear.getAsType(JavaArchive.class, "/" + DATASTORE_JAR);
            datastoreJar.addAsManifestResource(TEST_PERSISTENCE_XML, PERSISTENCE_XML);
        }

        private File getProjectTopLevelDir() {
            File projectTopLevelDir = new File("").getAbsoluteFile();
            if (!isProjectTopLevelDir(projectTopLevelDir)) {
                projectTopLevelDir = projectTopLevelDir.getParentFile();
                if (!isProjectTopLevelDir(projectTopLevelDir)) {
                    throw new IllegalStateException(
                            "Can not find project top level directory from " + projectTopLevelDir.getAbsolutePath());
                }
            }
            return projectTopLevelDir;
        }

        private boolean isProjectTopLevelDir(File dir) {
            File testModule = dir == null ? null : new File(dir, INTEGRATION_TEST_MODULE_DIR);
            return testModule != null && testModule.exists() && testModule.isDirectory();
        }

        private boolean isCreateArchiveCopy() {
            return System.getProperty("createArchiveCopy") != null;
        }

        private void writeArchiveToFile(Archive<?> archive, File file) {
            archive.as(ZipExporter.class).exportTo(file, true);
        }

        // copied from org.jboss.shrinkwrap.api.ArchiveFactory.createFromZipFile(final Class<T> type, final File
        // archiveFile) -
        // added parameter archiveName
        /**
         * Creates a new archive of the specified type as imported from the specified {@link File}. The file is expected
         * to be encoded as ZIP (ie. JAR/WAR/EAR). The name of the archive will be set to {@link File#getName()}. The
         * archive will be be backed by the {@link org.jboss.shrinkwrap.api.Configuration} specific to this
         * {@link org.jboss.shrinkwrap.api.ArchiveFactory}.
         *
         * @param type The type of the archive e.g. {@link org.jboss.shrinkwrap.api.spec.WebArchive}
         * @param archiveFile the archiveFile to use
         * @param archiveName the name of created archive
         * @return An {@link Assignable} view
         * @throws IllegalArgumentException If either argument is not supplied, if the specified {@link File} does not
         *         exist, or is not a valid ZIP file
         * @throws org.jboss.shrinkwrap.api.importer.ArchiveImportException If an error occurred during the import
         *         process
         */
        public <T extends Assignable> T createFromZipFile(
                final Class<T> type,
                final File archiveFile,
                String archiveName) throws IllegalArgumentException, ArchiveImportException {
            // Precondition checks
            if (type == null) {
                throw new IllegalArgumentException("Type must be specified");
            }
            if (archiveFile == null) {
                throw new IllegalArgumentException("File must be specified");
            }
            if (!archiveFile.exists()) {
                throw new IllegalArgumentException("File for import does not exist: " + archiveFile.getAbsolutePath());
            }
            if (archiveFile.isDirectory()) {
                throw new IllegalArgumentException(
                        "File for import must not be a directory: " + archiveFile.getAbsolutePath());
            }

            // Construct ZipFile
            final ZipFile zipFile;
            try {
                zipFile = new ZipFile(archiveFile);
            } catch (final ZipException ze) {
                throw new IllegalArgumentException(
                        "Does not appear to be a valid ZIP file: " + archiveFile.getAbsolutePath());
            } catch (final IOException ioe) {
                throw new RuntimeException(
                        "I/O Error in importing new archive from ZIP: " + archiveFile.getAbsolutePath(),
                        ioe);
            }

            // Import
            return ShrinkWrap.create(type, archiveName).as(ZipImporter.class).importFrom(zipFile).as(type);

        }
    }
}
