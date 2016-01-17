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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.RemoteRepository;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.mavenrepositorymanager.fixture.TestHttpServer;
import org.junit.Before;
import org.junit.Rule;

import java.io.InputStream;
import java.util.Collections;

public class AbstractImportTest extends AbstractRepositoryManagerDriverTest {
    
    protected static final String STORE = "test";

    protected static final String PUBLIC = "public";

    protected static final String SHARED_IMPORTS = "shared-imports";

    @Rule
    public TestHttpServer server = new TestHttpServer("repos");

    protected Aprox aprox;
    
    @Before
    public void before() throws Exception
    {
        aprox = driver.getAprox();

        // create a remote repo pointing at our server fixture's 'repo/test' directory.
        aprox.stores().create(new RemoteRepository(STORE, server.formatUrl(STORE)), "Creating test remote repo",
                RemoteRepository.class);
        
        Group publicGroup = aprox.stores().load(StoreType.group, PUBLIC, Group.class);
        if (publicGroup == null) {
            publicGroup = new Group(PUBLIC, new StoreKey(StoreType.remote, STORE));
            aprox.stores().create(publicGroup, "creating public group", Group.class);
        } else {
            publicGroup.setConstituents(Collections.singletonList(new StoreKey(StoreType.remote, STORE)));
            aprox.stores().update(publicGroup, "adding test remote to public group");
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
            content = IOUtils.toString(stream);
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }

        return content;
    }

}
