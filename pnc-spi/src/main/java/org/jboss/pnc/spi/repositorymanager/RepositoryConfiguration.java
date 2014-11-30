package org.jboss.pnc.spi.repositorymanager;

import java.util.List;
import org.jboss.pnc.spi.repositorymanager.*;

/**
 * Encapsulates Aprox configuration for the source(s) repositories
 * and deployment repository,
 */
public interface RepositoryConfiguration
{
    /**
     * TODO: Should this be using Aprox api ?
     */
    Repository getSourceRepository();

    Repository getDeploymentRepository ();
}
