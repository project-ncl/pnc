package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;
import org.jboss.logging.Logger;
import org.jboss.pnc.datastore.repositories.*;
import org.jboss.pnc.model.*;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

/**
 * Data for the DEMO.
 */
@Singleton
@Startup
public class DatabaseDataInitializer {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PNC_PRODUCT_NAME = "Project Newcastle Demo Product";
    private static final String PNC_PRODUCT_VERSION = "1.0.0.DR1";
    private static final String PNC_PROJECT_1_NAME = "Project Newcastle Demo Project 1";
    private static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductVersionProjectRepository productVersionProjectRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    EnvironmentRepository environmentRepository;

    @PostConstruct
    public void initialize() {
        initiliazeData();
        verifyData();
    }

    public void verifyData() {
        // Check number of entities in DB
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions.checkState(buildConfigurationRepository.count() > 0, "Expecting number of BuildConfigurations > 0");
        Preconditions.checkState(productVersionRepository.count() > 0, "Expecting number of ProductVersions > 0");
        Preconditions.checkState(productVersionProjectRepository.count() > 0, "Expecting number of ProductVersionProjects > 0");

        BuildConfiguration buildConfigurationDB = buildConfigurationRepository.findAll().get(0);

        // Check that BuildConfiguration and ProductVersionProject have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getProductVersion() != null,
                "Product version of buildConfiguration must be not null");

        ProductVersionProject productVersionProjectDB = productVersionProjectRepository.findAll().get(0);

        Preconditions.checkState(productVersionProjectDB.getProductVersion() != null,
                "Product version of productVersionProject must be not null");

        // Check that mapping between Product and Project via ProductVersionProject is correct
        Preconditions.checkState(productVersionProjectDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to Project must be " + PNC_PRODUCT_NAME);
        Preconditions.checkState(productVersionProjectDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to Project must be " + PNC_PRODUCT_VERSION);
        Preconditions.checkState(productVersionProjectDB.getProject().getName().equals(PNC_PROJECT_1_NAME),
                "Project mapped to Product must be " + PNC_PROJECT_1_NAME);

        // Check that BuildConfiguration and ProductVersionProject have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to BuildConfiguration must be " + PNC_PRODUCT_VERSION);
        Preconditions.checkState(buildConfigurationDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to BuildConfiguration must be " + PNC_PRODUCT_NAME);

        // Check data of BuildConfiguration
        Preconditions.checkState(buildConfigurationDB.getProject().getName().equals(PNC_PROJECT_1_NAME),
                "Project mapped to BuildConfiguration must be " + PNC_PROJECT_1_NAME);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initiliazeData() {
        logger.info("Initializing DEMO data");

        long numberOfProjectInDB = projectRepository.count();
        if (numberOfProjectInDB == 0) {

            Environment environment1 = createAndPersistDefultEnvironment();
            Environment environment2 = createAndPersistDefultEnvironment();

            /*
             * All the bi-directional mapping settings are managed inside the Builders
             */
            Product product = Product.Builder.newBuilder().name(PNC_PRODUCT_NAME).description("Project Newcastle Product")
                    .build();
            product = productRepository.save(product);

            ProductVersion productVersion = ProductVersion.Builder.newBuilder().version(PNC_PRODUCT_VERSION).product(product)
                    .build();
            productVersion = productVersionRepository.save(productVersion);

            // Example projects
            Project project1 = Project.Builder.newBuilder()
                    .name(PNC_PROJECT_1_NAME).description("Example Project for Newcastle Demo")
                    .projectUrl("https://github.com/project-ncl/pnc")
                    .issueTrackerUrl("https://projects.engineering.redhat.com/browse/NCL").build();
            Project project2 = Project.Builder.newBuilder()
                    .name("JBoss Modules").description("JBoss Modules Project")
                    .projectUrl("https://github.com/jboss-modules/jboss-modules")
                    .issueTrackerUrl("https://issues.jboss.org/browse/MODULES").build();
            Project project3 = Project.Builder.newBuilder()
                    .name("JBoss JavaEE Servlet Spec API").description("JavaEE Servlet Spec API")
                    .projectUrl("https://github.com/jboss/jboss-servlet-api_spec")
                    .issueTrackerUrl("https://issues.jboss.org/browse/JBEE").build();

            // Map projects to the product version
            ProductVersionProject productVersionProject1 = ProductVersionProject.Builder.newBuilder().project(project1)
                    .productVersion(productVersion).build();
            ProductVersionProject productVersionProject2 = ProductVersionProject.Builder.newBuilder().project(project2)
                    .productVersion(productVersion).build();
            ProductVersionProject productVersionProject3 = ProductVersionProject.Builder.newBuilder().project(project3)
                    .productVersion(productVersion).build();

            // Example build configurations
            BuildConfiguration buildConfiguration1 = BuildConfiguration.Builder.newBuilder()
                    .name(PNC_PROJECT_BUILD_CFG_ID)
                    .project(project1)
                    .description("Test build config for project newcastle")
                    .environment(environment1)
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .scmRepoURL("https://github.com/project-ncl/pnc.git")
                    .productVersion(productVersion)
                    .scmRevision("*/v0.2")
                    .build();
            buildConfiguration1 = buildConfigurationRepository.save(buildConfiguration1);

            BuildConfiguration buildConfiguration2 = BuildConfiguration.Builder.newBuilder()
                    .name("jboss-modules-1.5.0")
                    .project(project2)
                    .description("Test config for JBoss modules build master branch.")
                    .environment(environment2)
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .productVersion(productVersion)
                    .scmRepoURL("https://github.com/jboss-modules/jboss-modules.git")
                    .scmRevision("9e7115771a791feaa5be23b1255416197f2cda38")
                    .build();
            buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

            BuildConfiguration buildConfiguration3 = BuildConfiguration.Builder.newBuilder()
                    .name("jboss-servlet-spec-api-1.0.1")
                    .project(project3)
                    .description("Test build for jboss java servlet api")
                    .environment(environment1)
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .productVersion(productVersion)
                    .scmRepoURL("https://github.com/jboss/jboss-servlet-api_spec.git")
                    .dependency(buildConfiguration2)
                    .build();
            buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

            User demoUser = User.Builder.newBuilder().username("demo-user").firstName("Demo First Name")
                    .lastName("Demo Last Name").email("demo-user@pnc.com").build();

            BuildRecord buildRecord = BuildRecord.Builder.newBuilder().buildScript("mvn clean deploy -Dmaven.test.skip")
                    .name(PNC_PROJECT_BUILD_CFG_ID).buildConfiguration(buildConfiguration3)
                    .scmRepoURL("https://github.com/project-ncl/pnc.git").scmRevision("*/v0.2")
                    .description("Build record test").build();

            project1.getBuildConfigurations().add(buildConfiguration1);
            project2.getBuildConfigurations().add(buildConfiguration2);
            project3.getBuildConfigurations().add(buildConfiguration3);

            userRepository.save(demoUser);
            buildRecordRepository.save(buildRecord);

        } else {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization." + numberOfProjectInDB);
        }

        logger.info("Finished initializing DEMO data");
    }

    private Environment createAndPersistDefultEnvironment() {
        Environment environment = Environment.Builder.defaultEnvironment().build();
        return environmentRepository.save(environment);
    }

}
