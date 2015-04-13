package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.util.UrlUtils;
import org.commonjava.aprox.model.core.StoreType;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadTwoThenVerifyExtractedArtifactsContainThemTest 
 extends AbstractImportTest
{

    @Test
    public void extractBuildArtifacts_ContainsTwoDownloads() throws Exception {
        String pomPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.pom";
        String jarPath = "org/commonjava/aprox/aprox-core/0.17.0/aprox-core-0.17.0.jar";
        String content = "This is a test " + System.currentTimeMillis();

        // setup the expectation that the remote repo pointing at this server will request this file...and define its content.
        server.expect(server.formatUrl(STORE, pomPath), 200, content);
        server.expect(server.formatUrl(STORE, jarPath), 200, content);

        BuildExecution execution = new TestBuildExecution();

        RepositorySession rc = driver.createBuildRepository(execution);
        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDependencyUrl();

        assertThat(download(UrlUtils.buildUrl(baseUrl, pomPath)), equalTo(content));
        assertThat(download(UrlUtils.buildUrl(baseUrl, jarPath)), equalTo(content));

        RepositoryManagerResult repositoryManagerResult = rc.extractBuildArtifacts();

        List<Artifact> deps = repositoryManagerResult.getDependencies();
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

        Aprox aprox = driver.getAprox();

        for (String path : new String[] { pomPath, jarPath }) {
            InputStream stream = aprox.content().get(StoreType.hosted, SHARED_IMPORTS, path);
            String downloaded = IOUtils.toString(stream);
            assertThat(downloaded, equalTo(content));
        }

    }

}
