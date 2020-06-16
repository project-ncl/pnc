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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.commonjava.atlas.maven.ident.ref.ProjectVersionRef;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.ref.SimpleProjectVersionRef;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(ContainerTest.class)
public class UploadTwoThenVerifyExtractedArtifactsContainThemTest extends AbstractImportTest {

    @Test
    public void extractBuildArtifacts_ContainsTwoUploads() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();

        RepositorySession rc = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap());
        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDeployUrl();
        String pomPath = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.pom";
        String jarPath = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.jar";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        // upload a couple files related to a single GAV using the repo session deployment url
        // this simulates a build deploying one jar and its associated POM
        for (String path : new String[] { pomPath, jarPath }) {
            final String url = UrlUtils.buildUrl(baseUrl, path);

            assertThat(
                    "Failed to upload: " + url,
                    ArtifactUploadUtils.put(client, url, "This is a test"),
                    equalTo(true));
        }

        // extract the "built" artifacts we uploaded above.
        RepositoryManagerResult repositoryManagerResult = rc.extractBuildArtifacts(true);

        // check that both files are present in extracted result
        List<Artifact> artifacts = repositoryManagerResult.getBuiltArtifacts();
        System.out.println(artifacts);

        assertThat(artifacts, notNullValue());
        assertThat(artifacts.size(), equalTo(2));

        ProjectVersionRef pvr = new SimpleProjectVersionRef("org.commonjava.indy", "indy-core", "0.17.0");
        Set<String> refs = new HashSet<>();
        refs.add(new SimpleArtifactRef(pvr, "pom", null).toString());
        refs.add(new SimpleArtifactRef(pvr, "jar", null).toString());

        // check that the artifact getIdentifier() stores GAVT[C] information in the standard Maven rendering
        for (Artifact artifact : artifacts) {
            assertThat(
                    artifact + " is not in the expected list of built artifacts: " + refs,
                    refs.contains(artifact.getIdentifier()),
                    equalTo(true));
        }

        // check that we can download the two files from the build repository
        for (String path : new String[] { pomPath, jarPath }) {
            StoreKey hostedKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, rc.getBuildRepositoryId());
            final String url = indy.content().contentUrl(hostedKey, path);
            boolean downloaded = client.execute(new HttpGet(url), response -> {
                try {
                    return response.getStatusLine().getStatusCode() == 200;
                } finally {
                    if (response instanceof CloseableHttpResponse) {
                        IOUtils.closeQuietly((CloseableHttpResponse) response);
                    }
                }
            });

            assertThat("Failed to download: " + url, downloaded, equalTo(true));
        }

        client.close();

    }

}
