package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.BooleanWrapper;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RepositoryManagerDriverTest {

    private RepositoryManagerDriver driver;

    @Before
    public void setup() {
        Properties props = new Properties();
        props.setProperty("base.url", "http://localhost/");
        Configuration config = new Configuration(props);
        driver = new RepositoryManagerDriver(config);
    }

    @Ignore
    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        ProjectBuildConfiguration pbc = simpleProjectBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        final Semaphore mutex = new Semaphore(1);
        BooleanWrapper completed = new BooleanWrapper(false);

        Consumer<RepositoryConfiguration> onComplete = (repositoryConfiguration) -> {
            assertThat(repositoryConfiguration, notNullValue());

            RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
            assertThat(connectionInfo, notNullValue());

            String expectedUrlWithoutTstamp = String.format("http://localhost/api/group/build+%s+%s+%s+", pbc.getProductVersion()
                    .getProduct().getName(), pbc.getProductVersion().getVersion(), pbc.getProject().getName());

            assertThat(
                    "Expected URL prefix: " + expectedUrlWithoutTstamp + "\nActual URL was: " + connectionInfo.getDependencyUrl(),
                    connectionInfo.getDependencyUrl().startsWith(expectedUrlWithoutTstamp), equalTo(true));

            completed.set(true);
            mutex.release();
        };
        Consumer<Exception> onError = (e) -> {
            e.printStackTrace();
        };

        driver.createRepository(pbc, bc, onComplete, onError);
        mutex.acquire(); //wait for callback to release
        Assert.assertTrue("There was no complete callback.", completed.get());
    }

    @Test
    public void formatRepositoryURLForSimpleInfo_AllURLsMatch() throws Exception {
        ProjectBuildConfiguration pbc = simpleProjectBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        final Semaphore mutex = new Semaphore(1);
        BooleanWrapper completed = new BooleanWrapper(false);

        Consumer<RepositoryConfiguration> onComplete = (repositoryConfiguration) -> {
            assertThat(repositoryConfiguration, notNullValue());

            RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
            assertThat(connectionInfo, notNullValue());

            String expectedUrl = connectionInfo.getDependencyUrl();

            assertThat(connectionInfo.getToolchainUrl(), equalTo(expectedUrl));
            assertThat(connectionInfo.getDeployUrl(), equalTo(expectedUrl));

            completed.set(true);
            mutex.release();
        };
        Consumer<Exception> onError = (e) -> {
            e.printStackTrace();
        };

        driver.createRepository(pbc, bc, onComplete, onError);
        mutex.acquire(); //wait for callback to release
        Assert.assertTrue("There was no complete callback.", completed.get());

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
