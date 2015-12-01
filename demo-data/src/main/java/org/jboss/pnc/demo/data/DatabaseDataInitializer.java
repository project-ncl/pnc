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
package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationAuditedSpringRepository;
import org.jboss.pnc.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.model.*;
import org.jboss.pnc.model.ProductRelease.SupportLevel;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.*;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Data for the DEMO. Note: The database initialization requires two separate transactions in order for the build configuration
 * audit record to be created and then linked to a build record.
 */
@Singleton
public class DatabaseDataInitializer {

    public static final Logger log = Logger.getLogger(DatabaseDataInitializer.class.getName());

    private static final String PNC_PRODUCT_NAME = "Project Newcastle Demo Product";
    private static final String PNC_PRODUCT_VERSION = "1.0";
    private static final String PNC_PRODUCT_RELEASE = "1.0.0.GA";
    private static final String PNC_PRODUCT_MILESTONE = "1.0.0.Build1";
    private static final String PNC_PROJECT_1_NAME = "Project Newcastle Demo Project 1";
    private static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    BuildConfigurationAuditedSpringRepository buildConfigurationAuditedRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductMilestoneRepository productMilestoneRepository;

    @Inject
    ProductReleaseRepository productReleaseRepository;

    @Inject
    BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    BuildRecordSetRepository buildRecordSetRepository;

    @Inject
    BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    BuildSystemImageRepository environmentRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

    @Inject
    Datastore datastore;

    BuildConfiguration buildConfiguration1;

    BuildConfiguration buildConfiguration2;

    BuildConfigurationSet buildConfigurationSet1;

    ProductMilestone demoProductMilestone;

    User demoUser;
    User pncAdminUser;

