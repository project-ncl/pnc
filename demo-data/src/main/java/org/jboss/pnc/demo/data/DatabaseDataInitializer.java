package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionProjectRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersionProject;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.model.builder.ProductBuilder;
import org.jboss.pnc.model.builder.ProductVersionBuilder;
import org.jboss.pnc.model.builder.ProductVersionProjectBuilder;
import org.jboss.pnc.model.builder.ProjectBuildConfigurationBuilder;
import org.jboss.pnc.model.builder.ProjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PNC_PRODUCT_NAME = "PNC Product";
    private static final String PNC_PRODUCT_VERSION = "1.0.0.DR1";
    private static final String PNC_PROJECT_NAME = "PNC Project";
    private static final String PNC_PROJECT_BUILD_CFG_ID = "pnc-1.0.0.DR1";

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductVersionProjectRepository productVersionProjectRepository;

    @PostConstruct
    public void initialize() {
        initilizeData();
        verifyData();
    }

    public void verifyData() {
        // Check number of entities in DB
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions.checkState(projectBuildConfigurationRepository.count() > 0,
                "Expecting number of ProjectBuildConfigurations > 0");
        Preconditions.checkState(productVersionRepository.count() > 0, "Expecting number of ProductVersions > 0");
        Preconditions.checkState(productVersionProjectRepository.count() > 0, "Expecting number of ProductVersionProjects > 0");

        ProjectBuildConfiguration projectBuildConfigurationDB = projectBuildConfigurationRepository.findAll().get(0);

        // Check that ProjectBuildConfiguration and ProductVersionProject have a ProductVersion associated
        Preconditions.checkState(projectBuildConfigurationDB.getProductVersion() != null,
                "Product version of projectBuildConfiguration must be not null");

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

        // Check that ProjectBuildConfiguration and ProductVersionProject have a ProductVersion associated
        Preconditions.checkState(projectBuildConfigurationDB.getProductVersion().getVersion().equals(PNC_PRODUCT_VERSION),
                "Product version mapped to ProjectBuildConfiguration must be " + PNC_PRODUCT_VERSION);
        Preconditions.checkState(projectBuildConfigurationDB.getProductVersion().getProduct().getName()
                .equals(PNC_PRODUCT_NAME), "Product mapped to ProjectBuildConfiguration must be " + PNC_PRODUCT_NAME);

        // Check data of ProjectBuildConfiguration
        Preconditions.checkState(projectBuildConfigurationDB.getProject().getName().equals(PNC_PROJECT_NAME),
                "Project mapped to ProjectBuildConfiguration must be " + PNC_PROJECT_NAME);
        Preconditions.checkState(projectBuildConfigurationDB.getIdentifier().equals(PNC_PROJECT_BUILD_CFG_ID),
                "ProjectBuildConfiguration name must be " + PNC_PROJECT_BUILD_CFG_ID);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initilizeData() {
        logger.info("Initializing DEMO data");

        long numberOfProjectInDB = projectRepository.count();
        if (numberOfProjectInDB == 0) {

            /*
             * All the bi-directional mapping settings are managed inside the Builders
             */

            Product product = ProductBuilder.newBuilder().name(PNC_PRODUCT_NAME).description("Project Newcastle Product")
                    .milestone(ProductMilestone.DR).build();
            ProductVersion productVersion = ProductVersionBuilder.newBuilder().version(PNC_PRODUCT_VERSION).product(product)
                    .build();

            Project project = ProjectBuilder.newBuilder().name(PNC_PROJECT_NAME).description("Project Newcastle Demo Project")
                    .projectUrl("https://github.com/project-ncl/pnc")
                    .issueTrackerUrl("https://projects.engineering.redhat.com/browse/NCL").build();

            // Needed to build correct mapping
            ProductVersionProject productVersionProject = ProductVersionProjectBuilder.newBuilder().project(project)
                    .productVersion(productVersion).build();
            ProjectBuildConfiguration projectBuildConfiguration = ProjectBuildConfigurationBuilder.newBuilder()
                    .buildScript("mvn clean deploy -Dmaven.test.skip")
                    .environment(EnvironmentBuilder.defaultEnvironment().build()).identifier(PNC_PROJECT_BUILD_CFG_ID)
                    .productVersion(productVersion).project(project).scmUrl("https://github.com/project-ncl/pnc.git").build();

            projectRepository.save(project);
            productRepository.save(product);

        } else {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization.", numberOfProjectInDB);
        }

        logger.info("Finished initializing DEMO data");
    }

}
