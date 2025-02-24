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
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.enums.SupportLevel;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
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
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static final String PNC_PRODUCT_MILESTONE5 = "2.0.0.Build1";

    public static final String PNC_PROJECT_1_NAME = "Project Newcastle Demo Project 1";

    public static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    public static final String EAP_PRODUCT_NAME = "JBoss EAP Demo Product";

    public static final String EAP_PRODUCT_VERSION = "7.0";

    public static final String EAP_PRODUCT_MILESTONE_1 = "7.0.0.Build1";

    public static final String EAP_PRODUCT_MILESTONE_2 = "7.0.0.Build2";

    public static final String EAP_PROJECT_NAME = "JBoss EAP Demo Project";

    public static final String EAP_PROJECT_BUILD_CFG_ID = "eap-7.0.0.CR1";

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
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    @Inject
    private DeliverableArtifactRepository deliverableArtifactRepository;

    @Inject
    private DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;

    @Inject
    private Datastore datastore;

    @Inject
    DemoDataConfig demoDataConfig;

    @Inject
    SystemConfig systemConfig;

    @Inject
    BuildConfigurationAuditedHelper helper;

    BuildConfiguration buildConfiguration1;

    BuildConfiguration buildConfiguration2;

    BuildConfiguration buildConfiguration6;

    BuildConfigurationSet buildConfigurationSet1;

    ProductMilestone demoProductMilestone1;

    ProductMilestone demoProductMilestone2;

    ProductMilestone demoProductMilestone3;

    ProductMilestone demoProductMilestone5;

    ProductMilestone demoProductMilestone6;

    ProductMilestone demoProductMilestone7;

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
        Product product1 = Product.Builder.newBuilder()
                .name(PNC_PRODUCT_NAME)
                .abbreviation("PNC")
                .description("Example Product for Project Newcastle Demo")
                .build();
        product1 = productRepository.save(product1);

        Product product2 = Product.Builder.newBuilder()
                .name(EAP_PRODUCT_NAME)
                .abbreviation("EAP")
                .description("Example Product for JBoss EAP Demo")
                .build();
        product2 = productRepository.save(product2);

        // Example product version, release, and milestone of the product
        ProductVersion productVersion1 = ProductVersion.Builder.newBuilder()
                .version(PNC_PRODUCT_VERSION_1)
                .product(product1)
                .generateBrewTagPrefix(
                        product1.getAbbreviation(),
                        PNC_PRODUCT_VERSION_1,
                        systemConfig.getBrewTagPattern())
                .build();
        productVersion1 = productVersionRepository.save(productVersion1);

        ProductVersion productVersion2 = ProductVersion.Builder.newBuilder()
                .version(PNC_PRODUCT_VERSION_2)
                .product(product1)
                .generateBrewTagPrefix(
                        product1.getAbbreviation(),
                        PNC_PRODUCT_VERSION_2,
                        systemConfig.getBrewTagPattern())
                .build();
        productVersion2 = productVersionRepository.save(productVersion2);

        ProductVersion productVersion3 = ProductVersion.Builder.newBuilder()
                .version(EAP_PRODUCT_VERSION)
                .product(product2)
                .generateBrewTagPrefix(
                        product2.getAbbreviation(),
                        EAP_PRODUCT_VERSION,
                        systemConfig.getBrewTagPattern())
                .build();
        productVersion3 = productVersionRepository.save(productVersion3);

        demoProductMilestone1 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE1)
                .startingDate(ONE_WEEK_BEFORE_TODAY)
                .plannedEndDate(TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone1 = productMilestoneRepository.save(demoProductMilestone1);

        demoProductMilestone2 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE2)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion1)
                .build();
        demoProductMilestone2 = productMilestoneRepository.save(demoProductMilestone2);

        Instant t0 = TODAY.toInstant();
        Instant successTime = t0.plus(10, ChronoUnit.MINUTES);

        demoProductMilestone3 = ProductMilestone.Builder.newBuilder()
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

        demoProductMilestone5 = ProductMilestone.Builder.newBuilder()
                .version(EAP_PRODUCT_MILESTONE_1)
                .startingDate(ONE_WEEK_BEFORE_TODAY)
                .plannedEndDate(TODAY)
                .endDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion3)
                .build();
        demoProductMilestone5 = productMilestoneRepository.save(demoProductMilestone5);

        demoProductMilestone6 = ProductMilestone.Builder.newBuilder()
                .version(EAP_PRODUCT_MILESTONE_2)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion3)
                .build();
        demoProductMilestone6 = productMilestoneRepository.save(demoProductMilestone6);

        demoProductMilestone7 = ProductMilestone.Builder.newBuilder()
                .version(PNC_PRODUCT_MILESTONE5)
                .startingDate(TODAY)
                .plannedEndDate(ONE_WEEK_AFTER_TODAY)
                .productVersion(productVersion2)
                .build();
        demoProductMilestone7 = productMilestoneRepository.save(demoProductMilestone7);

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
        Project project6 = Project.Builder.newBuilder()
                .name(EAP_PROJECT_NAME)
                .description("Example Project for JBoss EAP Demo")
                .projectUrl("https://github.com/jboss/eap")
                .build();

        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);
        projectRepository.save(project6);

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
        RepositoryConfiguration repositoryConfiguration6 = createRepositoryConfiguration(
                demoDataConfig.getInternalRepo(5),
                "https://github.com/jboss/eap.git");

        repositoryConfigurationRepository.save(repositoryConfiguration1);
        repositoryConfigurationRepository.save(repositoryConfiguration2);
        repositoryConfigurationRepository.save(repositoryConfiguration3);
        repositoryConfigurationRepository.save(repositoryConfiguration4);
        repositoryConfigurationRepository.save(repositoryConfiguration5);
        repositoryConfigurationRepository.save(repositoryConfiguration6);

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

        buildConfiguration6 = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .name(EAP_PROJECT_BUILD_CFG_ID)
                .project(project6)
                .description("Test build config for project eap")
                .buildType(BuildType.MVN)
                .buildEnvironment(environment1)
                .buildScript("mvn clean deploy")
                .repositoryConfiguration(repositoryConfiguration6)
                .build();
        buildConfiguration6 = buildConfigurationRepository.save(buildConfiguration6);

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

    @TransactionAttribute
    public void updateBuildConfigurations() {
        buildConfiguration2.setBuildScript("mvn deploy -DskipTests");
        helper.save(buildConfiguration2);

        buildConfiguration6.setBuildScript(buildConfiguration6.getBuildScript() + " -DskipTests");
        helper.save(buildConfiguration6);

        buildConfiguration1.setBuildScript("mvn clean install -DskipTests=true");
        helper.save(buildConfiguration1);

        buildConfiguration1.setBuildScript("mvn clean install");
        helper.save(buildConfiguration1);
    }

    /**
     * Build record needs to be initialized in a separate transaction so that the audited build configuration can be
     * set.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeBuildRecordDemoData() {
        final int INITIAL_REVISION = 1;
        final int SECOND_REVISION = 2;
        final int THIRD_REVISION = 3;

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
        // builtArtifact9 defined in this place in order to be part of savedBuildRecord1
        Artifact builtArtifact9 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact7:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 9")
                .md5("md-fake-abcdefg4321")
                .sha1("sha1-fake-abcdefg4321")
                .sha256("sha256-fake-abcdefg4321")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built7")
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
                .artifactQuality(ArtifactQuality.IMPORTED)
                .buildCategory(BuildCategory.STANDARD)
                .deployPath("/imported2")
                .build();

        importedArtifact1 = artifactRepository.save(importedArtifact1);
        importedArtifact2 = artifactRepository.save(importedArtifact2);

        Set<BuildRecord> buildRecords = new HashSet<>();

        List<BuildConfigurationAudited> buildConfig1Revisions = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(buildConfiguration1.getId());
        int buildConfig1RevisionsCount = buildConfig1Revisions.size();

        BuildConfigurationAudited buildConfigAudited1 = buildConfig1Revisions
                .get(buildConfig1RevisionsCount - INITIAL_REVISION);
        Objects.requireNonNull(buildConfigAudited1, "couldn't get buildConfigAudited1");

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
                .attribute("FOO", "bar")
                .build();

        log.info("Saving buildRecord1: " + buildRecord1);
        BuildRecord savedBuildRecord1 = buildRecordRepository.save(buildRecord1);
        builtArtifact1.setBuildRecord(savedBuildRecord1);
        builtArtifact2.setBuildRecord(savedBuildRecord1);
        builtArtifact9.setBuildRecord(savedBuildRecord1);

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
        Artifact builtArtifact10 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact7:jar:1.1")
                .targetRepository(targetRepository)
                .filename("demo built artifact 10")
                .md5("md5-fake-abc123")
                .sha1("sha1-fake-abc123")
                .sha256("sha256-fake-abc123")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built7")
                .build();

        builtArtifact5 = artifactRepository.save(builtArtifact5);
        builtArtifact6 = artifactRepository.save(builtArtifact6);
        builtArtifact7 = artifactRepository.save(builtArtifact7);
        builtArtifact8 = artifactRepository.save(builtArtifact8);
        builtArtifact9 = artifactRepository.save(builtArtifact9);
        builtArtifact10 = artifactRepository.save(builtArtifact10);

        Artifact dependencyBuiltArtifact1 = artifactRepository
                .queryByPredicates(withIdentifierAndSha256(builtArtifact1.getIdentifier(), builtArtifact1.getSha256()));

        // For timestamp tests where concrete timestamp is needed
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, Calendar.JANUARY, 10);

        List<BuildConfigurationAudited> buildConfig2Revisions = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(buildConfiguration2.getId());
        int buildConfig2RevisionsCount = buildConfig2Revisions.size();

        BuildConfigurationAudited buildConfig2InitialAudit = buildConfig2Revisions
                .get(buildConfig2RevisionsCount - INITIAL_REVISION);
        BuildConfigurationAudited buildConfig2SecondAudit = buildConfig2Revisions
                .get(buildConfig2RevisionsCount - SECOND_REVISION);

        if (buildConfig2InitialAudit != null && buildConfig2SecondAudit != null) {

            nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfig2InitialAudit)
                    .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                    .startTime(Timestamp.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                    .endTime(Timestamp.from(Instant.now()))
                    .dependency(dependencyBuiltArtifact1)
                    .dependency(importedArtifact1)
                    .user(demoUser)
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfig2InitialAudit.getBuildEnvironment())
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

            BuildRecord tempRecord2 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfig2InitialAudit)
                    .submitTime(Timestamp.from(calendar.toInstant().minus(8, ChronoUnit.HOURS)))
                    .startTime(Timestamp.from(calendar.toInstant().minus(5, ChronoUnit.HOURS)))
                    .endTime(Timestamp.from(calendar.toInstant()))
                    .user(demoUser)
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfig2InitialAudit.getBuildEnvironment())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.2.4")
                    .temporaryBuild(true)
                    .dependency(builtArtifact3)
                    .dependency(builtArtifact7)
                    .build();

            BuildRecord savedTempRecord2 = buildRecordRepository.save(tempRecord2);
            builtArtifact7.setBuildRecord(savedTempRecord2);
            builtArtifact8.setBuildRecord(savedTempRecord2);
            buildRecords.add(tempRecord2);

            nextId = Sequence.nextBase32Id();
            log.info("####nextId: " + nextId);

            BuildRecord buildRecord3 = BuildRecord.Builder.newBuilder()
                    .id(nextId)
                    .buildConfigurationAudited(buildConfig2SecondAudit)
                    .productMilestone(demoProductMilestone2)
                    .submitTime(ONE_WEEK_BEFORE_TODAY)
                    .startTime(Timestamp.from(calendar.toInstant().minus(1, ChronoUnit.HOURS)))
                    .endTime(Timestamp.from(calendar.toInstant()))
                    .user(demoUser)
                    .status(BuildStatus.SUCCESS)
                    .buildEnvironment(buildConfig2InitialAudit.getBuildEnvironment())
                    .executionRootName("org.jboss.pnc:parent")
                    .executionRootVersion("1.4.2")
                    .temporaryBuild(false)
                    .build();

            BuildRecord savedBuildRecord3 = buildRecordRepository.save(buildRecord3);
            builtArtifact10.setBuildRecord(savedBuildRecord3);
        }

        List<BuildConfigurationAudited> buildConfig6Revisions = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(buildConfiguration6.getId());
        int buildConfig6RevisionsCount = buildConfig6Revisions.size();

        BuildConfigurationAudited buildConfig6InitialAudit = buildConfig6Revisions
                .get(buildConfig6RevisionsCount - INITIAL_REVISION);
        BuildConfigurationAudited buildConfig6SecondAudit = buildConfig6Revisions
                .get(buildConfig6RevisionsCount - SECOND_REVISION);

        BuildConfigurationAudited buildConfig1SecondAudit = buildConfig1Revisions
                .get(buildConfig1RevisionsCount - SECOND_REVISION);
        BuildConfigurationAudited buildConfig1ThirdAudit = buildConfig1Revisions
                .get(buildConfig1RevisionsCount - THIRD_REVISION);

        nextId = Sequence.nextBase32Id();
        log.info("####nextId: " + nextId);

        BuildRecord buildRecord4 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .buildConfigurationAudited(buildConfig6InitialAudit)
                .productMilestone(demoProductMilestone5)
                .submitTime(Timestamp.from(Instant.now().minus(8, ChronoUnit.MINUTES)))
                .startTime(Timestamp.from(Instant.now().minus(7, ChronoUnit.MINUTES)))
                .endTime(Timestamp.from(Instant.now().minus(6, ChronoUnit.MINUTES)))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfig6InitialAudit.getBuildEnvironment())
                .executionRootName("org.jboss.eap:parent")
                .executionRootVersion("7.0.3")
                .temporaryBuild(false)
                .dependency(importedArtifact1)
                .build();

        nextId = Sequence.nextBase32Id();
        log.info("####nextId: " + nextId);

        BuildRecord buildRecord5 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .buildConfigurationAudited(buildConfig6SecondAudit)
                .productMilestone(demoProductMilestone6)
                .submitTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.MINUTES)))
                .startTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfig6SecondAudit.getBuildEnvironment())
                .executionRootName("org.jboss.eap:parent")
                .executionRootVersion("7.0.4")
                .temporaryBuild(false)
                .build();

        nextId = Sequence.nextBase32Id();
        log.info("####nextId: " + nextId);

        BuildRecord buildRecord6 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .productMilestone(demoProductMilestone7)
                .buildConfigurationAudited(buildConfig1SecondAudit)
                .submitTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.MINUTES)))
                .startTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfig1SecondAudit.getBuildEnvironment())
                .executionRootName("org.jboss.pnc:parent")
                .executionRootVersion("1.2.3")
                .temporaryBuild(false)
                .build();

        nextId = Sequence.nextBase32Id();
        log.info("####nextId: " + nextId);

        BuildRecord buildRecord7 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .buildConfigurationAudited(buildConfig1ThirdAudit)
                .submitTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.MINUTES)))
                .startTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfig1ThirdAudit.getBuildEnvironment())
                .executionRootName("org.jboss.pnc:parent")
                .executionRootVersion("1.2.3")
                .temporaryBuild(false)
                .build();

        buildRecord4 = buildRecordRepository.save(buildRecord4);
        buildRecord5 = buildRecordRepository.save(buildRecord5);
        buildRecord6 = buildRecordRepository.save(buildRecord6);
        buildRecord7 = buildRecordRepository.save(buildRecord7);

        Artifact builtArtifact11 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord4)
                .identifier("demo:built-artifact11:jar:1.1")
                .targetRepository(targetRepository)
                .filename("demo built artifact 11")
                .md5("md5-fake-abc123")
                .sha1("sha1-fake-abc123")
                .sha256("sha256-fake-abc123")
                .size(10L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built11")
                .build();

        Artifact builtArtifact12 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord5)
                .identifier("demo:built-artifact12:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 12")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(11L)
                .artifactQuality(ArtifactQuality.VERIFIED)
                .deployPath("/built12")
                .build();

        Artifact builtArtifact13 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord6)
                .identifier("demo:built-artifact13:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 13")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(13L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built13")
                .build();

        Artifact builtArtifact14 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact14:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 14")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(14L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built14")
                .build();

        Artifact builtArtifact15 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact15:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 15")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(15L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/built15")
                .build();

        Artifact builtArtifact16a = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact16:jar:1.0.redhat-a")
                .targetRepository(targetRepository)
                .filename("demo built artifact 16a")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(16L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/demo/built-artifact16/1.0.redhat-a/built-artifact16-1.0.redhat-a.jar")
                .build();

        Artifact builtArtifact16b = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact16:jar:1.0.redhat-b")
                .targetRepository(targetRepository)
                .filename("demo built artifact 16b")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(16L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/demo/built-artifact16/1.0.redhat-b/built-artifact16-1.0.redhat-b.jar")
                .build();
        Artifact builtArtifact17 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact17:jar:1.0.redhat")
                .targetRepository(targetRepository)
                .filename("demo built artifact 17")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(17L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/demo/built-artifact17/1.0.redhat/built-artifact17-1.0.redhat.jar")
                .build();
        Artifact builtArtifact18 = Artifact.Builder.newBuilder()
                .buildRecord(buildRecord7)
                .identifier("demo:built-artifact18:jar:1.0.redhat")
                .targetRepository(targetRepository)
                .filename("demo built artifact 18")
                .md5("md5-fake-123abc")
                .sha1("sha1-fake-123abc")
                .sha256("sha256-fake-123abc")
                .size(18L)
                .artifactQuality(ArtifactQuality.NEW)
                .deployPath("/demo/built-artifact18/1.0.redhat/built-artifact18-1.0.redhat.jar")
                .build();

        builtArtifact11 = artifactRepository.save(builtArtifact11);
        builtArtifact12 = artifactRepository.save(builtArtifact12);
        builtArtifact13 = artifactRepository.save(builtArtifact13);
        builtArtifact14 = artifactRepository.save(builtArtifact14);
        builtArtifact15 = artifactRepository.save(builtArtifact15);
        builtArtifact16a = artifactRepository.save(builtArtifact16a);
        builtArtifact16b = artifactRepository.save(builtArtifact16b);
        builtArtifact17 = artifactRepository.save(builtArtifact17);
        builtArtifact18 = artifactRepository.save(builtArtifact18);

        demoProductMilestone1 = productMilestoneRepository.queryById(demoProductMilestone1.getId());
        demoProductMilestone3 = productMilestoneRepository.queryById(demoProductMilestone3.getId());
        demoProductMilestone7 = productMilestoneRepository.queryById(demoProductMilestone7.getId());

        BuildConfigSetRecord buildConfigSetRecord1 = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet1)
                .startTime(Timestamp.from(Instant.now()))
                .endTime(Timestamp.from(Instant.now()))
                .user(demoUser)
                .status(BuildStatus.FAILED)
                .buildRecords(Set.of(savedBuildRecord1))
                .temporaryBuild(false)
                .build();
        savedBuildRecord1.setBuildConfigSetRecord(buildConfigSetRecord1);
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
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .build();
        operation2 = deliverableAnalyzerOperationRepository.save(operation2);

        DeliverableAnalyzerOperation operation3 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000003l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone3)
                .build();
        operation3 = deliverableAnalyzerOperationRepository.save(operation3);

        DeliverableAnalyzerOperation operation4 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000004l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone7)
                .build();
        operation4 = deliverableAnalyzerOperationRepository.save(operation4);

        DeliverableAnalyzerOperation operation5 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000005l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .build();
        operation5 = deliverableAnalyzerOperationRepository.save(operation5);

        DeliverableAnalyzerOperation operation6 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000006l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone2)
                .build();
        operation6 = deliverableAnalyzerOperationRepository.save(operation6);

        DeliverableAnalyzerOperation operation7 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000007l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone3)
                .build();
        operation7 = deliverableAnalyzerOperationRepository.save(operation7);

        DeliverableAnalyzerOperation operation8 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000008l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .build();
        operation8 = deliverableAnalyzerOperationRepository.save(operation8);

        DeliverableAnalyzerOperation operation9 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000009l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone5)
                .build();
        operation9 = deliverableAnalyzerOperationRepository.save(operation9);

        DeliverableAnalyzerOperation operation10 = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(new Base32LongID(1000010l))
                .progressStatus(ProgressStatus.FINISHED)
                .submitTime(TODAY)
                .startTime(TODAY)
                .user(demoUser)
                .productMilestone(demoProductMilestone1)
                .build();
        operation10 = deliverableAnalyzerOperationRepository.save(operation10);

        DeliverableAnalyzerReport report1 = DeliverableAnalyzerReport.builder()
                .operation(operation2)
                .labels(EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED))
                .build();
        report1 = deliverableAnalyzerReportRepository.save(report1);

        DeliverableAnalyzerReport report2 = DeliverableAnalyzerReport.builder()
                .operation(operation3)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report2 = deliverableAnalyzerReportRepository.save(report2);

        DeliverableAnalyzerReport report3 = DeliverableAnalyzerReport.builder()
                .operation(operation4)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report3 = deliverableAnalyzerReportRepository.save(report3);

        DeliverableAnalyzerReport report4 = DeliverableAnalyzerReport.builder()
                .operation(operation5)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report4 = deliverableAnalyzerReportRepository.save(report4);

        DeliverableAnalyzerReport report5 = DeliverableAnalyzerReport.builder()
                .operation(operation6)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report5 = deliverableAnalyzerReportRepository.save(report5);

        DeliverableAnalyzerReport report6 = DeliverableAnalyzerReport.builder()
                .operation(operation7)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report6 = deliverableAnalyzerReportRepository.save(report6);

        DeliverableAnalyzerReport report7 = DeliverableAnalyzerReport.builder()
                .operation(operation8)
                .labels(EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH))
                .build();
        report7 = deliverableAnalyzerReportRepository.save(report7);

        DeliverableAnalyzerReport report8 = DeliverableAnalyzerReport.builder()
                .operation(operation9)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report8 = deliverableAnalyzerReportRepository.save(report8);

        DeliverableAnalyzerReport report9 = DeliverableAnalyzerReport.builder()
                .operation(operation10)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        report9 = deliverableAnalyzerReportRepository.save(report9);

        DeliverableAnalyzerLabelEntry report1LabelEntry1 = DeliverableAnalyzerLabelEntry.builder()
                .report(report1)
                .changeOrder(1)
                .entryTime(ONE_WEEK_BEFORE_TODAY)
                .user(pncAdminUser)
                .label(DeliverableAnalyzerReportLabel.RELEASED)
                .change(LabelOperation.ADDED)
                .reason("This was a game-changer! Release it! <3")
                .build();
        deliverableAnalyzerLabelEntryRepository.save(report1LabelEntry1);

        DeliverableArtifact analyzedArtifact1 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact1)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact2 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact5)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact3 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact9)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact4 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact10)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact5 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact11)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact6 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(builtArtifact12)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact7 = DeliverableArtifact.builder()
                .report(report1)
                .artifact(importedArtifact2)
                .builtFromSource(false)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact8 = DeliverableArtifact.builder()
                .report(report2)
                .artifact(builtArtifact13)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact9 = DeliverableArtifact.builder()
                .report(report3)
                .artifact(builtArtifact14)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact10 = DeliverableArtifact.builder()
                .report(report3)
                .artifact(builtArtifact15)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact11a = DeliverableArtifact.builder()
                .report(report4)
                .artifact(builtArtifact16a)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact11b = DeliverableArtifact.builder()
                .report(report4)
                .artifact(builtArtifact16b)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();
        DeliverableArtifact analyzedArtifact12 = DeliverableArtifact.builder()
                .report(report5)
                .artifact(builtArtifact16b)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact13 = DeliverableArtifact.builder()
                .report(report5)
                .artifact(builtArtifact17)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact14 = DeliverableArtifact.builder()
                .report(report6)
                .artifact(builtArtifact18)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact15 = DeliverableArtifact.builder()
                .report(report7)
                .artifact(builtArtifact18)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact16 = DeliverableArtifact.builder()
                .report(report8)
                .artifact(builtArtifact13)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact17 = DeliverableArtifact.builder()
                .report(report8)
                .artifact(builtArtifact2)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact18 = DeliverableArtifact.builder()
                .report(report2)
                .artifact(builtArtifact2)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        DeliverableArtifact analyzedArtifact19 = DeliverableArtifact.builder()
                .report(report9)
                .artifact(builtArtifact18)
                .builtFromSource(true)
                .brewBuildId(null)
                .build();

        deliverableArtifactRepository.save(analyzedArtifact1);
        deliverableArtifactRepository.save(analyzedArtifact2);
        deliverableArtifactRepository.save(analyzedArtifact3);
        deliverableArtifactRepository.save(analyzedArtifact4);
        deliverableArtifactRepository.save(analyzedArtifact5);
        deliverableArtifactRepository.save(analyzedArtifact6);
        deliverableArtifactRepository.save(analyzedArtifact7);
        deliverableArtifactRepository.save(analyzedArtifact8);
        deliverableArtifactRepository.save(analyzedArtifact9);
        deliverableArtifactRepository.save(analyzedArtifact10);
        deliverableArtifactRepository.save(analyzedArtifact11a);
        deliverableArtifactRepository.save(analyzedArtifact11b);
        deliverableArtifactRepository.save(analyzedArtifact12);
        deliverableArtifactRepository.save(analyzedArtifact13);
        deliverableArtifactRepository.save(analyzedArtifact14);
        deliverableArtifactRepository.save(analyzedArtifact15);
        deliverableArtifactRepository.save(analyzedArtifact16);
        deliverableArtifactRepository.save(analyzedArtifact17);
        deliverableArtifactRepository.save(analyzedArtifact18);
        deliverableArtifactRepository.save(analyzedArtifact19);
    }

    private RepositoryConfiguration createRepositoryConfiguration(String internalScmUrl, String externalUrl) {
        return RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(internalScmUrl)
                .externalUrl(externalUrl)
                .build();
    }

}
