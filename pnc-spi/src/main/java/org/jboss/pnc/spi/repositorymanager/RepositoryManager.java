package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.RepositoryManagerType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {
    Repository createEmptyRepository();
    Repository createProxyRepository();

    boolean canManage(RepositoryManagerType managerType);
}
