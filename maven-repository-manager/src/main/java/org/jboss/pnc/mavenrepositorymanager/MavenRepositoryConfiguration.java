package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.RepositoryManagerType;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;

public class MavenRepositoryConfiguration implements RepositoryConfiguration
{

    private final String id;

    private final RepositoryConnectionInfo connectionInfo;

    // TODO: Create and pass in suitable parameters to Aprox to create the
    //       proxy repository.
    public MavenRepositoryConfiguration(String id, MavenRepositoryConnectionInfo info)
    {
        this.id = id;
        this.connectionInfo = info;
    }


    @Override
    public String toString() {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }


    @Override
    public RepositoryManagerType getType() {
        return RepositoryManagerType.MAVEN;
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public RepositoryConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }
}
