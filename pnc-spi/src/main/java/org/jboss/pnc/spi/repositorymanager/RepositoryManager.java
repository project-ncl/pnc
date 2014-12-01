package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryManagerType;

import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    boolean canManage(RepositoryManagerType managerType);

    RepositoryConfiguration createBuildRepository(ProjectBuildConfiguration productBuildConfiguration);

    List<Artifact> promoteBuildRepositoryOutput(RepositoryConfiguration b);

    List<Artifact> getBuildRepositoryInput(RepositoryConfiguration b);

    void closeBuildRepository(RepositoryConfiguration b);

    /*
    // TODO:
    // Determine how internal product repositories should be created/handled.

    ProductRepositoryConfiguration createProductRepository(ProductConfiguration p);

    List<ProductRepositoryConfiguration> listOpenProductRepositories();

    void closeProductRepository(ProductRepositoryConfiguration p);
    */
}