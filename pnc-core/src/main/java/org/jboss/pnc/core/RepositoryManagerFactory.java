package org.jboss.pnc.core;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;


/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class RepositoryManagerFactory {

    @Inject
    Instance<RepositoryManager> availableManagers;

    public RepositoryManager getRepositoryManager(RepositoryType managerType) throws CoreException {

        for (RepositoryManager manager : availableManagers) {
            if (manager.canManage(managerType)) {
                return manager;
            }
        }
        throw new CoreException("No repository manager available for " + managerType + " build type.");
    }

}
