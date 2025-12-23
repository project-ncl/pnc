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
import org.jboss.pnc.api.slsa.dto.provenance.v1.BuildDefinition;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.api.slsa.dto.provenance.v1.ResourceDescriptor;
import org.jboss.pnc.api.slsa.dto.provenance.v1.RunDetails;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.client.SlsaProvenanceV1Client;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.rest.endpoints.SlsaProvenanceV1EndpointImpl;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class SlsaProvenanceEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(SlsaProvenanceEndpointTest.class);
    private static String buildId; // buildRecord1

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        try (BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous())) {
            RemoteCollection<Build> builds = bc.getAll(null, null);

            // Sort by ID to retain IDs in the test
            // After, NCL-8156 the default ordering was fixed and changed to submitTime
            Iterator<Build> it = builds.getAll()
                    .stream()
                    .sorted(Comparator.comparingLong(build -> new Base32LongID(build.getId()).getLongId()))
                    .iterator();

            buildId = it.next().getId();
        }
    }

    @Test
    public void shouldSendNotFound() throws RemoteResourceException {
        String buildId = "DOES_NOT_EXIST";
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asSystem())) {
            slsaClient.getFromBuildId(buildId);
        } catch (RemoteResourceNotFoundException | NotFoundException nfe) {
            String reason = String.format(
                    "%s with %s: %s not found. %s",
                    Build.class.getSimpleName(),
                    "id",
                    buildId,
                    SlsaProvenanceV1EndpointImpl.PROVENANCE_UNAVAILABLE);
            assertThat(nfe.getMessage().equals(reason));
        }
    }

    @Test
    public void shouldProduceProvenance() throws RemoteResourceException {
        try (SlsaProvenanceV1Client slsaClient = new SlsaProvenanceV1Client(RestClientConfiguration.asSystem())) {
            Provenance provenance = slsaClient.getFromBuildId(buildId);
            logger.debug("Got provenance: {}", provenance);

            assertThat(provenance.getSubject()).hasSize(3);

            List<String> subjectNames = provenance.getSubject()
                    .stream()
                    .map(ResourceDescriptor::getName)
                    .collect(Collectors.toList());
            List<String> digests = provenance.getSubject()
                    .stream()
                    .map(ResourceDescriptor::getDigest)
                    .filter(Objects::nonNull)
                    .flatMap(m -> m.values().stream())
                    .collect(Collectors.toList());

            assertThat(subjectNames).hasSize(3);
            assertThat(subjectNames).containsExactlyInAnyOrder(
                    "demo built artifact 1",
                    "demo built artifact 2",
                    "demo built artifact 9");
            assertThat(provenance.getPredicateType()).isEqualTo("https://slsa.dev/provenance/v1");

            assertThat(digests).hasSize(3);
            assertThat(digests).containsExactlyInAnyOrder(
                    "1660168483cb8a05d1cc2e77c861682a42ed9517ba945159d5538950c5db00fa",
                    "2fafc2ed0f752ac2540283d48c5cd663254a853c5cb13dec02dce023fc7471a9",
                    "sha256-fake-abcdefg4321");

            BuildDefinition buildDefinition = provenance.getPredicate().getBuildDefinition();
            assertThat(buildDefinition.getBuildType())
                    .isEqualTo("https://project-ncl.github.io/slsa-pnc-buildtypes/workflow/v1");
            assertThat(buildDefinition.getExternalParameters()).containsOnlyKeys("repository", "environment", "build");

            Map<String, String> repository = (Map<String, String>) buildDefinition.getExternalParameters()
                    .get("repository");
            assertThat(repository.keySet()).containsOnly("preBuildSync", "uri", "revision");
            assertThat(repository.get("preBuildSync")).isEqualTo("true");
            assertThat(repository.get("uri")).isEqualTo("https://github.com/project-ncl/pnc.git");
            assertThat(repository.get("revision")).isEqualTo("*/v0.2");

            Map<String, String> build = (Map<String, String>) buildDefinition.getExternalParameters().get("build");
            assertThat(build.keySet()).containsOnly("temporary", "script", "name", "parameters", "type");
            assertThat(build.get("temporary")).isEqualTo("false");
            assertThat(build.get("script")).isEqualTo("mvn deploy -DskipTests=true");
            assertThat(build.get("name")).isEqualTo("pnc-1.0.0.DR1");
            assertThat(build.get("type")).isEqualTo("MVN");

            assertThat(buildDefinition.getInternalParameters()).containsOnlyKeys("defaultAlignmentParameters");
            assertThat(buildDefinition.getInternalParameters().get("defaultAlignmentParameters")).isEqualTo(
                    "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true");

            Optional<ResourceDescriptor> repoDownstream = buildDefinition.getResolvedDependencies()
                    .stream()
                    .filter(r -> r.getName().equals("repository.downstream"))
                    .findFirst();
            assertThat(repoDownstream.isPresent()).isTrue();
            assertThat(repoDownstream.get().getUri()).isEqualTo("ssh://git@github.com:22/project-ncl/pnc.git");

            Optional<ResourceDescriptor> dependency = buildDefinition.getResolvedDependencies()
                    .stream()
                    .filter(r -> r.getName().equals("demo imported artifact 1"))
                    .findFirst();
            assertThat(dependency.isPresent()).isTrue();
            assertThat(dependency.get().getAnnotations().get("purl"))
                    .isEqualTo("pkg:maven/demo/imported-artifact1@1.0?type=jar");

            RunDetails runDetails = provenance.getPredicate().getRunDetails();
            assertThat(runDetails.getBuilder().getId()).isEqualTo("/builds/" + buildId);
            assertThat(runDetails.getBuilder().getVersion())
                    .containsOnlyKeys("https://github.com/project-ncl/bifrost", "https://github.com/project-ncl/pnc");
            assertThat(runDetails.getBuilder().getVersion().get("https://github.com/project-ncl/bifrost"))
                    .isEqualTo("http://localhost:8081//version");
            assertThat(runDetails.getBuilder().getVersion().get("https://github.com/project-ncl/pnc"))
                    .isEqualTo("http://pncHost//version");
            assertThat(runDetails.getMetadata().getInvocationId()).isEqualTo(buildId);

            Optional<ResourceDescriptor> buildLog = runDetails.getByproducts()
                    .stream()
                    .filter(r -> r.getName().equals("buildLog"))
                    .findFirst();
            assertThat(buildLog.isPresent()).isTrue();
            assertThat(buildLog.get().getUri()).isEqualTo("/builds/" + buildId + "/logs/build");
            Optional<ResourceDescriptor> alignmentLog = runDetails.getByproducts()
                    .stream()
                    .filter(r -> r.getName().equals("alignmentLog"))
                    .findFirst();
            assertThat(alignmentLog.isPresent()).isTrue();
            assertThat(alignmentLog.get().getUri()).isEqualTo("/builds/" + buildId + "/logs/align");

        }
    }

}
