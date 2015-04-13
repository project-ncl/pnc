package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.aprox.client.core.util.UrlUtils;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class ImportDepVerifyPromotionToSharedImportsTest extends AbstractImportTest {

    @Test
    public void extractBuildArtifactsTriggersImportPromotion() throws Exception {
        String path = "/org/myproj/myproj/1.0/myproj-1.0.pom";
        String content = "This is a test " + System.currentTimeMillis();

        // setup the expectation that the remote repo pointing at this server will request this file...and define its content.
        server.expect(server.formatUrl(STORE, path), 200, content);

        BuildExecution execution = new TestBuildExecution();
        RepositorySession session = driver.createBuildRepository(execution);

        // simulate a build resolving an artifact via the AProx remote repository.
        assertThat(download(UrlUtils.buildUrl(session.getConnectionInfo().getDependencyUrl(), path)), equalTo(content));

        // now, extract the build artifacts. This will trigger promotion of imported stuff to shared-imports.
        RepositoryManagerResult result = session.extractBuildArtifacts();

        // do some sanity checks while we're here
        List<Artifact> deps = result.getDependencies();
        assertThat(deps.size(), equalTo(1));

        Artifact a = deps.get(0);
        assertThat(a.getFilename(), equalTo(new File(path).getName()));

        // end result: you should be able to download this artifact from shared-imports now.
        assertThat(download(aprox.content().contentUrl(StoreType.hosted, SHARED_IMPORTS, path)), equalTo(content));
    }

}
