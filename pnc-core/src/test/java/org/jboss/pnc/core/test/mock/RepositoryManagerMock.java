package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {


    @Override
    public RepositoryConfiguration createRepository(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet) throws RepositoryManagerException {

        RepositoryConfiguration repositoryConfiguration = new RepositoryConfigurationMock();
        return repositoryConfiguration;
    }

    @Override
    public boolean canManage(RepositoryType managerType) {
        return true;
    }

}