    public void verifyData() {
        // Check number of entities in DB
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions.checkState(buildConfigurationRepository.count() > 0, "Expecting number of BuildConfigurations > 0");
        Preconditions.checkState(productVersionRepository.count() > 0, "Expecting number of ProductVersions > 0");
        Preconditions.checkState(buildConfigurationSetRepository.count() > 0, "Expecting number of BuildRepositorySets > 0");

        BuildConfiguration buildConfigurationDB = buildConfigurationRepository.queryAll().get(0);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(
                buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion() != null,
                "Product version of buildConfiguration must be not null");

        BuildConfigurationSet buildConfigurationSetDB = buildConfigurationSetRepository.queryAll().get(0);

        Preconditions.checkState(buildConfigurationSetDB.getProductVersion() != null,
                "Product version of buildConfigurationSet must be not null");

        // Check that mapping between Product and Build Configuration via BuildConfigurationSet is correct
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to Project must be " + PNC_PRODUCT_NAME);
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to Project must be " + PNC_PRODUCT_VERSION);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion()
                .getVersion().equals(PNC_PRODUCT_VERSION), "Product version mapped to BuildConfiguration must be "
                + PNC_PRODUCT_VERSION);
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion()
                .getProduct().getName().equals(PNC_PRODUCT_NAME), "Product mapped to BuildConfiguration must be "
                + PNC_PRODUCT_NAME);

        // Check data of BuildConfiguration
        Preconditions.checkState(buildConfigurationDB.getProject().getName().equals(PNC_PROJECT_1_NAME),
                "Project mapped to BuildConfiguration must be " + PNC_PROJECT_1_NAME);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeProjectProductData() {

        BuildEnvironment environment1Unsaved = BuildEnvironment.Builder.newBuilder()
                .name("Demo Environment 1")
                .build();
        BuildEnvironment environment1 = environmentRepository.save(environment1Unsaved);

        /*
         * All the bi-directional mapping settings are managed inside the Builders
         */
        // Example product and product version
        Product product = Product.Builder.newBuilder().name(PNC_PRODUCT_NAME).abbreviation("PNC")
                .description("Example Product for Project Newcastle Demo").productCode("PNC").pgmSystemName("newcastle")
                .build();
        product = productRepository.save(product);

        // Example product version, release, and milestone of the product
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().version(PNC_PRODUCT_VERSION).product(product)
                .build();
        productVersion = productVersionRepository.save(productVersion);

        demoProductMilestone = ProductMilestone.Builder.newBuilder().version(PNC_PRODUCT_MILESTONE)
                .productVersion(productVersion).build();
        buildRecordSetRepository.save(demoProductMilestone.getDistributedBuildRecordSet());
        buildRecordSetRepository.save(demoProductMilestone.getPerformedBuildRecordSet());
        demoProductMilestone = productMilestoneRepository.save(demoProductMilestone);

        ProductRelease productRelease = ProductRelease.Builder.newBuilder().version(PNC_PRODUCT_RELEASE)
                .productMilestone(demoProductMilestone).supportLevel(SupportLevel.EARLYACCESS)
                .build();
        productRelease = productReleaseRepository.save(productRelease);

        productVersion.setCurrentProductMilestone(demoProductMilestone);
        productVersion = productVersionRepository.save(productVersion);

        // Example projects
        Project project1 = Project.Builder.newBuilder().name(PNC_PROJECT_1_NAME)
                .description("Example Project for Newcastle Demo").projectUrl("https://github.com/project-ncl/pnc").build();
        Project project2 = Project.Builder.newBuilder().name("JBoss Modules").description("JBoss Modules Project")
                .projectUrl("https://github.com/jboss-modules/jboss-modules")
                .issueTrackerUrl("https://issues.jboss.org/browse/MODULES").build();
        Project project3 = Project.Builder.newBuilder().name("JBoss JavaEE Servlet Spec API")
                .description("JavaEE Servlet Spec API").projectUrl("https://github.com/jboss/jboss-servlet-api_spec")
                .issueTrackerUrl("https://issues.jboss.org/browse/JBEE").build();
        Project project4 = Project.Builder.newBuilder().name("Fabric8")
                .description("Integration platform for working with Apache ActiveMQ, Camel, CXF and Karaf in the cloud")
                .projectUrl("https://github.com/fabric8io/fabric8")
                .issueTrackerUrl("https://github.com/fabric8io/fabric8/issues").build();
        Project project5 = Project.Builder.newBuilder().name("Maven Plugin Test")
                .description("Sample Maven Project with plugins and external downloads")
                .projectUrl("https://github.com/rnc/mvn-plugin-test").build();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);

        // Example build configurations
        buildConfiguration1 = BuildConfiguration.Builder.newBuilder().name(PNC_PROJECT_BUILD_CFG_ID).project(project1)
                .description("Test build config for project newcastle").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").scmRepoURL("https://github.com/project-ncl/pnc.git")
                .scmRevision("*/v0.2").build();
        buildConfiguration1 = buildConfigurationRepository.save(buildConfiguration1);

        buildConfiguration2 = BuildConfiguration.Builder.newBuilder().name("jboss-modules-1.5.0").project(project2)
                .description("Test config for JBoss modules build master branch.").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRepoURL("https://github.com/jboss-modules/jboss-modules.git")
                .scmRevision("9e7115771a791feaa5be23b1255416197f2cda38").build();
        buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

        BuildConfiguration buildConfiguration3 = BuildConfiguration.Builder.newBuilder().name("jboss-servlet-spec-api-1.0.1")
                .project(project3).description("Test build for jboss java servlet api").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRepoURL("https://github.com/jboss/jboss-servlet-api_spec.git").dependency(buildConfiguration2).build();
        buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

        BuildConfiguration buildConfiguration4 = BuildConfiguration.Builder.newBuilder().name("io-fabric8-2.2-SNAPSHOT")
                .project(project4).description("Test build for Fabric8").buildEnvironment(environment1)
                .buildScript("mvn clean deploy -DskipTests=true").scmRepoURL("https://github.com/fabric8io/fabric8.git")
                .build();
        buildConfiguration4 = buildConfigurationRepository.save(buildConfiguration4);

        BuildConfiguration buildConfiguration5 = BuildConfiguration.Builder.newBuilder().name("maven-plugin-test")
                .project(project5).description("Test build for Plugins with external downloads").buildEnvironment(environment1)
                .buildScript("mvn clean deploy").scmRepoURL("https://github.com/rnc/mvn-plugin-test.git")
                .build();
        buildConfiguration5 = buildConfigurationRepository.save(buildConfiguration5);

        // Build config set containing the three example build configs
        buildConfigurationSet1 = BuildConfigurationSet.Builder.newBuilder().name("Build Config Set 1")
                .buildConfiguration(buildConfiguration1).buildConfiguration(buildConfiguration2)
                .buildConfiguration(buildConfiguration3).productVersion(productVersion).build();

        BuildConfigurationSet buildConfigurationSet2 = BuildConfigurationSet.Builder.newBuilder()
                .name("Fabric Configuration Set").buildConfiguration(buildConfiguration4).
                        productVersion(productVersion).build();

        demoUser = User.Builder.newBuilder().username("demo-user").firstName("Demo First Name").lastName("Demo Last Name")
                .email("demo-user@pnc.com").build();

        pncAdminUser = User.Builder.newBuilder().username("pnc-admin").firstName("pnc-admin").lastName("pnc-admin")
                .email("pnc-admin@pnc.com").build();

        buildConfigurationSetRepository.save(buildConfigurationSet1);
        buildConfigurationSetRepository.save(buildConfigurationSet2);
        demoUser = userRepository.save(demoUser);
        pncAdminUser = userRepository.save(pncAdminUser);

    }

    /**
     * Build record needs to be initialized in a separate transaction so that the audited build configuration can be set.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeBuildRecordDemoData() {

        Artifact builtArtifact1 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/built1")
                .status(ArtifactStatus.BINARY_BUILT).build();
        Artifact builtArtifact2 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/built2")
                .status(ArtifactStatus.BINARY_BUILT).build();

        Artifact importedArtifact1 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/imported1")
                .status(ArtifactStatus.BINARY_IMPORTED).build();
        Artifact importedArtifact2 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/imported2")
                .status(ArtifactStatus.BINARY_IMPORTED).build();

        Set<BuildRecord> buildRecords = new HashSet<BuildRecord>();

        final int INITIAL_REVISION = 1;
        IdRev buildConfig1AuditIdRev = new IdRev(buildConfiguration1.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited1 = buildConfigurationAuditedRepository.findOne(buildConfig1AuditIdRev);
        if (buildConfigAudited1 != null) {

            int nextId = datastore.getNextBuildRecordId();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder().id(nextId)
                    .latestBuildConfiguration(buildConfiguration1)
                    .buildConfigurationAudited(buildConfigAudited1)
                    .submitTime(Timestamp.from(Instant.now()))
                    .startTime(Timestamp.from(Instant.now()))
                    .endTime(Timestamp.from(Instant.now()))
                    .builtArtifact(builtArtifact1).builtArtifact(builtArtifact2)
                    .builtArtifact(importedArtifact1).builtArtifact(importedArtifact2)
                    .user(demoUser)
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .build();

            buildRecordRepository.save(buildRecord1);
            buildRecords.add(buildRecord1);

            BuildRecordSet performedBuildRecordSet = demoProductMilestone.getPerformedBuildRecordSet();
            performedBuildRecordSet.addBuildRecord(buildRecord1);
            buildRecordSetRepository.save(performedBuildRecordSet);
            BuildRecordSet distributedBuildRecordSet = demoProductMilestone.getDistributedBuildRecordSet();
            distributedBuildRecordSet.addBuildRecord(buildRecord1);
            buildRecordSetRepository.save(distributedBuildRecordSet);
        }

        Artifact builtArtifact3 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/built3")
                .status(ArtifactStatus.BINARY_BUILT).build();
        Artifact builtArtifact4 = Artifact.Builder.newBuilder().identifier("test").deployUrl("http://google.pl/built4")
                .status(ArtifactStatus.BINARY_BUILT).build();

        IdRev buildConfig2AuditIdRev = new IdRev(buildConfiguration2.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited2 = buildConfigurationAuditedRepository.findOne(buildConfig2AuditIdRev);
        if (buildConfigAudited2 != null) {

            int nextId = datastore.getNextBuildRecordId();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder().id(nextId)
                    .latestBuildConfiguration(buildConfiguration2)
                    .buildConfigurationAudited(buildConfigAudited2)
                    .submitTime(Timestamp.from(Instant.now()))
                    .startTime(Timestamp.from(Instant.now()))
                    .endTime(Timestamp.from(Instant.now()))
                    .builtArtifact(builtArtifact3)
                    .builtArtifact(builtArtifact4)
                    .user(demoUser)
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .build();

            buildRecordRepository.save(buildRecord2);
            buildRecords.add(buildRecord2);
        }

        BuildConfigSetRecord buildConfigSetRecord1 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .startTime(Timestamp.from(Instant.now())).endTime(Timestamp.from(Instant.now())).user(demoUser)
                .status(BuildStatus.FAILED).build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord1);

        BuildConfigSetRecord buildConfigSetRecord2 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .buildRecords(buildRecords).startTime(Timestamp.from(Instant.now())).endTime(Timestamp.from(Instant.now()))
                .user(demoUser).status(BuildStatus.SUCCESS).build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord2);
    }

}
