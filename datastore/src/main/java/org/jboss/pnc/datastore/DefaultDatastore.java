package org.jboss.pnc.datastore;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.Datastore;

@Stateless
public class DefaultDatastore implements Datastore {

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void storeCompletedBuild(BuildRecord buildRecord) {
        buildRecordRepository.save(buildRecord);
    }
}
