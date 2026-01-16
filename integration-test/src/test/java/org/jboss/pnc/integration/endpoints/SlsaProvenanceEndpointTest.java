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
import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.api.slsa.dto.provenance.v1.ResourceDescriptor;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.client.SlsaProvenanceV1Client;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.endpoints.SlsaProvenanceV1EndpointImpl;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ws.rs.NotFoundException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:andrea.vibelli@gmail.com">Andrea Vibelli</a>
 *
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class SlsaProvenanceEndpointTest {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static List<Artifact> builtArtifacts = new ArrayList<>();

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {

        try (ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous())) {
            RemoteCollection<Artifact> artifacts = client.getAll(null, null, null);
            builtArtifacts = artifacts.getAll().stream().filter(a -> a.getBuild() != null).collect(Collectors.toList());

            assertThat(builtArtifacts.isEmpty()).isFalse();
        }
    }

    @Test
    public void shouldSendNotFoundById() throws RemoteResourceException {
        String randomId = Integer.toString(RANDOM.nextInt());
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            slsaClient.getFromArtifactId(randomId);
        } catch (RemoteResourceNotFoundException | NotFoundException nfe) {
            String reason = String.format(
                    "Artifact with id: %s not found. %s",
                    randomId,
                    SlsaProvenanceV1EndpointImpl.PROVENANCE_UNAVAILABLE);
            assertThat(nfe.getMessage().equals(reason));
        }
    }

    @Test
    public void shouldSendNotFoundByHash() throws RemoteResourceException {
        String randomId = Integer.toString(RANDOM.nextInt());
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            slsaClient.getFromArtifactDigest(randomHex(32), null, null);
        } catch (RemoteResourceNotFoundException | NotFoundException nfe) {
            String reason = String.format(
                    "Artifact with id: %s not found. %s",
                    randomId,
                    SlsaProvenanceV1EndpointImpl.PROVENANCE_UNAVAILABLE);
            assertThat(nfe.getMessage().equals(reason));
        }
    }

    @Test
    public void shouldProduceProvenanceForArtifactId() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            Provenance provenance = slsaClient.getFromArtifactId(builtArtifacts.get(0).getId());
            assertThat(provenance).isNotNull();
            ResourceDescriptor subject = provenance.getSubject()
                    .stream()
                    .filter(descriptor -> descriptor.getName().equals(builtArtifacts.get(0).getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(subject);
        }
    }

    @Test
    public void shouldProduceProvenanceForArtifactSha256() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            Provenance provenance = slsaClient.getFromArtifactDigest(builtArtifacts.get(0).getSha256(), null, null);
            assertThat(provenance).isNotNull();
            ResourceDescriptor subject = provenance.getSubject()
                    .stream()
                    .filter(descriptor -> descriptor.getName().equals(builtArtifacts.get(0).getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(subject);
        }
    }

    @Test
    public void shouldProduceProvenanceForArtifactSha1() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            Provenance provenance = slsaClient.getFromArtifactDigest(null, null, builtArtifacts.get(0).getSha1());
            assertThat(provenance).isNotNull();
            ResourceDescriptor subject = provenance.getSubject()
                    .stream()
                    .filter(descriptor -> descriptor.getName().equals(builtArtifacts.get(0).getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(subject);
        }
    }

    @Test
    public void shouldProduceProvenanceForArtifactMd51() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            Provenance provenance = slsaClient.getFromArtifactDigest(null, builtArtifacts.get(0).getMd5(), null);
            assertThat(provenance).isNotNull();
            ResourceDescriptor subject = provenance.getSubject()
                    .stream()
                    .filter(descriptor -> descriptor.getName().equals(builtArtifacts.get(0).getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(subject);
        }
    }

    @Test
    public void shouldProduceProvenanceForArtifactShas1() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asUser())) {
            Provenance provenance = slsaClient.getFromArtifactDigest(
                    builtArtifacts.get(0).getSha256(),
                    builtArtifacts.get(0).getMd5(),
                    builtArtifacts.get(0).getSha1());
            assertThat(provenance).isNotNull();
            ResourceDescriptor subject = provenance.getSubject()
                    .stream()
                    .filter(descriptor -> descriptor.getName().equals(builtArtifacts.get(0).getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(subject);
        }
    }

    private static String randomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);

        StringBuilder sb = new StringBuilder(byteLength * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
