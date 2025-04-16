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
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.client.ProductMilestoneClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.Maps;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.ParsedArtifact;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.requests.DeliverablesAnalysisRequest;
import org.jboss.pnc.dto.requests.validation.VersionValidationRequest;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneDeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.enums.ValidationErrorType;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.integration.utils.BPMWireMock;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jboss.pnc.demo.data.DatabaseDataInitializer.log;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductMilestoneEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductMilestoneEndpointTest.class);

    private static Product product;
    private static ProductVersion productVersion;
    private static ProductMilestone milestone;
    private static String milestoneId;
    private static ProductMilestone milestone2;

    private static ProductMilestone milestone3;

    private static BPMWireMock bpm;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        ProductClient productClient = new ProductClient(RestClientConfiguration.asAnonymous());
        product = productClient.getAll().iterator().next();
        productVersion = productClient.getProductVersions(product.getId()).iterator().next();
        ProductVersionClient productVersionClient = new ProductVersionClient(RestClientConfiguration.asAnonymous());
        Iterator<ProductMilestone> it = productVersionClient.getMilestones(productVersion.getId()).iterator();
        milestone = it.next();
        milestoneId = milestone.getId();
        milestone2 = it.next();
        milestone3 = it.next();
        var bpmPort = 8288;
        bpm = new BPMWireMock(bpmPort);
        log.info("Mocked BPM started at port: " + bpmPort);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        bpm.close();
        log.info("Mocked BPM stopped");
    }

    @Test
    public void testCreateNew() throws ClientException {
        // given
        ProductMilestone productMilestone = ProductMilestone.builder()
                .productVersion(productVersion)
                .version("1.0.0.ER1")
                .startingDate(Instant.ofEpochMilli(100_000))
                .plannedEndDate(Instant.ofEpochMilli(200_000))
                .build();
        // when
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());
        ProductMilestone created = client.createNew(productMilestone);
        assertThat(created.getId()).isNotEmpty();
        ProductMilestone retrieved = client.getSpecific(created.getId());

        // then
        assertThat(created.getProductVersion().getId()).isEqualTo(productMilestone.getProductVersion().getId());
        assertThat(retrieved.getProductVersion().getId()).isEqualTo(productMilestone.getProductVersion().getId());
        assertThat(created).isEqualToIgnoringGivenFields(productMilestone, "id", "productVersion");
        assertThat(retrieved).isEqualToIgnoringGivenFields(productMilestone, "id", "productVersion");
        assertThat(retrieved).isEqualTo(created);
    }

    @Test
    public void shouldFailToCreateExistingMilestone() throws IOException {
        // given
        ProductMilestone copyWithoutId = milestone.toBuilder().id(null).build();

        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when then
        assertThatThrownBy(() -> client.createNew(copyWithoutId)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldFailToCreateClosedMilestone() throws IOException {
        // given
        ProductMilestone closedMilestone = ProductMilestone.builder()
                .productVersion(productVersion)
                .version("1.0.0.ER2")
                .startingDate(Instant.ofEpochMilli(100_000))
                .plannedEndDate(Instant.ofEpochMilli(200_000))
                .endDate(Instant.ofEpochMilli(300_000))
                .build();

        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when then
        assertThatThrownBy(() -> client.createNew(closedMilestone)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldFailToCreateMilestoneWithMalformedVersion() throws IOException {
        // given
        ProductMilestone productMilestone = ProductMilestone.builder()
                .productVersion(productVersion)
                .version("1.0-ER1")
                .build();

        // when then
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());
        assertThatThrownBy(() -> client.createNew(productMilestone)).isInstanceOf(ClientException.class);
    }

    @Test
    public void testGetSpecific() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        ProductMilestone dto = client.getSpecific(milestoneId);

        assertThat(dto.getVersion()).isEqualTo("1.0.0.Build1"); // from DatabaseDataInitializer
        assertThat(dto.getProductVersion().getId()).isEqualTo(productVersion.getId()); // from DatabaseDataInitializer
    }

    @Test
    public void shouldUpdateProductMilestone() throws Exception {
        // given
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());
        final String version = "1.0.1.ER1";
        assertThat(milestone.getProductRelease()).isNotNull();
        ProductMilestone toUpdate = milestone.toBuilder().version(version).build();

        // when
        client.update(milestone.getId(), toUpdate);

        // then
        ProductMilestone retrieved = client.getSpecific(milestone.getId());
        assertThat(retrieved).isEqualTo(toUpdate);
        assertThat(retrieved).isEqualToIgnoringGivenFields(milestone, "version");
        assertThat(retrieved.getVersion()).isEqualTo(version);
    }

    @Test
    public void testValidateWithWrongPattern() throws RemoteResourceException {
        // with
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when
        final ValidationResponse response = client.validateVersion(
                VersionValidationRequest.builder().productVersionId("shouldn't matter").version("1.NOTVALID").build());

        // then
        assertThat(response.getIsValid()).isFalse();
        assertThat(response.getErrorType()).isEqualTo(ValidationErrorType.FORMAT);
        assertThat(response.getHints()).isNotEmpty();
    }

    @Test
    public void testValidateWithExistingVersion() throws RemoteResourceException {
        // with
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());
        ProductVersionClient productVersionClient = new ProductVersionClient(RestClientConfiguration.asUser());
        final ProductMilestone milestone = productVersionClient.getMilestones("100").iterator().next();

        // when
        final ValidationResponse response = client.validateVersion(
                VersionValidationRequest.builder().productVersionId("100").version(milestone.getVersion()).build());

        // then
        assertThat(response.getIsValid()).isFalse();
        assertThat(response.getErrorType()).isEqualTo(ValidationErrorType.DUPLICATION);
        assertThat(response.getHints()).isNotEmpty();
    }

    @Test
    public void testSuccessfulValidationWithNewVersion() throws RemoteResourceException {
        // with
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when
        final ValidationResponse response = client.validateVersion(
                VersionValidationRequest.builder().productVersionId("100").version("1.0.3.Build1").build());

        // then
        assertThat(response.getIsValid()).isTrue();
        assertThat(response.getErrorType()).isNull();
        assertThat(response.getHints()).isNullOrEmpty();
    }

    @Test
    public void testShouldFailToCloseWithNoBuilds() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());
        ProductMilestone newMilestone = ProductMilestone.builder()
                .productVersion(productVersion)
                .version("9.9.9.ER9")
                .startingDate(Instant.ofEpochMilli(100_000))
                .plannedEndDate(Instant.ofEpochMilli(200_000))
                .build();

        ProductMilestone created = client.createNew(newMilestone);
        assertThat(created.getId()).isNotEmpty();
        ProductMilestone retrieved = client.getSpecific(created.getId());
        assertThatThrownBy(() -> client.closeMilestone(retrieved.getId()))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void testGetBuilds() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Build> all = client.getBuilds(milestoneId, null);

        assertThat(all).hasSize(1);
    }

    @Test
    public void testGetDeliveredArtifacts() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> all = client.getDeliveredArtifacts(milestoneId);

        assertThat(all).hasSize(10);
        RemoteCollection<Artifact> built = client.getDeliveredArtifacts(
                milestoneId,
                Optional.empty(),
                Optional.of("md5==4af310bf0ef67bc7d143f35818ea1ed2"));

        assertThat(built).hasSize(1);
        Artifact builtArtifact = built.iterator().next();
        assertThat(builtArtifact.getIdentifier()).isEqualTo("demo:built-artifact1:jar:1.0");
    }

    @Test
    public void testGetDeliverableAnalyzerOperations() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<DeliverableAnalyzerOperation> all = client.getAllDeliverableAnalyzerOperations(milestoneId);

        assertThat(all).hasSize(5);

        RemoteCollection<DeliverableAnalyzerOperation> allInProgress = client.getAllDeliverableAnalyzerOperations(
                milestoneId,
                Optional.empty(),
                Optional.of("progressStatus==IN_PROGRESS"));

        assertThat(allInProgress).hasSize(1);
    }

    @Test
    public void testGetStatistics() throws ClientException {
        // given
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        ProductMilestoneDeliveredArtifactsStatistics expectedDeliveredArtifactsStats = ProductMilestoneDeliveredArtifactsStatistics
                .builder()
                .thisMilestone(2L) // builtArtifact1, builtArtifact9
                .otherMilestones(1L) // builtArtifact10
                .otherProducts(2L) // builtArtifact11, builtArtifact12
                .noMilestone(4L) // builtArtifact5, builtArtifact16a, builtArtifact16b, builtArtifact18
                .noBuild(1L) // importedArtifact2
                .build();

        EnumMap<ArtifactQuality, Long> expectedArtifactQualities = Maps
                .initEnumMapWithDefaultValue(ArtifactQuality.class, 0L);
        expectedArtifactQualities.put(ArtifactQuality.NEW, 8L);
        expectedArtifactQualities.put(ArtifactQuality.VERIFIED, 1L);
        expectedArtifactQualities.put(ArtifactQuality.IMPORTED, 1L);

        EnumMap<RepositoryType, Long> expectedRepositoryTypes = Maps
                .initEnumMapWithDefaultValue(RepositoryType.class, 0L);
        expectedRepositoryTypes.put(RepositoryType.MAVEN, 10L);

        ProductMilestoneStatistics expectedStats = ProductMilestoneStatistics.builder()
                .artifactsInMilestone(3L) // builtArtifact1, builtArtifact2, builtArtifact9
                .deliveredArtifactsSource(expectedDeliveredArtifactsStats)
                .artifactQuality(expectedArtifactQualities)
                .repositoryType(expectedRepositoryTypes)
                .build();

        // then
        ProductMilestoneStatistics actualStats = client.getStatistics(milestoneId);

        // assert
        assertThat(actualStats.getArtifactsInMilestone()).isEqualTo(expectedStats.getArtifactsInMilestone());
        assertThat(actualStats.getDeliveredArtifactsSource().getThisMilestone())
                .isEqualTo(expectedDeliveredArtifactsStats.getThisMilestone());
        assertThat(actualStats.getDeliveredArtifactsSource().getOtherMilestones())
                .isEqualTo(expectedDeliveredArtifactsStats.getOtherMilestones());
        assertThat(actualStats.getDeliveredArtifactsSource().getOtherProducts())
                .isEqualTo(expectedDeliveredArtifactsStats.getOtherProducts());
        assertThat(actualStats.getDeliveredArtifactsSource().getNoMilestone())
                .isEqualTo(expectedDeliveredArtifactsStats.getNoMilestone());
        assertThat(actualStats.getDeliveredArtifactsSource().getNoBuild())
                .isEqualTo(expectedDeliveredArtifactsStats.getNoBuild());
        assertThat(actualStats.getArtifactQuality()).isEqualTo(expectedArtifactQualities);
        assertThat(actualStats.getRepositoryType()).isEqualTo(expectedRepositoryTypes);
        assertThat(actualStats).isEqualTo(expectedStats);
    }

    @Test
    public void testCompareArtifactsDeliveredInMilestonesWithTwoMilestonesAndCommonPrefix() throws ClientException {
        // arrange
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        ParsedArtifact parsedArtifact1 = ParsedArtifact.builder()
                .id("117")
                .artifactVersion("1.0.redhat-a")
                .type("jar")
                .classifier(null)
                .build();
        ParsedArtifact parsedArtifact2 = ParsedArtifact.builder()
                .id("118")
                .artifactVersion("1.0.redhat-b")
                .type("jar")
                .classifier(null)
                .build();

        DeliveredArtifactInMilestones expectedDeliveredArtifactsInMilestones = DeliveredArtifactInMilestones.builder()
                .artifactIdentifierPrefix("demo:built-artifact16")
                .productMilestoneArtifacts(
                        Map.of("100", List.of(parsedArtifact1, parsedArtifact2), "101", List.of(parsedArtifact2)))
                .build();

        // act
        List<DeliveredArtifactInMilestones> actualDeliveredArtifactsInMilestonesList = client
                .compareArtifactVersionsDeliveredInMilestones(List.of(milestone.getId(), milestone2.getId()));

        // assert
        assertThat(actualDeliveredArtifactsInMilestonesList).hasSize(1);
        var actualDeliveredArtifactsInMilestones = actualDeliveredArtifactsInMilestonesList.iterator().next();

        assertThat(actualDeliveredArtifactsInMilestones.getArtifactIdentifierPrefix())
                .isEqualTo(expectedDeliveredArtifactsInMilestones.getArtifactIdentifierPrefix());
        assertThat(actualDeliveredArtifactsInMilestones.getProductMilestoneArtifacts())
                .isEqualTo(expectedDeliveredArtifactsInMilestones.getProductMilestoneArtifacts());
    }

    @Test
    public void testCompareArtifactsDeliveredInMilestonesWithTwoMilestonesAndNoCommonPrefix() throws ClientException {
        // arrange
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        // act
        List<DeliveredArtifactInMilestones> actualDeliveredArtifactsInMilestonesList = client
                .compareArtifactVersionsDeliveredInMilestones(List.of(milestone2.getId(), milestone3.getId()));

        // assert
        assertThat(actualDeliveredArtifactsInMilestonesList).hasSize(0);
    }

    @Test
    public void testCompareArtifactsDeliveredInMilestonesArtifactFoundInMultipleAnalysisOfSameMilestone()
            throws ClientException {
        // arrange
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        ParsedArtifact parsedArtifact = ParsedArtifact.builder()
                .id("114")
                .artifactVersion("1.0")
                .type("jar")
                .classifier(null)
                .build();

        DeliveredArtifactInMilestones expectedDeliveredArtifactsInMilestones = DeliveredArtifactInMilestones.builder()
                .artifactIdentifierPrefix("demo:built-artifact13")
                .productMilestoneArtifacts(Map.of("104", List.of(parsedArtifact), "105", List.of(parsedArtifact)))
                .build();

        // act
        List<DeliveredArtifactInMilestones> actualDeliveredArtifactsInMilestonesList = client
                .compareArtifactVersionsDeliveredInMilestones(List.of("104", "105"));

        // assert
        assertThat(actualDeliveredArtifactsInMilestonesList).hasSize(1);
        var actualDeliveredArtifactsInMilestones = actualDeliveredArtifactsInMilestonesList.iterator().next();

        assertThat(actualDeliveredArtifactsInMilestones.getArtifactIdentifierPrefix())
                .isEqualTo(expectedDeliveredArtifactsInMilestones.getArtifactIdentifierPrefix());
        assertThat(actualDeliveredArtifactsInMilestones.getProductMilestoneArtifacts())
                .isEqualTo(expectedDeliveredArtifactsInMilestones.getProductMilestoneArtifacts());
    }

    @Test
    public void testGetMilestonesSharingDeliveredArtifactsGraph() throws ClientException {
        // arrange
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        Set<String> expectedVerticesKeys = Set.of("100", "101", "102", "104", "105");
        List<Edge> expectedEdges = List.of(
                Edge.builder().source("101").target("100").cost(1).build(),
                Edge.builder().source("104").target("102").cost(2).build(),
                Edge.builder().source("105").target("102").cost(1).build(),
                Edge.builder().source("105").target("104").cost(1).build(),
                Edge.builder().source("102").target("100").cost(1).build());

        // act
        Graph<ProductMilestone> actualGraph = client
                .getMilestonesSharingDeliveredArtifactsGraph(milestone.getId().toString(), null);

        // assert
        assertThat(actualGraph.getVertices().keySet()).isEqualTo(expectedVerticesKeys);
        assertThat(actualGraph.getEdges()).isEqualTo(expectedEdges);
    }

    @Test
    public void testGetDeliveredArtifactsSharedInMilestones() throws ClientException {
        // arrange
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        List<String> expectedSharedDeliveredArtifactIds = List.of("114", "101");

        // act
        RemoteCollection<Artifact> actualList = client.getDeliveredArtifactsSharedInMilestones("102", "104");

        // assert
        assertThat(actualList).hasSize(2);
        assertThat(actualList).extracting(Artifact::getId).hasSameElementsAs(expectedSharedDeliveredArtifactIds);
    }

    @Test
    @Ignore
    // Update the count in testGetDeliverableAnalyzerOperations when the Ignore is removed
    public void shouldScratchFlagBeFalseImplicitly() throws ClientException {
        // given
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when
        client.analyzeDeliverables(
                milestoneId,
                DeliverablesAnalysisRequest.builder()
                        .deliverablesUrls(
                                List.of(
                                        "https://indy.psi.idk.com/api/content/maven/hosted/pnc-builds/com/jboss/super-important-1.jar"))
                        .build());

        // then
        bpm.getWireMockServer()
                .verify(
                        postRequestedFor(urlMatching(".*")).withRequestBody(
                                matching(".*super-important-1.jar.*")
                                        .and(matching(".*\"runAsScratchAnalysis\":false.*"))));
    }

    @Test
    @Ignore
    // Update the count in testGetDeliverableAnalyzerOperations when the Ignore is removed
    public void shouldScratchFlagBeFalse() throws ClientException {
        // given
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when
        client.analyzeDeliverables(
                milestoneId,
                DeliverablesAnalysisRequest.builder()
                        .deliverablesUrls(
                                List.of(
                                        "https://indy.psi.idk.com/api/content/maven/hosted/pnc-builds/com/jboss/super-important-2.jar"))
                        .runAsScratchAnalysis(false)
                        .build());

        // then
        bpm.getWireMockServer()
                .verify(
                        postRequestedFor(urlMatching(".*")).withRequestBody(
                                matching(".*super-important-2.jar.*")
                                        .and(matching(".*\"runAsScratchAnalysis\":false.*"))));
    }

    @Test
    @Ignore
    // Update the count in testGetDeliverableAnalyzerOperations when the Ignore is removed
    public void shouldScratchFlagBeTrue() throws ClientException {
        // given
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asUser());

        // when
        client.analyzeDeliverables(
                milestoneId,
                DeliverablesAnalysisRequest.builder()
                        .deliverablesUrls(
                                List.of(
                                        "https://indy.psi.idk.com/api/content/maven/hosted/pnc-builds/com/jboss/super-important-3.jar"))
                        .runAsScratchAnalysis(true)
                        .build());

        // then
        bpm.getWireMockServer()
                .verify(
                        postRequestedFor(urlMatching(".*")).withRequestBody(
                                matching(".*super-important-3.jar.*")
                                        .and(matching(".*\"runAsScratchAnalysis\":true.*"))));
    }
}
