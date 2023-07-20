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
import org.jboss.pnc.demo.data.DatabaseDataInitializer;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.requests.validation.VersionValidationRequest;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.DeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.enums.ValidationErrorType;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.api.parameters.ProductMilestoneCloseParameters;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jboss.pnc.demo.data.DatabaseDataInitializer.PNC_PRODUCT_MILESTONE3;
import static org.jboss.pnc.demo.data.DatabaseDataInitializer.PNC_PRODUCT_NAME;

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
    public void shouldGetMilestoneRelease() throws IOException, RemoteResourceException {
        // given
        ProductClient productClient = new ProductClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<Product> products = productClient
                .getAll(Optional.empty(), Optional.of("name==\"" + PNC_PRODUCT_NAME + "\""));
        Product product = products.iterator().next();
        Map<String, ProductVersionRef> productVersions = product.getProductVersions();
        Optional<ProductVersionRef> productVersion = productVersions.values()
                .stream()
                .filter(pv -> pv.getVersion().equals(DatabaseDataInitializer.PNC_PRODUCT_VERSION_1))
                .findAny();

        ProductVersionClient productVersionClient = new ProductVersionClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<ProductMilestone> milestones = productVersionClient.getMilestones(
                productVersion.get().getId(),
                Optional.empty(),
                Optional.of("version==\"" + PNC_PRODUCT_MILESTONE3 + "\""));
        ProductMilestone milestone = milestones.iterator().next();

        ProductMilestoneClient milestoneClient = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<ProductMilestoneCloseResult> milestoneReleases = milestoneClient
                .getCloseResults(milestone.getId(), null);
        // then
        Assert.assertEquals(3, milestoneReleases.size());
        // make sure the result is ordered by date
        Instant previous = Instant.EPOCH;
        for (Iterator<ProductMilestoneCloseResult> iter = milestoneReleases.iterator(); iter.hasNext();) {
            ProductMilestoneCloseResult next = iter.next();
            logger.debug("MilestoneRelease id: {}, StartingDate: {}.", next.getId(), next.getStartingDate());
            Assert.assertTrue("Wong milestone releases order.", next.getStartingDate().isAfter(previous));
            previous = next.getStartingDate();
        }

        // when
        ProductMilestoneCloseParameters filter = new ProductMilestoneCloseParameters();
        filter.setLatest(true);
        RemoteCollection<ProductMilestoneCloseResult> latestMilestoneRelease = milestoneClient
                .getCloseResults(milestone.getId(), filter);
        // then
        Assert.assertEquals(1, latestMilestoneRelease.getAll().size());
        // the latest one in demo data has status SUCCEEDED
        Assert.assertEquals(MilestoneCloseStatus.SUCCEEDED, latestMilestoneRelease.iterator().next().getStatus());
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

        assertThat(all).hasSize(4);
    }

    @Test
    public void testGetDeliverableAnalyzerOperations() throws ClientException {
        ProductMilestoneClient client = new ProductMilestoneClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<DeliverableAnalyzerOperation> all = client.getAllDeliverableAnalyzerOperations(milestoneId);

        assertThat(all).hasSize(2);

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
        DeliveredArtifactsStatistics expectedDeliveredArtifactsStats = DeliveredArtifactsStatistics.builder()
                .thisMilestone(2) // builtArtifact1, builtArtifact9
                .previousMilestones(1) // builtArtifact10
                .otherProducts(2) // TODO 3, TODO 4
                .noMilestone(1) // builtArtifact5
                .noBuild(1) // importedArtifact2
                .build();
        ProductMilestoneStatistics expectedStats = ProductMilestoneStatistics.builder()
                .artifactsInMilestone(3) // builtArtifact1, builtArtifact2, builtArtifact9
                .deliveredArtifactsSource(expectedDeliveredArtifactsStats)
                // .artifactQuality()
                // .repositoryType()
                .build();

        // then
        ProductMilestoneStatistics actualStats = client.getStatistics(milestoneId);

        // assert
        // assertThat(actualStats).isEqualTo(expectedStats);
    }
}
