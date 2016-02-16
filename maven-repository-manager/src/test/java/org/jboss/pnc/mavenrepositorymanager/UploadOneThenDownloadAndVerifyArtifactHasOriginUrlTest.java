/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mavenrepositorymanager;

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
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.util.UrlUtils;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactQuality;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(ContainerTest.class)
public class UploadOneThenDownloadAndVerifyArtifactHasOriginUrlTest
    extends AbstractImportTest
{

    @Test
    public void extractBuildArtifacts_ContainsTwoUploads() throws Exception {
        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();
        RepositorySession rc = driver.createBuildRepository(execution);

        assertThat(rc, notNullValue());

        String baseUrl = rc.getConnectionInfo().getDeployUrl();
        String path = "org/commonjava/indy/indy-core/0.17.0/indy-core-0.17.0.pom";
        String content = "This is a test";

        CloseableHttpClient client = HttpClientBuilder.create().build();

        // upload a couple files related to a single GAV using the repo session deployment url
        // this simulates a build deploying one jar and its associated POM
        final String url = UrlUtils.buildUrl(baseUrl, path);

        HttpPut put = new HttpPut(url);
        put.setEntity(new StringEntity(content));

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

        // download the two files via the repo session's dependency URL, which will proxy the test http server
        // using the expectations above
        assertThat(download(UrlUtils.buildUrl(baseUrl, path)), equalTo(content));

        ProjectVersionRef pvr = new SimpleProjectVersionRef("org.commonjava.indy", "indy-core", "0.17.0");
        String aref = new SimpleArtifactRef(pvr, "pom", null).toString();

        // extract the "built" artifacts we uploaded above.
        RepositoryManagerResult repositoryManagerResult = rc.extractBuildArtifacts();

        // check that both files are present in extracted result
        List<Artifact> built = repositoryManagerResult.getBuiltArtifacts();
        System.out.println(built);

        assertThat(built, notNullValue());
        assertThat(built.size(), equalTo(1));

        Artifact builtArtifact = built.get(0);
        assertThat(builtArtifact + " doesn't match pom ref: " + aref,
                aref.equals(builtArtifact.getIdentifier()),
                equalTo(true));

        List<Artifact> dependencies = repositoryManagerResult.getDependencies();
        assertThat(dependencies, notNullValue());
        assertThat(dependencies.size(), equalTo(1));

        Artifact dep = dependencies.get(0);
        assertThat(dep.getIdentifier(), equalTo(aref));
        assertThat(dep.getArtifactQuality(), equalTo(ArtifactQuality.IMPORTED));
        assertThat(dep.getOriginUrl(), notNullValue());

        client.close();
    }

}
