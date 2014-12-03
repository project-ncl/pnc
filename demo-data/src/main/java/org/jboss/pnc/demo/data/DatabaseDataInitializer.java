package org.jboss.pnc.demo.data;

import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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

    @PostConstruct
    public void initialize() {
        logger.info("Initializing DEMO data");

        long numberOfProjectInDB = projectRepository.count();
        if(numberOfProjectInDB > 0) {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization.", numberOfProjectInDB);
            Project project = new Project();
            project.setName("demo");

            ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
            projectBuildConfigurationA1.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
            projectBuildConfigurationA1.setProject(project);
            project.addProjectBuildConfiguration(projectBuildConfigurationA1);

            projectRepository.save(project);

            Product product = new Product("foo", "foo description", "1.0");
            BuildCollection buildCollection = new BuildCollection();
            buildCollection.setProduct(product);

            productRepository.save(product);
        }
        logger.info("Finished initializing DEMO data");
    }

}
