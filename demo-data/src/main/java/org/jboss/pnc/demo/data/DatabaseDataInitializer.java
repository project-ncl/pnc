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
package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;

import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.enums.*;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierAndSha256;

/**
 * Data for the DEMO. Note: The database initialization requires two separate transactions in order for the build
 * configuration audit record to be created and then linked to a build record.
 */
@Singleton
public class DatabaseDataInitializer {

    public static final Logger log = Logger.getLogger(DatabaseDataInitializer.class.getName());

    public static final String PNC_PRODUCT_NAME = "Project Newcastle Demo Product";

    public static final String PNC_PRODUCT_VERSION_1 = "1.0";

    public static final String PNC_PRODUCT_VERSION_2 = "2.0";

    public static final String PNC_PRODUCT_RELEASE = "1.0.0.GA";

    public static final String PNC_PRODUCT_MILESTONE1 = "1.0.0.Build1";

    public static final String PNC_PRODUCT_MILESTONE2 = "1.0.0.Build2";

    public static final String PNC_PRODUCT_MILESTONE3 = "1.0.0.Build3";

    public static final String PNC_PRODUCT_MILESTONE4 = "1.0.0.Build4";

    public static final String PNC_PROJECT_1_NAME = "Project Newcastle Demo Project 1";

    public static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private ArtifactAuditedRepository artifactAuditedRepository;

    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private ProductVersionRepository productVersionRepository;

    @Inject
    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    private ProductMilestoneReleaseRepository productMilestoneReleaseRepository;

    @Inject
    private ProductReleaseRepository productReleaseRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private BuildEnvironmentRepository environmentRepository;

    @Inject
    private SequenceHandlerRepository sequenceHandlerRepository;

    @Inject
    private DeliverableAnalyzerOperationRepository deliverableAnalyzerOperationRepository;

    @Inject
    private Datastore datastore;

    @Inject
    DemoDataConfig demoDataConfig;

    @Inject
    SystemConfig systemConfig;

    BuildConfiguration buildConfiguration1;

    BuildConfiguration buildConfiguration2;

    BuildConfigurationSet buildConfigurationSet1;

    ProductMilestone demoProductMilestone1;

    User demoUser;

    User pncAdminUser;

    final int DAYS_IN_A_WEEK = 7;
    final Date TODAY = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    final Date ONE_WEEK_BEFORE_TODAY = Date
            .from(LocalDateTime.now().minusDays(DAYS_IN_A_WEEK).atZone(ZoneId.systemDefault()).toInstant());
    final Date ONE_WEEK_AFTER_TODAY = Date
            .from(LocalDateTime.now().plusDays(DAYS_IN_A_WEEK).atZone(ZoneId.systemDefault()).toInstant());

