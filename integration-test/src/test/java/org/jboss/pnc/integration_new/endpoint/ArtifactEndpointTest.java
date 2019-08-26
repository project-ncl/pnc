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
package org.jboss.pnc.integration_new.endpoint;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.TargetRepositoryRef;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;

import java.util.ArrayList;
import java.util.List;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ArtifactEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpointTest.class);

    private TargetRepositoryRef targetRepositoryRef;

    private static Artifact artifactRest1;
    private static Artifact artifactRest2;
    private static Artifact artifactRest3;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Before
    public void setTargetRepository() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.NONE));

        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact artifact : client.getAll(null, null, null)) {
            artifacts.add(artifact);
        }

        targetRepositoryRef = artifacts.get(0).getTargetRepository();
        artifactRest1 = artifacts.get(0);
        artifactRest2 = artifacts.get(1);
        artifactRest3 = artifacts.get(4);
        logger.debug("Using targetRepositoryRef: {}", targetRepositoryRef);
    }

    @Test
    public void testGetAllArtifacts() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> all = client.getAll(null, null, null);

        assertThat(all).hasSize(6); // from DatabaseDataInitializer
    }

    @Test
    public void testGetAllArfifactsWithMd5() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, artifactRest1.getMd5(), null);

        //artifacts 1 and 2 have same MD5
        assertThat(artifacts)
                .hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId(), artifactRest2.getId()));
    }

    @Test
    public void testGetAllArfifactsWithSha1() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, null, artifactRest2.getSha1());

        //artifacts 2 and 3 have same SHA1
        assertThat(artifacts)
                .hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest2.getId(), artifactRest3.getId()));
    }

    @Test
    public void testGetAllArfifactsWithSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest1.getSha256(), null, null);

        //artifacts 1 and 3 have same SHA256
        assertThat(artifacts)
                .hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId(), artifactRest3.getId()));
    }

    @Test
    public void testGetAllArfifactsWithMd5AndSha1() throws RemoteResourceException {

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, artifactRest2.getMd5(), artifactRest2.getSha1());

        assertThat(artifacts)
                .hasSize(1)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest2.getId()));
    }

    @Test
    public void testGetAllArfifactsWithMd5AndSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest1.getSha256(), artifactRest1.getMd5(), null);

        assertThat(artifacts)
                .hasSize(1)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId()));
    }

    @Test
    public void testGetAllArfifactsWithSha1AndSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest3.getSha256(), null, artifactRest3.getSha1());

        assertThat(artifacts)
                .hasSize(1)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest3.getId()));
    }

    @Test
    public void testGetSpecificArtifact() throws ClientException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        Artifact artifact = client.getSpecific(artifactRest1.getId());

        assertThat(artifact.getId()).isEqualTo(artifactRest1.getId());
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
        Artifact retrieved = client.getSpecific(id);
        Assertions.assertThat(retrieved.getArtifactQuality()).isEqualTo(ArtifactQuality.NEW);
        Assertions.assertThat(retrieved.getMd5()).isEqualTo("insert-md5-2");
        Assertions.assertThat(retrieved.getSize()).isEqualTo(10L);

        Artifact.Builder builder = inserted.toBuilder();
        builder.artifactQuality(ArtifactQuality.TESTED);
        Artifact update = builder.build();
        client.update(id, update);

        Artifact updated = client.getSpecific(id);
        Assertions.assertThat(updated.getArtifactQuality()).isEqualTo(ArtifactQuality.TESTED);
    }


    @Test
    public void shouldUpdateArtifact() throws ClientException {
        String id = artifactRest1.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact artifact = client.getSpecific(id);
        final long size = artifact.getSize() + 10;
        Artifact updatedArtifact = artifact.toBuilder().size(size).build();
        client.update(id, updatedArtifact);


        Artifact artifact2 = client.getSpecific(id);
        assertThat(artifact2.getSize()).isEqualTo(size);
    }
}
