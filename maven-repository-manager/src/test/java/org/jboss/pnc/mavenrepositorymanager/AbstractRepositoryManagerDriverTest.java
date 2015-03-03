package org.jboss.pnc.mavenrepositorymanager;

import java.util.Properties;
import java.util.logging.Logger;

import org.commonjava.aprox.boot.BootStatus;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class AbstractRepositoryManagerDriverTest {
    
    private static final Logger log = Logger.getLogger(AbstractRepositoryManagerDriverTest.class.getName());

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    protected RepositoryManagerDriver driver;
    protected CoreServerFixture fixture;
    protected String url;

    @Before
    public void setup() throws Exception {
        fixture = new CoreServerFixture(temp);
        fixture.start();
        
        url = fixture.getUrl();
        if (!fixture.isStarted()) {
            final BootStatus status = fixture.getBootStatus();
            throw new IllegalStateException("server fixture failed to boot.", status.getError());
        }

        log.info("Using base URL: " + url);
        Configuration<MavenRepoDriverModuleConfig> config = new Configuration<MavenRepoDriverModuleConfig>();
        driver = new RepositoryManagerDriver(config);
    }

    @After
    public void teardown() throws Exception {
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