    public void verifyData() {
        // Check number of entities in DB
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions
                .checkState(buildConfigurationRepository.count() > 0, "Expecting number of BuildConfigurations > 0");
        Preconditions.checkState(productVersionRepository.count() > 0, "Expecting number of ProductVersions > 0");
        Preconditions
                .checkState(buildConfigurationSetRepository.count() > 0, "Expecting number of BuildRepositorySets > 0");
        Preconditions.checkState(artifactRepository.count() > 0, "Expecting number of Artifacts > 0");

        BuildConfiguration buildConfigurationDB = buildConfigurationRepository.queryAll().get(0);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        BuildConfigurationSet buildConfigurationSet = buildConfigurationDB.getBuildConfigurationSets()
                .iterator()
                .next();
        Preconditions.checkState(
                buildConfigurationSet.getProductVersion() != null,
                "Product version of buildConfiguration must be not null");

        BuildConfigurationSet buildConfigurationSetDB = buildConfigurationSetRepository.queryAll().get(0);

        Preconditions.checkState(
                buildConfigurationSetDB.getProductVersion() != null,
                "Product version of buildConfigurationSet must be not null");

        // Check that mapping between Product and Build Configuration via BuildConfigurationSet is correct
        Preconditions.checkState(
                buildConfigurationSetDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to Project must be " + PNC_PRODUCT_NAME);
        Preconditions.checkState(
                buildConfigurationSetDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION_1),
                "Product version mapped to Project must be " + PNC_PRODUCT_VERSION_1);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(
                buildConfigurationSet.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION_1),
                "Product version mapped to BuildConfiguration must be " + PNC_PRODUCT_VERSION_1);
        Preconditions.checkState(
                buildConfigurationSet.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to BuildConfiguration must be " + PNC_PRODUCT_NAME);

        // Check data of BuildConfiguration
        Preconditions.checkState(
                buildConfigurationDB.getProject().getName().equals(PNC_PROJECT_1_NAME),
                "Project mapped to BuildConfiguration must be " + PNC_PROJECT_1_NAME);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeProjectProductData() {

        BuildEnvironment environment1Unsaved = BuildEnvironment.Builder.newBuilder()
                .name("Demo Environment 1")
                .description("Basic Java and Maven Environment")
                .attribute("JDK", "1.7.0")
                .attribute("OS", "Linux")
                .systemImageId("12345678")
                .systemImageRepositoryUrl("my.registry/newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .deprecated(false)
                .build();
        BuildEnvironment environment1 = environmentRepository.save(environment1Unsaved);

        BuildEnvironment environment2Unsaved = BuildEnvironment.Builder.newBuilder()
                .name("Demo Environment 2")
                .description("Basic Java and Maven Environment")
                .attribute("JDK", "1.7.0")
                .attribute("OS", "Linux")
                .systemImageId("12345679")
                .systemImageRepositoryUrl("my.registry/newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .deprecated(true)
                .build();
        BuildEnvironment environment2 = environmentRepository.save(environment2Unsaved);

        /*
         * All the bi-directional mapping settings are managed inside the Builders
         */
        // Example product and product version
        Product product = Product.Builder.newBuilder()
                .name(PNC_PRODUCT_NAME)
                .abbreviation("PNC")
                .description("Example Product for Project Newcastle Demo")
                .build();
        product = productRepository.save(product);

        // Example product version, release, and milestone of the product
        ProductVersion productVersion1 = ProductVersion.Builder.newBuilder()
                .version(PNC_PRODUCT_VERSION_1)
                .product(product)
                .generateBrewTagPrefix(
                        product.getAbbreviation(),
                        PNC_PRODUCT_VERSION_1,
                        systemConfig.getBrewTagPattern())
                .build();
        productVersion1 = productVersionRepository.save(productVersion1);

        ProductVersion productVersion2 = ProductVersion.Builder.newBuilder()
                .version(PNC_PRODUCT_VERSION_2)
                .product(product)
                .generateBrewTagPrefix(
                        product.getAbbreviation(),
                        PNC_PRODUCT_VERSION_2,
                        systemConfig.getBrewTagPattern())
                .build();
        productVersion2 = productVersionRepository.save(productVersion2);

        demoProductMilestone1 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE1)
                .startingDate(ONE_WEEK_BEFORE_TODAY)
                .plannedEndDate(TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone1 = productMilestoneRepository.save(demoProductMilestone1);

        ProductMilestone demoProductMilestone2 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE2)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone2 = productMilestoneRepository.save(demoProductMilestone2);

        Instant t0 = TODAY.toInstant();
        Instant successTime = t0.plus(10, ChronoUnit.MINUTES);

        ProductMilestone demoProductMilestone3 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE3)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .endDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone3 = productMilestoneRepository.save(demoProductMilestone3);

        ProductMilestone demoProductMilestone4 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE4)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .endDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone4 = productMilestoneRepository.save(demoProductMilestone4);

        ProductMilestoneRelease milestoneRelease1 = new ProductMilestoneRelease();
        milestoneRelease1.setId(Sequence.nextId());
        milestoneRelease1.setMilestone(demoProductMilestone3);
        // first store with latter starting date to test sort function
        milestoneRelease1.setStartingDate(Date.from(t0.plus(2, ChronoUnit.MINUTES)));
        milestoneRelease1.setStatus(MilestoneCloseStatus.SYSTEM_ERROR);
        productMilestoneReleaseRepository.save(milestoneRelease1);

        ProductMilestoneRelease milestoneRelease2 = new ProductMilestoneRelease();
        milestoneRelease2.setId(Sequence.nextId());
        milestoneRelease2.setMilestone(demoProductMilestone3);
        milestoneRelease2.setStartingDate(Date.from(t0));
        milestoneRelease2.setStatus(MilestoneCloseStatus.FAILED);
        productMilestoneReleaseRepository.save(milestoneRelease2);

        ProductMilestoneRelease milestoneRelease3 = new ProductMilestoneRelease();
        milestoneRelease3.setId(Sequence.nextId());
        milestoneRelease3.setMilestone(demoProductMilestone3);
        milestoneRelease3.setStartingDate(Date.from(successTime));
        milestoneRelease3.setStatus(MilestoneCloseStatus.SUCCEEDED);
        productMilestoneReleaseRepository.save(milestoneRelease3);

        ProductRelease productRelease = ProductRelease.Builder.newBuilder()
                .version(PNC_PRODUCT_RELEASE)
                .productMilestone(demoProductMilestone1)
                .supportLevel(SupportLevel.EARLYACCESS)
                .build();
        productRelease = productReleaseRepository.save(productRelease);

        productVersion1.setCurrentProductMilestone(demoProductMilestone3);
        productVersion1 = productVersionRepository.save(productVersion1);

        // Example projects
        Project project1 = Project.Builder.newBuilder()
                .name(PNC_PROJECT_1_NAME)
                .description("Example Project for Newcastle Demo")
                .projectUrl("https://github.com/project-ncl/pnc")
                .build();
        Project project2 = Project.Builder.newBuilder()
                .name("Causeway")
                .description("Causeway - Koji integration")
                .projectUrl("https://github.com/project-ncl/causeway")
                .build();
        Project project3 = Project.Builder.newBuilder()
                .name("Pnc Build Agent")
                .description("Pnc Build Agent - remote client to execute commands.")
                .projectUrl("https://github.com/project-ncl/pnc-build-agent")
                .build();
        Project project4 = Project.Builder.newBuilder()
                .name("Dependency Analysis")
                .description("Dependency Analysis - Analise project dependencies.")
                .projectUrl("https://github.com/project-ncl/dependency-analysis")
                .build();
        Project project5 = Project.Builder.newBuilder()
                .name("termd")
                .description("Remote shell.")
                .projectUrl("https://github.com/project-ncl/termd")
                .build();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);

