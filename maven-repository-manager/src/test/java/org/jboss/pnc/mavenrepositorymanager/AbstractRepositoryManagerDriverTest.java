package org.jboss.pnc.mavenrepositorymanager;

import org.apache.commons.io.FileUtils;
import org.commonjava.aprox.boot.BootStatus;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Properties;

public class AbstractRepositoryManagerDriverTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected RepositoryManagerDriver driver;
    protected CoreServerFixture fixture;
    protected String url;

    private String oldIni;

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

    protected BuildConfiguration simpleBuildConfiguration() {
        Project project = new Project();
        project.setName("myproject");

        Product product = new Product();
        product.setName("myproduct");

        ProductVersion pv = new ProductVersion();
        pv.setProduct(product);
        pv.setVersion("1.0");

        BuildConfiguration pbc = new BuildConfiguration();
        pbc.setProject(project);
        pbc.setProductVersion(pv);

        return pbc;
    }

}
