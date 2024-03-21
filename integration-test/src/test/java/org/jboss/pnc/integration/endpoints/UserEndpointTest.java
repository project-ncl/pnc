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
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.UserClient;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class UserEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(UserEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void testGetAllBuilds() throws ClientException {

        UserClient client = new UserClient(RestClientConfiguration.asUser());
        BuildClient buildClient = new BuildClient(RestClientConfiguration.asAnonymous());

        BuildsFilterParameters params = new BuildsFilterParameters();
        params.setLatest(false);
        params.setRunning(false);

        RemoteCollection<Build> remoteBuilds = buildClient.getAll(null, null);

        User user = remoteBuilds.iterator().next().getUser();

        RemoteCollection<Build> builds = client.getBuilds(user.getId(), params);

        assertThat(builds).isNotNull();
        assertThat(builds.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testGetUser() throws ClientException {

        // when
        UserClient client = new UserClient(RestClientConfiguration.asUser());
        User user = client.getCurrentUser();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        assertThat(user.getId()).isNotNull();

        // when
        client = new UserClient(RestClientConfiguration.asSystem());
        user = client.getCurrentUser();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNotNull();
        assertThat(user.getId()).isNotNull();
    }
}
