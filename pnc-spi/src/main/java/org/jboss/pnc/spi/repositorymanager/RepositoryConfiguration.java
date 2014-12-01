package org.jboss.pnc.spi.repositorymanager;

/**
 * Encapsulates Aprox configuration for the source(s) repositories
 * and deployment repository,
 */
public interface RepositoryConfiguration
{
    /**
     * Persist the BuildConfiguration in storage
     */
    void persist ();

    /**
     * TODO: Should this be using Aprox api ?
     */
    Repository getSourceRepository();

    Repository getDeploymentRepository ();
}
