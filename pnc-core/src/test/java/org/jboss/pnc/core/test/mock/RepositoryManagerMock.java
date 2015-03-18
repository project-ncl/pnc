package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {


    @Override
    public RepositorySession createBuildRepository(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet) throws RepositoryManagerException {

        RepositorySession repositoryConfiguration = new RepositorySessionMock();
        return repositoryConfiguration;
    }

    @Override
    public boolean canManage(RepositoryType managerType) {
        return true;
    }

}
