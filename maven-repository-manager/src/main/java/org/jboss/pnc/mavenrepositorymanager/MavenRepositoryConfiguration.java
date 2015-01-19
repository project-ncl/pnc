package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;

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
    public RepositoryType getType() {
        return RepositoryType.MAVEN;
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
