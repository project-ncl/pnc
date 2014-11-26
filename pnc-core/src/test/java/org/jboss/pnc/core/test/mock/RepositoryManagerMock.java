package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.spi.repositorymanager.Repository;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.model.RepositoryManagerType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {
    @Override
    public Repository createEmptyRepository() {
        return new Repository() {
            @Override
            public void persist() {

            }
        };
    }

    @Override
    public Repository createProxyRepository() {
        return new Repository() {
            @Override
            public void persist() {

            }
        };
    }

    @Override
    public boolean canManage(RepositoryManagerType managerType) {
        return true;
    }
}
