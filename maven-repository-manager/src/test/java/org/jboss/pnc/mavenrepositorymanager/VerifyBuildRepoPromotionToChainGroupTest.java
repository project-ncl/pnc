package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.commonjava.aprox.folo.client.AproxFoloContentClientModule;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class VerifyBuildRepoPromotionToChainGroupTest extends AbstractRepositoryManagerDriverTest {

    @Test
    public void extractBuildArtifactsTriggersBuildRepoPromotionToChainGroup() throws Exception {
        String path = "/org/myproj/myproj/1.0/myproj-1.0.pom";
        String content = "This is a test " + System.currentTimeMillis();

        String chainId = "chain";
        String buildId = "build";

        BuildExecution execution = new TestBuildExecution(null, chainId, buildId, BuildExecutionType.COMPOSED_BUILD);
        RepositorySession session = driver.createBuildRepository(execution);

        // simulate a build deploying a file.
        driver.getAprox().module(AproxFoloContentClientModule.class)
                .store(buildId, StoreType.hosted, buildId, path, new ByteArrayInputStream(content.getBytes()));

        // now, extract the build artifacts. This will trigger promotion of the build hosted repo to the chain group.
        RepositoryManagerResult result = session.extractBuildArtifacts();

        // do some sanity checks while we're here
        List<Artifact> deps = result.getBuiltArtifacts();
        assertThat(deps.size(), equalTo(1));

        Artifact a = deps.get(0);
        assertThat(a.getFilename(), equalTo(new File(path).getName()));

        // end result: the chain group should contain the build hosted repo.
        Group chainGroup = driver.getAprox().stores().load(StoreType.group, chainId, Group.class);
        assertThat(chainGroup.getConstituents().contains(new StoreKey(StoreType.hosted, buildId)), equalTo(true));
    }

}
