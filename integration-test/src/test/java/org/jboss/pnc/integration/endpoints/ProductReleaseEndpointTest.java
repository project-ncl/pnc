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
import org.jboss.pnc.client.ProductReleaseClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.enums.SupportLevel;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductReleaseEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductReleaseEndpointTest.class);

    private static Product product;
    private static ProductVersion productVersion;
    private static ProductMilestone milestone;
    private static String releaseId;

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
        milestone = productVersionClient.getMilestones(productVersion.getId()).iterator().next();
        releaseId = productVersionClient.getReleases(productVersion.getId()).iterator().next().getId();
    }

    @Test
    public void testCreateNew() throws ClientException {
        ProductReleaseClient client = new ProductReleaseClient(RestClientConfiguration.asUser());

        ProductRelease productRelease = ProductRelease.builder()
                .productVersion(productVersion)
                .productMilestone(milestone)
                .version("1.0.1.GA")
                .build();
        ProductRelease created = client.createNew(productRelease);
        assertThat(created.getId()).isNotEmpty();
        ProductRelease retrieved = client.getSpecific(created.getId());

        assertThat(created.getProductMilestone().getId()).isEqualTo(productRelease.getProductMilestone().getId());
        assertThat(retrieved.getProductMilestone().getId()).isEqualTo(productRelease.getProductMilestone().getId());
        assertThat(created.getProductVersion().getId()).isEqualTo(productRelease.getProductVersion().getId());
        assertThat(retrieved.getProductVersion().getId()).isEqualTo(productRelease.getProductVersion().getId());
        assertThat(created).isEqualToIgnoringGivenFields(productRelease, "id", "productMilestone", "productVersion");
        assertThat(retrieved).isEqualToIgnoringGivenFields(productRelease, "id", "productMilestone", "productVersion");
        assertThat(retrieved).isEqualTo(created);
    }

    @Test
    public void testGetSpecific() throws ClientException {
        ProductReleaseClient client = new ProductReleaseClient(RestClientConfiguration.asAnonymous());

        ProductRelease dto = client.getSpecific(releaseId);

        assertThat(dto.getVersion()).isEqualTo("1.0.0.GA"); // from DatabaseDataInitializer
        assertThat(dto.getProductVersion().getId()).isEqualTo(productVersion.getId()); // from DatabaseDataInitializer
    }

    @Test
    public void testUpdate() throws ClientException {
        // given
        ProductReleaseClient client = new ProductReleaseClient(RestClientConfiguration.asUser());
        final String version = "2.1.1.GA";
        ProductRelease dto = client.getSpecific(releaseId);
        ProductRelease toUpdate = dto.toBuilder().version(version).build();

        // when
        client.update(releaseId, toUpdate);

        // then
        ProductRelease retrieved = client.getSpecific(dto.getId());

        assertThat(retrieved).isEqualTo(toUpdate);
        assertThat(retrieved).isEqualToIgnoringGivenFields(dto, "version");
        assertThat(retrieved.getVersion()).isEqualTo(version);
    }

    @Test
    public void testGetAllSupportLevel() throws ClientException {
        ProductReleaseClient client = new ProductReleaseClient(RestClientConfiguration.asAnonymous());
        Set<SupportLevel> all = client.getSupportLevels();

        assertThat(all).hasSameSizeAs(SupportLevel.values()).contains(SupportLevel.values());
    }

}
