/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.demo.data;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;

/**
 * This class runs at startup to initialize the application data.
 */
@Singleton
@Startup
@DependsOn("CustomSequenceConfiguration")
public class DemoDataInitializer {

    private static final Logger logger = Logger.getLogger(DemoDataInitializer.class);

    @Inject
    Configuration configuration;

    @Inject
    DatabaseDataInitializer dbDataInitializer;

    @Inject
    ProjectRepository projectRepository;

    @PostConstruct
    public void initialize() {
        DemoDataConfig demoDataConfig = null;
        try {
            demoDataConfig = configuration.getModuleConfig(new PncConfigProvider<>(DemoDataConfig.class));
        } catch (ConfigurationParseException e) {
            logger.warn("Cannot read demo data config.", e);
        }

        if (demoDataConfig == null || !demoDataConfig.getImportDemoData()) {
            logger.info("Demo data import is not enabled.");
            return;
        }

        long numberOfProjectInDB = projectRepository.count();
        if (numberOfProjectInDB != 0) {
            logger.info("There are >0 ({}) projects in DB. Skipping initialization." + numberOfProjectInDB);
        } else {
            logger.info("Initializing DEMO data");
            dbDataInitializer.initiliazeProjectProductData();
            dbDataInitializer.updateBuildConfigurations();
            dbDataInitializer.initiliazeBuildRecordDemoData();
            dbDataInitializer.verifyData();
            logger.info("Finished initializing DEMO data");
        }
    }

}
