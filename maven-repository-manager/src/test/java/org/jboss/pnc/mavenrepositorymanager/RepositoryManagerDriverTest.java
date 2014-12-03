package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class RepositoryManagerDriverTest {

    private RepositoryManagerDriver driver;

    @Before
    public void setup() {
        Properties props = new Properties();
        props.setProperty("base.url", "http://localhost/");
        Configuration config = new Configuration(props);
        driver = new RepositoryManagerDriver(config);
    }

    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        ProjectBuildConfiguration pbc = simpleProjectBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration repositoryConfiguration = driver.createRepository(pbc, bc);
        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrlWithoutTstamp = String.format("http://localhost/api/group/build+%s+%s+%s+", pbc.getProductVersion()
                .getProduct().getName(), pbc.getProductVersion().getVersion(), pbc.getProject().getName());

        assertThat(
                "Expected URL prefix: " + expectedUrlWithoutTstamp + "\nActual URL was: " + connectionInfo.getDependencyUrl(),
                connectionInfo.getDependencyUrl().startsWith(expectedUrlWithoutTstamp), equalTo(true));
    }

    @Test
    public void formatRepositoryURLForSimpleInfo_AllURLsMatch() throws Exception {
        ProjectBuildConfiguration pbc = simpleProjectBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration repositoryConfiguration = driver.createRepository(pbc, bc);
        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrl = connectionInfo.getDependencyUrl();

        assertThat(connectionInfo.getToolchainUrl(), equalTo(expectedUrl));
        assertThat(connectionInfo.getDeployUrl(), equalTo(expectedUrl));
    }

    private ProjectBuildConfiguration simpleProjectBuildConfiguration() {
        Project project = new Project();
        project.setName("myproject");

        Product product = new Product();
        product.setName("myproduct");

        ProductVersion pv = new ProductVersion();
        pv.setProduct(product);
        pv.setVersion("1.0");

        ProjectBuildConfiguration pbc = new ProjectBuildConfiguration();
        pbc.setProject(project);
        pbc.setProductVersion(pv);

        return pbc;
    }

}
