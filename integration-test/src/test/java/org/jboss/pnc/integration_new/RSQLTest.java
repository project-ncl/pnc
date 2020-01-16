/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RSQLTest {

    private static final Logger logger = LoggerFactory.getLogger(RSQLTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldConvertBoolean() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String queryTemporary = "temporaryBuild==TRUE";
        RemoteCollection<Build> temporary = client.getAll(null, null, Optional.empty(), Optional.of(queryTemporary));
        assertThat(temporary).hasSize(2);

        String queryPersistent = "temporaryBuild==FALSE";
        RemoteCollection<Build> persistent = client.getAll(null, null, Optional.empty(), Optional.of(queryPersistent));
        assertThat(persistent).hasSize(2);
    }
    @Test
    public void shouldFailWithMisingSelectorElement() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String queryTemporary = "environment==foo";
        assertThatThrownBy(() -> client.getAll(null, null, Optional.empty(), Optional.of(queryTemporary)))
                .isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldConvertEnum() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String queryFailed = "status==FAILED";
        RemoteCollection<Build> temporary = client.getAll(null, null, Optional.empty(), Optional.of(queryFailed));
        assertThat(temporary).isEmpty();

        String querySuccess = "status==SUCCESS";
        RemoteCollection<Build> persistent = client.getAll(null, null, Optional.empty(), Optional.of(querySuccess));
        assertThat(persistent).hasSize(4);
    }

    @Test
    public void shouldConvertDate() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String queryLT = "endTime=lt=2019-01-01T00:00:00Z";
        RemoteCollection<Build> temporary = client.getAll(null, null, Optional.empty(), Optional.of(queryLT));
        assertThat(temporary).isEmpty();

        String queryGT = "endTime=gt=2019-01-01T00:00:00Z";
        RemoteCollection<Build> persistent = client.getAll(null, null, Optional.empty(), Optional.of(queryGT));
        assertThat(persistent).hasSize(4);
    }

    @Test
    public void shouldReturnTemporaryBuildsOlderThanTimestamp() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String queryLT = "endTime=lt=2019-02-02T00:00:00Z;temporaryBuild==TRUE";
        RemoteCollection<Build> temporary = client.getAll(null, null, Optional.empty(), Optional.of(queryLT));
        assertThat(temporary).hasSize(1);
    }

    @Test
    public void shouldFailSortById() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String sortQuery = "sort=desc=id";
        assertThatThrownBy(() -> client.getAll(null, null, Optional.of(sortQuery), Optional.empty()))
                .isInstanceOf(ClientException.class);
    }

}
