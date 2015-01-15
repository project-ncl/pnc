package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.FileUtils;
import org.commonjava.aprox.boot.BootStatus;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Properties;

public class RepositoryManagerDriverTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private RepositoryManagerDriver driver;
    private CoreServerFixture fixture;
    private String oldIni;

    private String url;

    @Before
    public void setup() throws Exception {
        fixture = new CoreServerFixture(temp);

        Properties sysprops = System.getProperties();
        oldIni = sysprops.getProperty(Configuration.CONFIG_SYSPROP);

        url = fixture.getUrl();
        File configFile = temp.newFile("pnc-config.ini");
        FileUtils.write(configFile, "base.url=" + url);

        sysprops.setProperty(Configuration.CONFIG_SYSPROP, configFile.getAbsolutePath());
        System.setProperties(sysprops);

        fixture.start();

        if (!fixture.isStarted()) {
            final BootStatus status = fixture.getBootStatus();
            throw new IllegalStateException("server fixture failed to boot.", status.getError());
        }

        Properties props = new Properties();
        props.setProperty("base.url", url);

        System.out.println("Using base URL: " + url);

        Configuration config = new Configuration(props);
        driver = new RepositoryManagerDriver(config);
    }

    @After
    public void teardown() throws Exception {
        Properties sysprops = System.getProperties();
        if (oldIni == null) {
            sysprops.remove(Configuration.CONFIG_SYSPROP);
        } else {
            sysprops.setProperty(Configuration.CONFIG_SYSPROP, oldIni);
        }
        System.setProperties(sysprops);

        if (fixture != null) {
            fixture.stop();
        }
    }

    // @Ignore
    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        ProjectBuildConfiguration pbc = simpleProjectBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration repositoryConfiguration = driver.createRepository(pbc, bc);

        assertThat(repositoryConfiguration, notNullValue());

        RepositoryConnectionInfo connectionInfo = repositoryConfiguration.getConnectionInfo();
        assertThat(connectionInfo, notNullValue());

        String expectedUrlPrefix = String.format("%sfolo/track/build+%s", url, pbc.getProject().getName());
        String expectedGroupPathPrefix = String.format("/group/build+%s", pbc.getProject().getName());

        assertThat("Expected URL prefix: " + expectedUrlPrefix + "\nActual URL was: " + connectionInfo.getDependencyUrl(),
                connectionInfo.getDependencyUrl().startsWith(expectedUrlPrefix), equalTo(true));

        assertThat("Expected URL to contain group path prefix: " + expectedGroupPathPrefix + "\nActual URL was: "
                + connectionInfo.getDependencyUrl(), connectionInfo.getDependencyUrl().contains(expectedGroupPathPrefix),
                equalTo(true));
    }

    @Test
    // @Ignore //TODO UPDATE this test
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
