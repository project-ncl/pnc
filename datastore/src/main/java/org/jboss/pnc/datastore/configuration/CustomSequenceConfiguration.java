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
package org.jboss.pnc.datastore.configuration;

import org.jboss.pnc.datastore.repositories.DefaultSequenceHandlerRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

@Singleton
@Startup
public class CustomSequenceConfiguration {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private DefaultSequenceHandlerRepository sequenceHandlerRepository;

    @PostConstruct
    public void initialize() {

        String hbm2ddlAutoValue = sequenceHandlerRepository.getEntityManagerFactoryProperty("hibernate.hbm2ddl.auto");
        logger.info("Found hibernate.hbm2ddl.auto {} ...", hbm2ddlAutoValue);
        // Possible values: validate | update | create | create-drop

        prepareSequence(hbm2ddlAutoValue, BuildRecord.SEQUENCE_NAME);
        prepareSequence(hbm2ddlAutoValue, BuildConfiguration.SEQUENCE_NAME);
    }

    private void prepareSequence(String hbm2ddlAutoValue, String sequenceName) {
        // I need to drop and re-create model
        if ("create".equalsIgnoreCase(hbm2ddlAutoValue) || "create-drop".equalsIgnoreCase(hbm2ddlAutoValue)) {

            try {
                logger.info("Dropping sequence {} ...", sequenceName);
                sequenceHandlerRepository.dropSequence(sequenceName);
            } catch (Exception e) {
                logger.debug(
                        "Error encountered when dropping sequence {} in 'create' or 'create-drop' schema phase. This can be safely ignored, as is due to db schema and/or sequence not yet existing.",
                        sequenceName);
            }

            try {
                logger.info("Creating sequence {} ...", sequenceName);
                sequenceHandlerRepository.createSequence(sequenceName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }

        // If I need to update the model them I can safely ignore any "sequence already-existing" error
        if ("update".equalsIgnoreCase(hbm2ddlAutoValue)) {

            try {
                logger.info("Updating sequence {} ...", sequenceName);
                sequenceHandlerRepository.createSequence(sequenceName);
            } catch (Exception e) {
                logger.debug(
                        "Error encountered when creating sequence {} in 'update' schema phase. This can be safely ignored, as is due to sequence already existing.",
                        sequenceName);
            }

        }
    }

}
