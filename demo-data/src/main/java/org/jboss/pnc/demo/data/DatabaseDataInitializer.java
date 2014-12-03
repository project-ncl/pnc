package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
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

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    @PostConstruct
    public void initialize() {
        initilizeData();
        verifyData();
    }

    public void verifyData() {
        Preconditions.checkState(projectRepository.count() > 0, "Expecting number of Projects > 0");
        Preconditions.checkState(productRepository.count() > 0, "Expecting number of Products > 0");
        Preconditions.checkState(projectBuildConfigurationRepository.count() > 0, "Expecting number of ProjectBuildConfigurations > 0");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initilizeData() {
        logger.info("Initializing DEMO data");

        long numberOfProjectInDB = projectRepository.count();
        if(numberOfProjectInDB == 0) {
            Project project = new Project();
            project.setName("demo");

            ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
            projectBuildConfigurationA1.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
            projectBuildConfigurationA1.setProject(project);
            project.addProjectBuildConfiguration(projectBuildConfigurationA1);

            projectRepository.save(project);

            Product product = new Product("foo", "foo description");

            productRepository.save(product);
        } else {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization.", numberOfProjectInDB);
        }

        logger.info("Finished initializing DEMO data");
    }

}
