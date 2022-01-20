/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.folo.client.IndyFoloContentClientModule;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(ContainerTest.class)
public class VerifyBuildRepoPromotionToUntestedBuildsGroupTest extends AbstractImportTest {

    @Test
    public void extractBuildArtifactsTriggersBuildRepoPromotionToChainGroup() throws Exception {
        String path = "/org/myproj/myproj/1.0/myproj-1.0.pom";
        String content = "This is a test " + System.currentTimeMillis();

        String buildId = "build";

        // create a dummy build execution, and a repo session based on it
        BuildExecution execution = new TestBuildExecution(buildId);
        RepositorySession session = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap(),
                false);

        StoreKey hostedKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildId);

        // simulate a build deploying a file.
        indy.module(IndyFoloContentClientModule.class)
                .store(buildId, hostedKey, path, new ByteArrayInputStream(content.getBytes()));

        // now, extract the build artifacts. This will trigger promotion of the build hosted repo to the pnc-builds
        // group.
        RepositoryManagerResult result = session.extractBuildArtifacts(true);
        assertThat(result.getCompletionStatus(), equalTo(CompletionStatus.SUCCESS));

        // do some sanity checks while we're here
        List<Artifact> deps = result.getBuiltArtifacts();
        assertThat(deps.size(), equalTo(1));

        Artifact a = deps.get(0);
        assertThat(a.getFilename(), equalTo(new File(path).getName()));

        // end result: the pnc-builds group should contain the build hosted repo.
        StoreKey pncBuildsKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, PNC_BUILDS);
        assertThat(indy.content().exists(pncBuildsKey, path), equalTo(true));
    }

}
