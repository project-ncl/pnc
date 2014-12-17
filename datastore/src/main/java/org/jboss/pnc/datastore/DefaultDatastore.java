package org.jboss.pnc.datastore;

import org.jboss.pnc.datastore.repositories.ProjectBuildResultRepository;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

@Stateless
public class DefaultDatastore implements Datastore {

    @Inject
    ProjectBuildResultRepository projectBuildResultRepository;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void storeCompletedBuild(ProjectBuildResult buildResult) {
        projectBuildResultRepository.save(buildResult);
    }
}
