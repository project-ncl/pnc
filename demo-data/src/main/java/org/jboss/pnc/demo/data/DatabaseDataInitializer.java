package org.jboss.pnc.demo.data;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.EnvironmentRepository;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.model.builder.BuildConfigurationSetBuilder;
import org.jboss.pnc.model.builder.BuildRecordBuilder;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.model.builder.ProductBuilder;
import org.jboss.pnc.model.builder.ProductVersionBuilder;
import org.jboss.pnc.model.builder.ProjectBuilder;
import org.jboss.pnc.model.builder.UserBuilder;

import com.google.common.base.Preconditions;

/**
 * Data for the DEMO.
 */
@Singleton
@Startup
public class DatabaseDataInitializer {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PNC_PRODUCT_NAME = "Project Newcastle Demo Product";
    private static final String PNC_PRODUCT_VERSION = "1.0.0.DR1";
    private static final String PNC_PROJECT_NAME = "Project Newcastle Demo Project 1";
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
    BuildConfigurationSetRepository buildConfigurationSetRepository;

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
        Preconditions.checkState(buildConfigurationSetRepository.count() > 0, "Expecting number of BuildRepositorySets > 0");

        BuildConfiguration buildConfigurationDB = buildConfigurationRepository.findAll().get(0);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next().getProductVersion() != null,
                "Product version of buildConfiguration must be not null");

        BuildConfigurationSet buildConfigurationSetDB = buildConfigurationSetRepository.findAll().get(0);

        Preconditions.checkState(buildConfigurationSetDB.getProductVersion() != null,
                "Product version of buildConfigurationSet must be not null");

        // Check that mapping between Product and Build Configuration via BuildConfigurationSet is correct
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to Project must be " + PNC_PRODUCT_NAME);
        Preconditions.checkState(buildConfigurationSetDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to Project must be " + PNC_PRODUCT_VERSION);

        // Check that BuildConfiguration and BuildConfigurationSet have a ProductVersion associated
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next()
                .getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to BuildConfiguration must be " + PNC_PRODUCT_VERSION);
        Preconditions.checkState(buildConfigurationDB.getBuildConfigurationSets().iterator().next()
                .getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
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
            // Example product and product version
            Product product = ProductBuilder.newBuilder().name(PNC_PRODUCT_NAME).description("Example Product for Project Newcastle Demo")
                    .build();
            ProductVersion productVersion = ProductVersionBuilder.newBuilder().version(PNC_PRODUCT_VERSION).product(product)
                    .build();

            // Example projects
            Project project1 = ProjectBuilder.newBuilder().name(PNC_PROJECT_NAME).description("Example Project for Newcastle Demo")
                    .projectUrl("https://github.com/project-ncl/pnc")
                    .issueTrackerUrl("https://projects.engineering.redhat.com/browse/NCL").build();
            Project project2 = ProjectBuilder.newBuilder().name("JBoss Modules").description("JBoss Modules Project")
                    .projectUrl("https://github.com/jboss-modules/jboss-modules")
                    .issueTrackerUrl("https://issues.jboss.org/browse/MODULES").build();
            Project project3 = ProjectBuilder.newBuilder().name("JBoss JavaEE Servlet Spec API").description("JavaEE Servlet Spec API")
                    .projectUrl("https://github.com/jboss/jboss-servlet-api_spec")
                    .issueTrackerUrl("https://issues.jboss.org/browse/JBEE").build();

            // Example build configurations
            BuildConfiguration buildConfiguration1 = BuildConfigurationBuilder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip").id(1)
                    .environment(environment1).name("Project Newcastle version 0.x demo")
                    .project(project1).scmRepoURL("https://github.com/project-ncl/pnc.git")
                    .scmRevision("*/v0.2").description("Test build config for project newcastle").build();
            buildConfiguration1 = buildConfigurationRepository.save(buildConfiguration1);

            BuildConfiguration buildConfiguration2 = BuildConfigurationBuilder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(environment2).name("jboss-modules-1.5.x")
                    .project(project2)
                    .description("Test config for JBoss modules build master branch.")
                    .scmRepoURL("https://github.com/jboss-modules/jboss-modules.git").build();
            buildConfiguration2 = buildConfigurationRepository.save(buildConfiguration2);

            BuildConfiguration buildConfiguration3 = BuildConfigurationBuilder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(environment1).name("jboss-servlet-spec-api-1.0.x")
                    .project(project3)
                    .scmRepoURL("https://github.com/jboss/jboss-servlet-api_spec.git").dependency(buildConfiguration2)
                    .description("Test build for jboss java servlet api").build();
            buildConfiguration3 = buildConfigurationRepository.save(buildConfiguration3);

            // Build config set containing the three example build configs
            BuildConfigurationSet buildConfigurationSet = BuildConfigurationSetBuilder.newBuilder().name("Build Config Set 1")
                    .buildConfiguration(buildConfiguration1)
                    .buildConfiguration(buildConfiguration2)
                    .buildConfiguration(buildConfiguration3)
                    .productVersion(productVersion).build();

            User demoUser = UserBuilder.newBuilder().username("demo-user").firstName("Demo First Name")
                    .lastName("Demo Last Name").email("demo-user@pnc.com").build();

            BuildRecord buildRecord = BuildRecordBuilder.newBuilder().buildScript("mvn clean deploy -Dmaven.test.skip")
                    .name(PNC_PROJECT_BUILD_CFG_ID).buildConfiguration(buildConfiguration3)
                    .scmRepoURL("https://github.com/project-ncl/pnc.git").scmRevision("*/v0.2")
                    .description("Build record test").build();

            project1.getBuildConfigurations().add(buildConfiguration1);
            project1.getBuildConfigurations().add(buildConfiguration2);
            project1.getBuildConfigurations().add(buildConfiguration3);

            projectRepository.save(project1);
            projectRepository.save(project2);
            projectRepository.save(project3);
            productRepository.save(product);
            buildConfigurationSetRepository.save(buildConfigurationSet);
            userRepository.save(demoUser);
            buildRecordRepository.save(buildRecord);

        } else {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization." + numberOfProjectInDB);
        }

        logger.info("Finished initializing DEMO data");
    }

    private Environment createAndPersistDefultEnvironment() {
        Environment environment = EnvironmentBuilder.defaultEnvironment().build();
        return environmentRepository.save(environment);
    }

}
