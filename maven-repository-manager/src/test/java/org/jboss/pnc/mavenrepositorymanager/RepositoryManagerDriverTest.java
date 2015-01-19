package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.commonjava.aprox.boot.BootStatus;
import org.commonjava.aprox.client.core.util.UrlUtils;
import org.commonjava.aprox.test.fixture.core.CoreServerFixture;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

    @Test
    public void formatRepositoryURLForSimpleInfo_CheckDependencyURL() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

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
    public void formatRepositoryURLForSimpleInfo_AllURLsMatch() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

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

    @Test
    public void persistArtifacts_ContainsTwoDownloads() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration rc = driver.createRepository(pbc, bc);
        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDependencyUrl();
        String pomPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.pom";
        String jarPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.jar";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        for (String path : new String[] { pomPath, jarPath }) {
            final String url = UrlUtils.buildUrl(baseUrl, path);
            boolean downloaded = client.execute(new HttpGet(url), new ResponseHandler<Boolean>() {
                @Override
                public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    try {
                        return response.getStatusLine().getStatusCode() == 200;
                    } finally {
                        if (response instanceof CloseableHttpResponse) {
                            IOUtils.closeQuietly((CloseableHttpResponse) response);
                        }
                    }
                }
            });

            assertThat("Failed to download: " + url, downloaded, equalTo(true));
        }

        client.close();

        BuildRecord record = new BuildRecord();
        driver.persistArtifacts(rc, record);

        List<Artifact> deps = record.getDependencies();
        System.out.println(deps);

        assertThat(deps, notNullValue());
        assertThat(deps.size(), equalTo(2));

        ProjectVersionRef pvr = new ProjectVersionRef("org.commonjava.aprox", "aprox-core", "0.17.0");
        Set<String> refs = new HashSet<>();
        refs.add(new ArtifactRef(pvr, "pom", null, false).toString());
        refs.add(new ArtifactRef(pvr, "jar", null, false).toString());

        for (Artifact artifact : deps) {
            assertThat(artifact + " is not in the expected list of deps: " + refs, refs.contains(artifact.getIdentifier()),
                    equalTo(true));
        }
    }

    @Test
    public void persistArtifacts_ContainsTwoUploads() throws Exception {
        BuildConfiguration pbc = simpleBuildConfiguration();

        BuildCollection bc = new BuildCollection();
        bc.setProductVersion(pbc.getProductVersion());

        RepositoryConfiguration rc = driver.createRepository(pbc, bc);
        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDeployUrl();
        String pomPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.pom";
        String jarPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.jar";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        for (String path : new String[] { pomPath, jarPath }) {
            final String url = UrlUtils.buildUrl(baseUrl, path);

            HttpPut put = new HttpPut(url);
            put.setEntity(new StringEntity("This is a test"));

            boolean uploaded = client.execute(put, new ResponseHandler<Boolean>() {
                @Override
                public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    try {
                        return response.getStatusLine().getStatusCode() == 201;
                    } finally {
                        if (response instanceof CloseableHttpResponse) {
                            IOUtils.closeQuietly((CloseableHttpResponse) response);
                        }
                    }
                }
            });

            assertThat("Failed to upload: " + url, uploaded, equalTo(true));
        }

        client.close();

        BuildRecord record = new BuildRecord();
        driver.persistArtifacts(rc, record);

        List<Artifact> artifacts = record.getBuiltArtifacts();
        System.out.println(artifacts);

        assertThat(artifacts, notNullValue());
        assertThat(artifacts.size(), equalTo(2));

        ProjectVersionRef pvr = new ProjectVersionRef("org.commonjava.aprox", "aprox-core", "0.17.0");
        Set<String> refs = new HashSet<>();
        refs.add(new ArtifactRef(pvr, "pom", null, false).toString());
        refs.add(new ArtifactRef(pvr, "jar", null, false).toString());

        for (Artifact artifact : artifacts) {
            assertThat(artifact + " is not in the expected list of built artifacts: " + refs,
                    refs.contains(artifact.getIdentifier()),
                    equalTo(true));
        }
    }

    private BuildConfiguration simpleBuildConfiguration() {
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
