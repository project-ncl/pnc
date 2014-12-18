package org.jboss.pnc.demo.data;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionProjectRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.model.Product;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    	logger.info("initialize()");
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
        Preconditions.checkState(projectBuildConfigurationDB.getProductVersion().getProduct().getName().equals(PNC_PRODUCT_NAME),
                "Product mapped to ProjectBuildConfiguration must be " + PNC_PRODUCT_NAME);

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

        	try {
        		Product product1 = loadProductXML("/xml-demo-data/product/newcastle-demo-product-1.xml");
        		Project project1 = loadProjectXML("/xml-demo-data/project/newcastle-demo-project-1.xml");
 
        		ProductVersion productVersion1 = loadProductVersionXML("/xml-demo-data/product-version/newcastle-demo-product-1-version-1.xml");
        		productVersion1.setProduct(product1);
        		ProductVersionProject productVersionProject1 = ProductVersionProjectBuilder.newBuilder().project(project1)
                        .productVersion(productVersion1).build();

        		ProjectBuildConfiguration projectBuildConfiguration1 = loadProjectBuildConfigurationXML("/xml-demo-data/build-configuration/newcastle-demo-build-configuration-1.xml");
        		projectBuildConfiguration1.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
        		projectBuildConfiguration1.setProject(project1);
        		projectBuildConfiguration1.setProductVersion(productVersion1);
        		
        		ProjectBuildConfiguration projectBuildConfiguration2 = loadProjectBuildConfigurationXML("/xml-demo-data/build-configuration/newcastle-demo-build-configuration-2.xml");
        		projectBuildConfiguration2.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
        		projectBuildConfiguration2.setProject(project1);
        		projectBuildConfiguration2.setProductVersion(productVersion1);
        		
        		ProjectBuildConfiguration projectBuildConfiguration3 = loadProjectBuildConfigurationXML("/xml-demo-data/build-configuration/newcastle-demo-build-configuration-3.xml");
        		projectBuildConfiguration3.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
        		projectBuildConfiguration3.setProject(project1);
        		projectBuildConfiguration3.setProductVersion(productVersion1);
        		
        		ProjectBuildConfiguration projectBuildConfiguration4 = loadProjectBuildConfigurationXML("/xml-demo-data/build-configuration/newcastle-demo-build-configuration-4.xml");
        		projectBuildConfiguration4.setEnvironment(EnvironmentBuilder.defaultEnvironment().build());
        		projectBuildConfiguration4.setProject(project1);
        		projectBuildConfiguration4.setProductVersion(productVersion1);
        		
                projectRepository.save(project1);
                productRepository.save(product1);
                productVersionRepository.save(productVersion1);
                productVersionProjectRepository.save(productVersionProject1);
                projectBuildConfigurationRepository.save(projectBuildConfiguration1);
                projectBuildConfigurationRepository.save(projectBuildConfiguration2);
                projectBuildConfigurationRepository.save(projectBuildConfiguration3);
                projectBuildConfigurationRepository.save(projectBuildConfiguration4);
        	} catch ( JAXBException e ) {
        		logger.error( "Unable to load data from XML: " + e );
        	} catch ( IOException e ) {
        		logger.error( "Unable to load data from XML: " + e );
        	}

        } else {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization.", numberOfProjectInDB);
        }

        logger.info("Finished initializing DEMO data");
    }
    
    private Product loadProductXML(String filePath) throws JAXBException, IOException {
        InputStream is = this.getClass().getResourceAsStream(filePath);
        Product product = null;
        if (is != null) {
            JAXBContext productJaxbCtxt = JAXBContext.newInstance(Product.class);
            Unmarshaller unmarshaller = productJaxbCtxt.createUnmarshaller();
            product = (Product) unmarshaller.unmarshal(new InputStreamReader(is));
            is.close();
            productRepository.save(product);
        }
        else {
        	logger.warn("Unable to locate xml file: " + filePath);
        }
        return product;
    }

    private ProductVersion loadProductVersionXML(String filePath) throws JAXBException, IOException {
        InputStream is = this.getClass().getResourceAsStream(filePath);
        ProductVersion productVersion = null;
        if (is != null) {
            JAXBContext productVersionJaxbCtxt = JAXBContext.newInstance(ProductVersion.class);
            Unmarshaller unmarshaller = productVersionJaxbCtxt.createUnmarshaller();
            productVersion = (ProductVersion) unmarshaller.unmarshal(new InputStreamReader(is));
            is.close();
        }
        else {
        	logger.warn("Unable to locate xml file: " + filePath);
        }
        return productVersion;
    }

    private Project loadProjectXML(String filePath) throws JAXBException, IOException {
        InputStream is = this.getClass().getResourceAsStream(filePath);

        Project project = null;
        if (is != null) {
           JAXBContext projectJaxbCtxt = JAXBContext.newInstance(Project.class);
           Unmarshaller unmarshaller = projectJaxbCtxt.createUnmarshaller();
           project = (Project) unmarshaller.unmarshal(new InputStreamReader(is));
        }
        return project;
    }

    private ProjectBuildConfiguration loadProjectBuildConfigurationXML(String filePath) throws JAXBException, IOException {
        InputStream is = this.getClass().getResourceAsStream(filePath);

        ProjectBuildConfiguration projectBuildConfiguration = null;
        if (is != null) {
           JAXBContext projectJaxbCtxt = JAXBContext.newInstance(ProjectBuildConfiguration.class);
           Unmarshaller unmarshaller = projectJaxbCtxt.createUnmarshaller();
           projectBuildConfiguration = (ProjectBuildConfiguration) unmarshaller.unmarshal(new InputStreamReader(is));
        }
        return projectBuildConfiguration;
    }

}
