package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-15.
 */
public class BuildJobConfiguration {
    private ProjectBuildConfiguration projectBuildConfiguration;
    private RepositoryConfiguration repositoryConfiguration;
    private BuildCollection buildCollection;


    public BuildJobConfiguration(ProjectBuildConfiguration projectBuildConfiguration) {
        this.projectBuildConfiguration = projectBuildConfiguration;

        //TODO remove buildCollection mock
        buildCollection = new BuildCollection();
        ProductVersion productVersion = new ProductVersion();
        productVersion.setVersion("my-product-version");
        Product product = new Product();
        product.setName("my-product");
        productVersion.setProduct(product);
        buildCollection.setProductVersion(productVersion);
    }

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    public BuildCollection getBuildCollection() {
        return buildCollection;
    }

    public void setBuildCollection(BuildCollection buildCollection) {
        this.buildCollection = buildCollection;
    }

    public Integer getId() {
        return this.projectBuildConfiguration.getId();
    }
}
