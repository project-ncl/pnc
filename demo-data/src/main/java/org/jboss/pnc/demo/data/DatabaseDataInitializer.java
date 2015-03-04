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

    private static final String PNC_PRODUCT_NAME = "PNC Product";
    private static final String PNC_PRODUCT_VERSION = "1.0.0.DR1";
    private static final String PNC_PROJECT_NAME = "PNC Project";
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
        Preconditions.checkState(productVersionProjectDB.getProject().getName().equals(PNC_PROJECT_NAME),
                "Project mapped to Product must be " + PNC_PROJECT_NAME);

        // Check that BuildConfiguration and ProductVersionProject have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to BuildConfiguration must be " + PNC_PRODUCT_VERSION);
        Preconditions.checkState(buildConfigurationDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to BuildConfiguration must be " + PNC_PRODUCT_NAME);

        // Check data of BuildConfiguration
        Preconditions.checkState(buildConfigurationDB.getProject().getName().equals(PNC_PROJECT_NAME),
                "Project mapped to BuildConfiguration must be " + PNC_PROJECT_NAME);

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
            ProductVersion productVersion = ProductVersion.Builder.newBuilder().version(PNC_PRODUCT_VERSION).product(product)
                    .build();

            Project project = Project.Builder.newBuilder().name(PNC_PROJECT_NAME).description("Project Newcastle Demo Project")
                    .projectUrl("https://github.com/project-ncl/pnc")
                    .issueTrackerUrl("https://projects.engineering.redhat.com/browse/NCL").build();

            // Needed to build correct mapping
            ProductVersionProject productVersionProject = ProductVersionProject.Builder.newBuilder().project(project)
                    .productVersion(productVersion).build();

            BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(environment1).name(PNC_PROJECT_BUILD_CFG_ID)
                    .productVersion(productVersion).project(project).scmRepoURL("https://github.com/project-ncl/pnc.git")
                    .scmRevision("*/v0.2").description("Test build config for project newcastle").build();
            buildConfiguration = buildConfigurationRepository.save(buildConfiguration);

            // Additional configurations
            BuildConfiguration buildConfiguration2 = BuildConfiguration.Builder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(environment2).name("jboss-modules-1.5.0")
                    .productVersion(productVersion).project(project)
                    .description("Test config for JBoss modules build master branch.")
                    .scmRepoURL("https://github.com/jboss-modules/jboss-modules.git").build();
            buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

            BuildConfiguration buildConfiguration3 = BuildConfiguration.Builder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(environment1).name("jboss-servlet-spec-api-1.0.1")
                    .productVersion(productVersion).project(project)
                    .scmRepoURL("https://github.com/jboss/jboss-servlet-api_spec.git").dependency(buildConfiguration2)
                    .description("Test build for jboss java servlet api").build();
            buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

            User demoUser = User.Builder.newBuilder().username("demo-user").firstName("Demo First Name")
                    .lastName("Demo Last Name").email("demo-user@pnc.com").build();

            BuildRecord buildRecord = BuildRecord.Builder.newBuilder().buildScript("mvn clean deploy -Dmaven.test.skip")
                    .name(PNC_PROJECT_BUILD_CFG_ID).buildConfiguration(buildConfiguration3)
                    .scmRepoURL("https://github.com/project-ncl/pnc.git").scmRevision("*/v0.2")
                    .description("Build record test").build();

            project.getBuildConfigurations().add(buildConfiguration);
            project.getBuildConfigurations().add(buildConfiguration2);
            project.getBuildConfigurations().add(buildConfiguration3);

            projectRepository.save(project);
            productRepository.save(product);
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
