package org.jboss.pnc.datastore.configuration;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class CustomSequenceConfiguration {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private SequenceHandlerRepository sequenceHandlerRepository;

    @PostConstruct
    public void initialize() {

        String hbm2ddlAutoValue = sequenceHandlerRepository.getEntityManagerFactoryProperty("hibernate.hbm2ddl.auto");
        logger.info("Found hibernate.hbm2ddl.auto {} ...", hbm2ddlAutoValue);

        if ("create".equalsIgnoreCase(hbm2ddlAutoValue) || "create-drop".equalsIgnoreCase(hbm2ddlAutoValue)) {

            try {
                logger.info("Dropping sequence {} ...", BuildRecord.SEQUENCE_NAME);
                sequenceHandlerRepository.dropSequence(BuildRecord.SEQUENCE_NAME);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            try {
                logger.info("Dropping sequence {} ...", BuildConfigSetRecord.SEQUENCE_NAME);
                sequenceHandlerRepository.dropSequence(BuildConfigSetRecord.SEQUENCE_NAME);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            try {
                logger.info("Creating sequence {} ...", BuildRecord.SEQUENCE_NAME);
                sequenceHandlerRepository.createSequence(BuildRecord.SEQUENCE_NAME);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            try {
                logger.info("Creating sequence {} ...", BuildConfigSetRecord.SEQUENCE_NAME);
                sequenceHandlerRepository.createSequence(BuildConfigSetRecord.SEQUENCE_NAME);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

}
