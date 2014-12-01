package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.RepositoryManagerType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryConfiguration {

    RepositoryManagerType getType();

    String getId();

    RepositoryConnectionInfo getConnectionInfo();
}
