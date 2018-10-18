/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.commonjava.indy.client.core.util.UrlUtils;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.jboss.pnc.enums.RepositoryType;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(ContainerTest.class)
public class UploadOneThenDownloadAndVerifyArtifactHasOriginUrlTest
    extends AbstractImportTest
{

    private static final Logger log = LoggerFactory.getLogger(UploadOneThenDownloadAndVerifyArtifactHasOriginUrlTest.class);

    @Test
    public void extractBuildArtifacts_ContainsTwoUploads() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();
        RepositorySession rc = driver.createBuildRepository(execution, accessToken, accessToken, RepositoryType.MAVEN);

        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDeployUrl();
        String path = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.pom";
        String content = "This is a test";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        // upload a couple files related to a single GAV using the repo session deployment url
        // this simulates a build deploying one jar and its associated POM
        final String url = UrlUtils.buildUrl(baseUrl, path);

        assertThat("Failed to upload: " + url, ArtifactUploadUtils.put(client, url, content), equalTo(true));

        // download the two files via the repo session's dependency URL, which will proxy the test http server
        // using the expectations above
        assertThat(download(UrlUtils.buildUrl(baseUrl, path)), equalTo(content));

        ProjectVersionRef pvr = new SimpleProjectVersionRef("org.commonjava.indy", "indy-core", "0.17.0");
        String artifactRef = new SimpleArtifactRef(pvr, "pom", null).toString();

        // extract the "builtArtifacts" artifacts we uploaded above.
        RepositoryManagerResult repositoryManagerResult = rc.extractBuildArtifacts();

        // check that both files are present in extracted result
        List<Artifact> builtArtifacts = repositoryManagerResult.getBuiltArtifacts();
        log.info("Built artifacts: " + builtArtifacts.toString());

        assertThat(builtArtifacts, notNullValue());
        assertThat(builtArtifacts.size(), equalTo(1));

        Artifact builtArtifact = builtArtifacts.get(0);
        assertThat(builtArtifact + " doesn't match pom ref: " + artifactRef,
                artifactRef.equals(builtArtifact.getIdentifier()),
                equalTo(true));

        client.close();
    }

}
