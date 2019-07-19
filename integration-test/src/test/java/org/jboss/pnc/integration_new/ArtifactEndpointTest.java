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

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.TargetRepositoryRef;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ArtifactEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpointTest.class);

    TargetRepositoryRef targetRepositoryRef;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    @Before
    public void setTargetRepository() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.NONE));
        Artifact artifact = client.getAll(null, null, null).iterator().next();
        targetRepositoryRef = artifact.getTargetRepository();
        logger.debug("Using targetRepositoryRef: {}", targetRepositoryRef);
    }

    @Test
    public void shouldFailToSaveArtifact() {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));
        Artifact artifact = Artifact.builder()
                .filename("builtArtifactInsert.jar")
                .identifier("integration-test:built-artifact-insert:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .md5("insert-md5-1")
                .sha1("insert-1")
                .sha256("insert-1")
                .build();

        Exception caught = null;
        try {
            client.create(artifact);
        } catch (ClientException e) {
            caught = e;
        }
        Assertions.assertThat(caught).isNotNull();
        Assertions.assertThat(caught.getCause()).isInstanceOf(javax.ws.rs.ForbiddenException.class);
    }

    @Test
    public void shouldSaveArtifact() throws ClientException {
        Artifact artifact = Artifact.builder()
                .artifactQuality(ArtifactQuality.NEW)
                .filename("builtArtifactInsert2.jar")
                .identifier("integration-test:built-artifact-insert2:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .md5("insert-md5-2")
                .sha1("insert-2")
                .sha256("insert-2")
                .size(10L)
                .build();

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.SYSTEM_USER));

        Artifact inserted = client.create(artifact);
        String id = inserted.getId();
        Artifact retrieved = client.getSpecific(Integer.valueOf(id));
        Assertions.assertThat(retrieved.getArtifactQuality()).isEqualTo(ArtifactQuality.NEW);
        Assertions.assertThat(retrieved.getMd5()).isEqualTo("insert-md5-2");
        Assertions.assertThat(retrieved.getSize()).isEqualTo(10L);

        Artifact.Builder builder = inserted.toBuilder();
        builder.artifactQuality(ArtifactQuality.TESTED);
        Artifact update = builder.build();
        client.update(Integer.valueOf(id), update);

        Artifact updated = client.getSpecific(Integer.valueOf(id));
        Assertions.assertThat(updated.getArtifactQuality()).isEqualTo(ArtifactQuality.TESTED);
    }
}
