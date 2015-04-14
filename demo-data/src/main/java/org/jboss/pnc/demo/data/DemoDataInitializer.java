package org.jboss.pnc.demo.data;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.datastore.repositories.ProjectRepository;

/**
 * This class runs at startup to initialize the application data.
 */
@Singleton
@Startup
public class DemoDataInitializer {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    DatabaseDataInitializer dbDataInitializer;

    @Inject
    ProjectRepository projectRepository;

    @PostConstruct
    public void initialize() {
        long numberOfProjectInDB = projectRepository.count();
        if (numberOfProjectInDB != 0) {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization." + numberOfProjectInDB);
        } else {
            logger.info("Initializing DEMO data");
            dbDataInitializer.initiliazeProjectProductData();
            dbDataInitializer.initiliazeBuildRecordDemoData();
            logger.info("Finished initializing DEMO data");
        }
        dbDataInitializer.verifyData();
    }

}
