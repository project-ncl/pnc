package org.jboss.pnc.spi.repositorymanager.model;

import org.jboss.pnc.model.RepositoryType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryConfiguration {

    RepositoryType getType();

    String getId();

    RepositoryConnectionInfo getConnectionInfo();
}
