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
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestHttpServer;
import org.junit.Before;
import org.junit.Rule;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.COMMON_BUILD_GROUP_CONSTITUENTS_GROUP;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.PUBLIC_GROUP_ID;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.SHARED_IMPORTS_ID;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractImportTest extends AbstractRepositoryManagerDriverTest {

    protected static final String STORE = "test";

    @Rule
    public TestHttpServer server = new TestHttpServer("repos");

    protected Indy indy;

    @Before
    public void before() throws Exception {
        indy = driver.getIndy(accessToken);

        // create a remote repo pointing at our server fixture's 'repo/test' directory.
        indy.stores()
                .create(
                        new RemoteRepository(MAVEN_PKG_KEY, STORE, server.formatUrl(STORE)),
                        "Creating test remote repo",
                        RemoteRepository.class);

        StoreKey publicKey = new StoreKey(MAVEN_PKG_KEY, StoreType.group, PUBLIC_GROUP_ID);
        StoreKey pncBuildsKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, PNC_BUILDS);
        StoreKey sharedImportsKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, SHARED_IMPORTS_ID);
        StoreKey commonConstituentsKey = new StoreKey(
                MAVEN_PKG_KEY,
                StoreType.group,
                COMMON_BUILD_GROUP_CONSTITUENTS_GROUP);
        StoreKey remoteKey = new StoreKey(MAVEN_PKG_KEY, StoreType.remote, STORE);

        createHostedIfMissing(pncBuildsKey, false, true);
        createHostedIfMissing(sharedImportsKey, false, true);
        createOrUpdateGroup(publicKey, remoteKey);
        createOrUpdateGroup(commonConstituentsKey, pncBuildsKey, sharedImportsKey, publicKey);
    }

    private void createHostedIfMissing(StoreKey hostedKey, boolean allowSnapshots, boolean allowReleases)
            throws IndyClientException {
        if (!indy.stores().exists(hostedKey)) {
            HostedRepository hosted = new HostedRepository(hostedKey.getPackageType(), hostedKey.getName());
            hosted.setAllowSnapshots(allowSnapshots);
            hosted.setAllowReleases(allowReleases);

            indy.stores().create(hosted, "Creating repository " + hostedKey.getName(), HostedRepository.class);
        }
    }

    private void createOrUpdateGroup(StoreKey groupKey, StoreKey... constituents) throws IndyClientException {
        Group group = indy.stores().load(groupKey, Group.class);
        if (group == null) {
            group = new Group(groupKey.getPackageType(), groupKey.getName(), constituents);
            indy.stores().create(group, "Creating " + groupKey.getName() + " group", Group.class);
        } else {
            List<StoreKey> constituentsList = Arrays.asList(constituents);
            group.setConstituents(constituentsList);
            indy.stores().update(group, "Setting constituents of " + groupKey.getName() + " group");
        }
    }

    protected String download(String url) throws Exception {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        String content = null;
        InputStream stream = null;
        try {
            client = HttpClientBuilder.create().build();
            response = client.execute(new HttpGet(url));
            assertThat(response.getStatusLine().getStatusCode(), equalTo(200));

            stream = response.getEntity().getContent();
            content = IOUtils.toString(stream, "UTF-8");
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }

        return content;
    }

}
