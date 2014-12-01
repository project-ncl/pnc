package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.spi.repositorymanager.Repository;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

public class MavenRepositoryConfiguration implements RepositoryConfiguration
{
    // TODO: Create and pass in suitable parameters to Aprox to create the
    //       proxy repository.
    public MavenRepositoryConfiguration()
    {
    }


    @Override
    public void persist() {

    }

    @Override
    public Repository getSourceRepository() {
        //TODO : call aprox ?
        return new Repository() {
            @Override
            public void persist() {

            }
        };
    }

    @Override
    public Repository getDeploymentRepository() {
        //TODO : call aprox ?
        return new Repository() {
            @Override
            public void persist() {

            }
        };
    }


    public String toString()
    {
        return "MavenRepositoryConfiguration " + this.hashCode();
    }
}
