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

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.DemoDataConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

/**
 * This class runs at startup to initialize the application data.
 */
@Singleton
@Startup
@DependsOn("CustomSequenceConfiguration")
public class DemoDataInitializer {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

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
