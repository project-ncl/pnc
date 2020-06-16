/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.apache.commons.io.IOUtils;
import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectVersionRef;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.util.UrlUtils;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.SHARED_IMPORTS_ID;

@Category(ContainerTest.class)
public class DownloadTwoThenVerifyExtractedArtifactsContainThemTest extends AbstractImportTest {

    @Test
    public void extractBuildArtifacts_ContainsTwoDownloads() throws Exception {
        String pomPath = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.pom";
        String jarPath = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.jar";
        String pomContent = "This is a pom test " + System.currentTimeMillis();
        String jarContent = "This is a jar test " + System.currentTimeMillis();

        // setup the expectation that the remote repo pointing at this server will request this file...and define its
        // content.
        server.expect(server.formatUrl(STORE, pomPath), 200, pomContent);
        server.expect(server.formatUrl(STORE, jarPath), 200, jarContent);

        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();

        RepositorySession rc = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap());
        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDependencyUrl();

        // download the two files via the repo session's dependency URL, which will proxy the test http server
        // using the expectations above
        assertThat(download(UrlUtils.buildUrl(baseUrl, pomPath)), equalTo(pomContent));
        assertThat(download(UrlUtils.buildUrl(baseUrl, jarPath)), equalTo(jarContent));

        // extract the build artifacts, which should contain the two imported deps.
        // This will also trigger promoting imported artifacts into the shared-imports hosted repo
        RepositoryManagerResult repositoryManagerResult = rc.extractBuildArtifacts(true);

        List<Artifact> deps = repositoryManagerResult.getDependencies();
        System.out.println(deps);

        assertThat(deps, notNullValue());
        assertThat("Expected 2 dependencies, got: " + deps, deps.size(), equalTo(2));

        ProjectVersionRef pvr = new SimpleProjectVersionRef("org.commonjava.indy", "indy-core", "0.17.0");
        Set<String> refs = new HashSet<>();
        refs.add(new SimpleArtifactRef(pvr, "pom", null).toString());
        refs.add(new SimpleArtifactRef(pvr, "jar", null).toString());

        // check that both files are in the dep artifacts list using getIdentifier() to match on GAVT[C]
        for (Artifact artifact : deps) {
            assertThat(
                    artifact + " is not in the expected list of deps: " + refs,
                    refs.contains(artifact.getIdentifier()),
                    equalTo(true));
        }

        Indy indy = driver.getIndy(accessToken);

        // check that the new imports are available from shared-imports
        assertAvailableInSharedImports(indy, pomContent, pomPath);
        assertAvailableInSharedImports(indy, jarContent, jarPath);
    }

    private void assertAvailableInSharedImports(Indy indy, String content, String path)
            throws IndyClientException, IOException {
        InputStream stream = indy.content().get(new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, SHARED_IMPORTS_ID), path);
        String downloaded = IOUtils.toString(stream, (String) null);
        assertThat(downloaded, equalTo(content));
    }

}