        RepositoryConfiguration repositoryConfiguration1 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(0),
                "https://github.com/project-ncl/pnc.git");
        RepositoryConfiguration repositoryConfiguration2 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(1),
                null);
        RepositoryConfiguration repositoryConfiguration3 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(2),
                null);
        RepositoryConfiguration repositoryConfiguration4 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(3),
                null);
        RepositoryConfiguration repositoryConfiguration5 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(4),
                null);

        repositoryConfigurationRepository.save(repositoryConfiguration1);
        repositoryConfigurationRepository.save(repositoryConfiguration2);
        repositoryConfigurationRepository.save(repositoryConfiguration3);
        repositoryConfigurationRepository.save(repositoryConfiguration4);
        repositoryConfigurationRepository.save(repositoryConfiguration5);

        // Example build configurations
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put("KEY", "VALUE");
        buildConfiguration1 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name(PNC_PROJECT_BUILD_CFG_ID)
                .project(project1)
                .description("Test build config for project newcastle")
                .buildType(BuildType.MVN)
                .buildEnvironment(environment1)
                .buildScript("mvn deploy -DskipTests=true")
                .repositoryConfiguration(repositoryConfiguration1)
                .productVersion(productVersion1)
                .scmRevision("*/v0.2")
                .genericParameters(genericParameters)
                .build();
        buildConfiguration1 = buildConfigurationRepository.save(buildConfiguration1);

        buildConfiguration2 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name("termd")
                .project(project2)
                .buildType(BuildType.MVN)
                .description("Test configueration for Termd.")
                .buildEnvironment(environment1)
                .buildScript("mvn deploy -DskipTests=true")
                .productVersion(productVersion1)
                .repositoryConfiguration(repositoryConfiguration2)
                .scmRevision("master")
                .build();
        buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

        BuildConfiguration buildConfiguration3 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name("pnc-build-agent-0.4")
                .project(project3)
                .description("Test config for Pnc Build Agent.")
                .buildType(BuildType.MVN)
                .buildEnvironment(environment1)
                .buildScript("mvn deploy -DskipTests=true")
                .productVersion(productVersion2)
                .repositoryConfiguration(repositoryConfiguration3)
                .dependency(buildConfiguration2)
                .build();
        buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

        BuildConfiguration buildConfiguration4 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name("dependency-analysis-1.3")
                .project(project4)
                .description("Test config for Dependency Analysis.")
                .buildType(BuildType.MVN)
                .buildEnvironment(environment1)
                .buildScript("mvn deploy -DskipTests=true")
                .repositoryConfiguration(repositoryConfiguration4)
                .dependency(buildConfiguration1)
                .build();
        buildConfiguration4 = buildConfigurationRepository.save(buildConfiguration4);

        BuildConfiguration buildConfiguration5 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name("maven-plugin-test")
                .project(project5)
                .description("Test build for Plugins with external downloads")
                .buildType(BuildType.MVN)
                .buildEnvironment(environment1)
                .buildScript("mvn clean deploy")
                .repositoryConfiguration(repositoryConfiguration5)
                .build();
        buildConfiguration5 = buildConfigurationRepository.save(buildConfiguration5);

        // Build config set containing the three example build configs
        buildConfigurationSet1 = BuildConfigurationSet.Builder.newBuilder()
                .name("Example-Build-Group-1")
                .buildConfiguration(buildConfiguration1)
                .buildConfiguration(buildConfiguration2)
                .buildConfiguration(buildConfiguration3)
                .productVersion(productVersion1)
                .build();

        BuildConfigurationSet buildConfigurationSet2 = BuildConfigurationSet.Builder.newBuilder()
                .name("Fabric-Build-Group")
                .buildConfiguration(buildConfiguration4)
                .productVersion(productVersion1)
                .build();

        demoUser = User.Builder.newBuilder()
                .username("demo-user")
                .firstName("Demo First Name")
                .lastName("Demo Last Name")
                .email("demo-user@pnc.com")
                .build();

        pncAdminUser = User.Builder.newBuilder()
                .username("pnc-admin")
                .firstName("pnc-admin")
                .lastName("pnc-admin")
                .email("pnc-admin@pnc.com")
                .build();

        buildConfigurationSetRepository.save(buildConfigurationSet1);
        buildConfigurationSetRepository.save(buildConfigurationSet2);
        demoUser = userRepository.save(demoUser);
        pncAdminUser = userRepository.save(pncAdminUser);

    }

    /**
     * Build record needs to be initialized in a separate transaction so that the audited build configuration can be
     * set.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeBuildRecordDemoData() {

        TargetRepository targetRepository = TargetRepository.newBuilder()
                .repositoryType(RepositoryType.MAVEN)
                .repositoryPath("builds-untested")
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .temporaryRepo(false)
                .build();

        TargetRepository targetRepository2 = TargetRepository.newBuilder()
                .repositoryType(RepositoryType.NPM)
                .repositoryPath("builds-tested")
                .identifier(ReposiotryIdentifier.INDY_NPM)
                .temporaryRepo(true)
                .build();

        targetRepositoryRepository.save(targetRepository);
        targetRepositoryRepository.save(targetRepository2);

        Artifact builtArtifact1 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact1:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 1")
                .md5("4af310bf0ef67bc7d143f35818ea1ed2")
                .sha1("3a8ff25c890f2a4a283876a91037ff6c57474a14")
                .sha256("1660168483cb8a05d1cc2e77c861682a42ed9517ba945159d5538950c5db00fa")
                .deployPath("demo/built-artifact1/1.0/built-artifact1-1.0.jar")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .buildCategory(BuildCategory.STANDARD)
                .build();
        Artifact builtArtifact2 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact2:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 2")
                .md5("4af310bf0ef67bc7d143f35818ea1ed2")
                .sha1("61dad16e14438d2d8c8cbd18b267d62944f37898")
                .sha256("2fafc2ed0f752ac2540283d48c5cd663254a853c5cb13dec02dce023fc7471a9")
                .deployPath("demo/built-artifact2/1.0/built-artifact2-1.0.jar")
                .size(11L)
                .artifactQuality(ArtifactQuality.VERIFIED)
                .buildCategory(BuildCategory.STANDARD)
                .build();
        Artifact builtArtifact3 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact11:pom:1.0")
                .targetRepository(targetRepository2)
                .filename("demo built artifact 11")
                .md5("5c8e1503e77dc8e370610098e01f0a8e")
                .sha1("550748f6f58ed8d4f6b63850a867ac207da30013")
                .sha256("b39f88c9937f201981767e539025121971e72bc590ea20ed7fdfffafc05f55a9")
                .deployPath("demo/built-artifact11/1.0/built-artifact11-1.0.pom")
                .size(10L)
                .artifactQuality(ArtifactQuality.DELETED)
                .buildCategory(BuildCategory.SERVICE)
                .build();
        Artifact builtArtifact4 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact22:jar:1.0")
                .targetRepository(targetRepository2)
                .filename("demo built artifact 21")
                .md5("48312fb24c7b2a116c2139d5b39bad66")
                .sha1("6ce2fd75c35e7eed2c45338b943be34d0b974f16")
                .sha256("61c9ccd3ba0013311ddb89cb9a29389b6761061bdcdfb48f0096bf98c7279a21")
                .deployPath("demo/built-artifact22/1.0/built-artifact22-1.0.jar")
                .size(11L)
                .artifactQuality(ArtifactQuality.NEW)
                .buildCategory(BuildCategory.SERVICE)
                .build();

        builtArtifact1 = artifactRepository.save(builtArtifact1);
        builtArtifact2 = artifactRepository.save(builtArtifact2);
        builtArtifact3 = artifactRepository.save(builtArtifact3);
        builtArtifact4 = artifactRepository.save(builtArtifact4);

        Artifact importedArtifact1 = Artifact.Builder.newBuilder()
                .identifier("demo:imported-artifact1:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo imported artifact 1")
                .originUrl("http://central/import1.jar")
                .importDate(Date.from(Instant.now()))
                .md5("md-fake-abcd1234")
                .sha1("sha1-fake-abcd1234")
                .sha256("sha256-fake-abcd1234")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .buildCategory(BuildCategory.STANDARD)
                .deployPath("/imported1")
                .build();
        Artifact importedArtifact2 = Artifact.Builder.newBuilder()
                .identifier("demo:imported-artifact2:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo imported artifact 2")
                .originUrl("http://central/import2.jar")
                .importDate(Date.from(Instant.now()))
                .md5("md-fake-abcd1234")
                .sha1("sha1-fake-abcd1234")
                .sha256("sha256-fake-abcd1234")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .buildCategory(BuildCategory.STANDARD)
                .deployPath("/imported2")
                .build();

        importedArtifact1 = artifactRepository.save(importedArtifact1);
        importedArtifact2 = artifactRepository.save(importedArtifact2);

        Set<BuildRecord> buildRecords = new HashSet<>();

        final int INITIAL_REVISION = 1;
        IdRev buildConfig1AuditIdRev = new IdRev(buildConfiguration1.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited1 = buildConfigurationAuditedRepository
                .queryById(buildConfig1AuditIdRev);
        if (buildConfigAudited1 != null) {

            String nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfigAudited1)
                    .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                    .startTime(Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                    .endTime(Timestamp.from(Instant.now()))
                    .dependency(importedArtifact1)
                    .dependency(importedArtifact2)
                    .user(pncAdminUser)
                    .repourLog("This is a wannabe alignment log.")
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog. 📦")
                    .status(BuildStatus.SUCCESS)
                    .productMilestone(demoProductMilestone1)
                    .sshCommand("ssh worker@localhost -P 9999")
                    .sshPassword("dontchangeme")
                    .buildEnvironment(buildConfigAudited1.getBuildEnvironment())
                    .scmRepoURL(buildConfigAudited1.getRepositoryConfiguration().getInternalUrl())
                    .scmRevision(buildConfigAudited1.getScmRevision())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.2.3")
                    .temporaryBuild(false)
                    .build();

            log.info("Saving buildRecord1: " + buildRecord1);
            BuildRecord savedBuildRecord1 = buildRecordRepository.save(buildRecord1);
            builtArtifact1.setBuildRecord(savedBuildRecord1);
            builtArtifact2.setBuildRecord(savedBuildRecord1);

            log.info(
                    "Saved buildRecord1: " + savedBuildRecord1 + "BuildConfigurationAuditedIdRev: "
                            + savedBuildRecord1.getBuildConfigurationAuditedIdRev());
            buildRecords.add(buildRecord1);

            nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord tempRecord1 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfigAudited1)
                    .submitTime(Timestamp.from(Instant.now()))
                    .startTime(Timestamp.from(Instant.now()))
                    .endTime(Timestamp.from(Instant.now()))
                    .user(pncAdminUser)
                    .repourLog("This is a wannabe alignment log.")
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfigAudited1.getBuildEnvironment())
                    .scmRepoURL(buildConfigAudited1.getRepositoryConfiguration().getInternalUrl())
                    .scmRevision(buildConfigAudited1.getScmRevision())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.2.3")
                    .temporaryBuild(true)
                    .build();

            log.info("Saving tempRecord1: " + tempRecord1);
            BuildRecord savedTempRecord1 = buildRecordRepository.save(tempRecord1);
            builtArtifact3.setBuildRecord(savedTempRecord1);
            builtArtifact4.setBuildRecord(savedTempRecord1);
            log.info(
                    "Saved buildRecord1: " + savedTempRecord1 + "BuildConfigurationAuditedIdRev: "
                            + savedTempRecord1.getBuildConfigurationAuditedIdRev());
            buildRecords.add(tempRecord1);

        }

        Artifact builtArtifact5 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact3:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 3")
                .md5("17353a18678c6c249e3052edec2e4c5c")
                .sha1("61dad16e14438d2d8c8cbd18b267d62944f37898")
                .sha256("1660168483cb8a05d1cc2e77c861682a42ed9517ba945159d5538950c5db00fa")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built3")
                .build();
        Artifact builtArtifact6 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact4:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 4")
                .md5("md-fake-abcd1234")
                .sha1("sha1-fake-abcd1234")
                .sha256("sha256-fake-abcd1234")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built4")
                .build();

        Artifact builtArtifact7 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact5:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 7")
                .md5("adsfs6df548w1327cx78he873217df98")
                .sha1("a56asdf87a3cvx231b87987fasd6f5ads4f32sdf")
                .sha256("sad5f64sf87b3cvx2b1v87tr89h7d3f5g432xcz1zv87fawrv23n8796534564er")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built5")
                .build();
        Artifact builtArtifact8 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact6:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 8")
                .md5("md-fake-abcdefg1234")
                .sha1("sha1-fake-abcdefg1234")
                .sha256("sha256-fake-abcdefg1234")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built6")
                .build();

        builtArtifact5 = artifactRepository.save(builtArtifact5);
        builtArtifact6 = artifactRepository.save(builtArtifact6);
        builtArtifact7 = artifactRepository.save(builtArtifact7);
        builtArtifact8 = artifactRepository.save(builtArtifact8);

        Artifact dependencyBuiltArtifact1 = artifactRepository
                .queryByPredicates(withIdentifierAndSha256(builtArtifact1.getIdentifier(), builtArtifact1.getSha256()));

        // For timestamp tests where concrete timestamp is needed
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.JANUARY, 10);
        IdRev buildConfig2AuditIdRev = new IdRev(buildConfiguration2.getId(), INITIAL_REVISION);
        BuildConfigurationAudited buildConfigAudited2 = buildConfigurationAuditedRepository
                .queryById(buildConfig2AuditIdRev);
        if (buildConfigAudited2 != null) {

            String nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfigAudited2)
                    .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                    .startTime(Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                    .endTime(Timestamp.from(Instant.now()))
                    .dependency(dependencyBuiltArtifact1)
                    .dependency(importedArtifact1)
                    .user(demoUser)
                    .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfigAudited2.getBuildEnvironment())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.2.4")
                    .temporaryBuild(false)
                    .build();

            nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord savedBuildRecord2 = buildRecordRepository.save(buildRecord2);
            builtArtifact5.setBuildRecord(savedBuildRecord2);
            builtArtifact6.setBuildRecord(savedBuildRecord2);
            buildRecords.add(buildRecord2);

            BuildRecord tempRecord1 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfigAudited2)
                    .submitTime(Timestamp.from(calendar.toInstant().minus(8, ChronoUnit.HOURS)))
                    .startTime(Timestamp.from(calendar.toInstant().minus(5, ChronoUnit.HOURS)))
                    .endTime(Timestamp.from(calendar.toInstant()))
                    .user(demoUser)
                    .buildLog("Is it free?")
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfigAudited2.getBuildEnvironment())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.2.4")
                    .temporaryBuild(true)
                    .build();

            BuildRecord savedTempRecord1 = buildRecordRepository.save(tempRecord1);

            builtArtifact7.setBuildRecord(savedTempRecord1);
            builtArtifact8.setBuildRecord(savedTempRecord1);
            buildRecords.add(tempRecord1);
        }

        BuildConfigSetRecord buildConfigSetRecord1 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .startTime(Timestamp.from(Instant.now()))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.FAILED)
                .temporaryBuild(false)
                .build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord1);

        BuildConfigSetRecord buildConfigSetRecord2 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .buildRecords(buildRecords)
                .startTime(Timestamp.from(Instant.now()))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .temporaryBuild(false)
                .build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord2);

        // update owning side of build record to link with build config set record
        buildRecords.forEach(record -> buildRecordRepository.save(record));

        BuildConfigSetRecord buildConfigSetRecord3 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .startTime(Timestamp.from(calendar.toInstant().minus(20, ChronoUnit.DAYS)))
                .endTime(Timestamp.from(calendar.toInstant().minus(20, ChronoUnit.DAYS)))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .temporaryBuild(true)
                .build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord3);

        demoProductMilestone1 = productMilestoneRepository.queryById(demoProductMilestone1.getId());
        demoProductMilestone1.addDeliveredArtifact(builtArtifact1);
        demoProductMilestone1.addDeliveredArtifact(builtArtifact5);
        demoProductMilestone1.addDeliveredArtifact(importedArtifact2);
        demoProductMilestone1 = productMilestoneRepository.save(demoProductMilestone1);

        Map<String, String> operationParameters = new HashMap<>();
        operationParameters.put("url-0", "https://github.com/project-ncl/pnc/archive/refs/tags/2.1.1.tar.gz");
        DeliverableAnalyzerOperation operation1 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000001l))
                .progressStatus(ProgressStatus.NEW)
                .submitTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .operationParameters(operationParameters)
                .build();
        deliverableAnalyzerOperationRepository.save(operation1);

        DeliverableAnalyzerOperation operation2 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000002l))
                .progressStatus(ProgressStatus.IN_PROGRESS)
                .submitTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .build();
        deliverableAnalyzerOperationRepository.save(operation2);
    }

    private RepositoryConfiguration createRepositoryConfiguration(String internalScmUrl, String externalUrl) {
        return RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(internalScmUrl)
                .externalUrl(externalUrl)
                .build();
    }

}
