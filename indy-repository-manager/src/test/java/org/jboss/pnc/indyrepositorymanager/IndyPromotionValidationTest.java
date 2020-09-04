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
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.commonjava.indy.client.core.util.UrlUtils;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.ConfigProvider;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Collections;

import org.jboss.pnc.enums.RepositoryType;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category({ DebugTest.class })
public class IndyPromotionValidationTest {

    @Test
    /**
     * Test whose purpose is to provide a means for more or less easy debugging of indy validation errors. For it to
     * work it needs a standalone Indy server, with a rule-set definition such as this: { "storeKeyPattern":
     * "group:builds-untested", "ruleNames": [ "no-snapshots.groovy", "parsable-pom.groovy" ], "validationParameters":
     * {} } Provide the base URL to this Indy server as a System parameter such as e.g.
     * -DbaseUrl="http://127.0.0.1:8090"
     */
    public void testIndyPromotionValidation() {
        String baseUrl = System.getProperty("baseUrl");
        if (StringUtils.isBlank(baseUrl)) {
            fail("No base URL has been specified");
        }

        RepositoryManager driver = null;
        try {
            driver = new RepositoryManagerDriver(new TestConfiguration(baseUrl), new BuildRecordRepositoryMock());
            RepositorySession repositorySession = driver.createBuildRepository(
                    new TestBuildExecution("test"),
                    null,
                    null,
                    RepositoryType.MAVEN,
                    Collections.emptyMap());

            CloseableHttpClient client = HttpClientBuilder.create().build();
            String deployUrl = repositorySession.getConnectionInfo().getDeployUrl();

            // Deploy several 'wrong' artifacts to get a composed error message back
            String pathPom1 = "org/foo/invalid/1/invalid-1.pom";
            String snapshotPom = "<?xml version=\"1.0\"?>\n<project><modelVersion>4.0.0</modelVersion><groupId>org.foo</groupId>"
                    + "<artifactId>invalid</artifactId><version>1</version><dependencies>"
                    + "<dependency><groupId>org.bar</groupId><artifactId>dep</artifactId>"
                    + "<version>1.0-SNAPSHOT</version></dependency></dependencies></project>";
            String url = UrlUtils.buildUrl(deployUrl, pathPom1);
            put(client, url, snapshotPom);

            String pathPom2 = "org/foo/invalid2/1/invalid2-1.pom";
            String snapshotPom2 = "<?xml version=\"1.0\"?>\n<project><modelVersion>4.0.0</modelVersion><groupId>org.foo</groupId>"
                    + "<artifactId>invalid2</artifactId><version>1</version><dependencies>"
                    + "<dependency><groupId>org.bar</groupId><artifactId>dep</artifactId>"
                    + "<version>1.0-SNAPSHOT</version></dependency></dependencies></project>";
            url = UrlUtils.buildUrl(deployUrl, pathPom2);
            put(client, url, snapshotPom2);

            String pathPom3 = "org/foo/nonparseable/1/nonparseable.pom";
            String nonparseablePom = "<?xml version=\"1.0\"?>\n<project><modelVersion>4.0.0</modelVersion><groupId>org.foo</groupId>"
                    + "<artifactId>nonparseable</artifactId><version>1</version><dependencies>"
                    + "<dependency><groupId>org.bar</groupId><artifactId>dep</artifactId>"
                    + "<version>1.0</version></dependency></dependencies></project>";
            url = UrlUtils.buildUrl(deployUrl, pathPom3);
            put(client, url, nonparseablePom);

            RepositoryManagerResult repositoryManagerResult = repositorySession.extractBuildArtifacts(true);
            // Just a dummy check, the point is really to be able to debug this
            assertSame(CompletionStatus.FAILED, repositoryManagerResult.getCompletionStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean put(CloseableHttpClient client, String url, String content) throws IOException {
        HttpPut put = new HttpPut(url);
        put.setEntity(new StringEntity(content));
        return client.execute(put, response -> {
            try {
                return response.getStatusLine().getStatusCode() == 201;
            } finally {
                if (response instanceof CloseableHttpResponse) {
                    IOUtils.closeQuietly((CloseableHttpResponse) response);
                }
            }
        });
    }

    private class TestConfiguration extends Configuration {
        private String baseUrl;

        private TestConfiguration(String baseUrl) throws ConfigurationParseException {
            super();
            this.baseUrl = baseUrl;
        }

        @Override
        public <T extends AbstractModuleConfig> T getModuleConfig(ConfigProvider<T> provider)
                throws ConfigurationParseException {
            IndyRepoDriverModuleConfig mvnCfg = new IndyRepoDriverModuleConfig();
            return (T) mvnCfg;
        }

        @Override
        public GlobalModuleGroup getGlobalConfig() throws ConfigurationParseException {
            GlobalModuleGroup result = super.getGlobalConfig();
            result.setIndyUrl(baseUrl);
            return result;
        }
    }
}
