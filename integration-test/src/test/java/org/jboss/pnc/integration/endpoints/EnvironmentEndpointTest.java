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

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.EnvironmentClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class EnvironmentEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentEndpointTest.class);
    private static String environmentId;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asAnonymous());
        environmentId = client.getAll().iterator().next().getId();
    }

    @Test
    @InSequence(10)
    public void testGetAllEnvironments() throws RemoteResourceException {
        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Environment> all = client.getAll();

        assertThat(all).hasSize(2); // from DatabaseDataInitializer
    }

    @Test
    @InSequence(20)
    public void testGetSpecificEnvironment() throws ClientException {
        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asAnonymous());

        Environment environment = client.getSpecific(environmentId);

        assertThat(environment.getName()).isEqualTo("Demo Environment 1"); // from DatabaseDataInitializer
    }

    @Test
    @InSequence(20)
    public void testQueryForEnvironment() throws RemoteResourceException {
        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Environment> allNonDeprecated = client
                .getAll(Optional.empty(), Optional.of("deprecated==false"));

        RemoteCollection<Environment> allDeprecated = client.getAll(Optional.empty(), Optional.of("deprecated==true"));

        assertThat(allNonDeprecated.getAll().stream().map(Environment::isDeprecated)).containsOnly(false);
        assertThat(allDeprecated.getAll().stream().map(Environment::isDeprecated)).containsOnly(true);

    }

    @Test
    @InSequence(30)
    public void shouldSaveEnvironmentAndDeprecateOldOnes() throws ClientException {

        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asSystem());
        Environment.Builder environmentBuilder = Environment.builder()
                .name("OpenJDK 14.0; Mvn 3.6.5")
                .description("OpenJDK 14.0; Mvn 3.6.5")
                .systemImageRepositoryUrl("quay.io/rh-newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE);

        Environment envVers10 = client
                .createNew(environmentBuilder.systemImageId("builder-rhel-7-j14-mvn3.6.5:1.0").build());
        String envVers10Id = envVers10.getId();
        Environment envVers10RetrievedFromDB = client.getSpecific(envVers10Id);
        Assertions.assertThat(envVers10RetrievedFromDB.getName()).isEqualTo("OpenJDK 14.0; Mvn 3.6.5");
        Assertions.assertThat(envVers10RetrievedFromDB.getSystemImageId()).isEqualTo("builder-rhel-7-j14-mvn3.6.5:1.0");
        Assertions.assertThat(envVers10RetrievedFromDB.isDeprecated()).isEqualTo(false);

        Environment envVers11 = client
                .createNew(environmentBuilder.systemImageId("builder-rhel-7-j14-mvn3.6.5:1.1").build());
        String envVers11Id = envVers11.getId();
        Environment envVers11RetrievedFromDB = client.getSpecific(envVers11Id);
        Assertions.assertThat(envVers11RetrievedFromDB.getName()).isEqualTo("OpenJDK 14.0; Mvn 3.6.5");
        Assertions.assertThat(envVers11RetrievedFromDB.getSystemImageId()).isEqualTo("builder-rhel-7-j14-mvn3.6.5:1.1");
        Assertions.assertThat(envVers11RetrievedFromDB.isDeprecated()).isEqualTo(false);

        Environment envVers12 = client
                .createNew(environmentBuilder.systemImageId("builder-rhel-7-j14-mvn3.6.5:1.2").build());
        String envVers12Id = envVers12.getId();
        Environment envVers12RetrievedFromDB = client.getSpecific(envVers12Id);
        Assertions.assertThat(envVers12RetrievedFromDB.getName()).isEqualTo("OpenJDK 14.0; Mvn 3.6.5");
        Assertions.assertThat(envVers12RetrievedFromDB.getSystemImageId()).isEqualTo("builder-rhel-7-j14-mvn3.6.5:1.2");
        Assertions.assertThat(envVers12RetrievedFromDB.isDeprecated()).isEqualTo(false);

        envVers10RetrievedFromDB = client.getSpecific(envVers10Id);
        envVers11RetrievedFromDB = client.getSpecific(envVers11Id);
        Assertions.assertThat(envVers12RetrievedFromDB.getName()).isEqualTo(envVers10RetrievedFromDB.getName());
        Assertions.assertThat(envVers12RetrievedFromDB.getName()).isEqualTo(envVers11RetrievedFromDB.getName());
        Assertions.assertThat(envVers12RetrievedFromDB.isDeprecated()).isEqualTo(false);
        Assertions.assertThat(envVers11RetrievedFromDB.isDeprecated()).isEqualTo(true);
        Assertions.assertThat(envVers10RetrievedFromDB.isDeprecated()).isEqualTo(true);
    }

    @Test
    @InSequence(30)
    public void shouldFailToSaveEnvironmentAsUser() {
        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asUser());
        Environment.Builder environmentBuilder = Environment.builder()
                .name("OpenJDK 14.0; Mvn 3.6.5")
                .description("OpenJDK 14.0; Mvn 3.6.5")
                .systemImageId("builder-rhel-8-j14-mvn3.6.5:1.0")
                .systemImageRepositoryUrl("quay.io/rh-newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE);

        Exception caught = null;
        try {
            client.createNew(environmentBuilder.build());
        } catch (ClientException e) {
            caught = e;
        }
        Assertions.assertThat(caught).isNotNull();
        Assertions.assertThat(caught.getCause()).isInstanceOf(javax.ws.rs.ForbiddenException.class);
    }

}
