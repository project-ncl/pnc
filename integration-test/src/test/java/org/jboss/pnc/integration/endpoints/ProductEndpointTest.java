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

import java.net.URISyntaxException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;

import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductVersion;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Collections;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.client.patch.ProductPatchBuilder;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductEndpointTest.class);
    private static String productId;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        ProductClient client = new ProductClient(RestClientConfiguration.asAnonymous());
        productId = client.getAll().iterator().next().getId();
    }

    @Before
    public void before() throws RemoteResourceException {
        ProductClient client = new ProductClient(RestClientConfiguration.asAnonymous());
        System.out.println("All things: ");
        for (Product product : client.getAll()) {
            System.out.println("  " + product);
        }
        System.out.println(".");
    }

    @Test
    @InSequence(10)
    public void testGetAll() throws RemoteResourceException {
        ProductClient client = new ProductClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Product> all = client.getAll();

        assertThat(all).hasSize(1); // from DatabaseDataInitializer
    }

    @Test
    @InSequence(20)
    public void testCreateNew() throws ClientException {
        ProductClient client = new ProductClient(RestClientConfiguration.asUser());

        Product product = Product.builder()
                .name("New Product")
                .abbreviation("NP")
                .description("The newst product of them all")
                .productVersions(Collections.emptyMap())
                .build();
        Product created = client.createNew(product);
        assertThat(created.getId()).isNotEmpty();
        Product retrieved = client.getSpecific(created.getId());

        assertThat(created).isEqualToIgnoringGivenFields(product, "id");
        assertThat(retrieved).isEqualToIgnoringGivenFields(product, "id");
        assertThat(retrieved).isEqualTo(created);
    }

    @Test
    public void shouldFailToSaveProductWithSpaceInAbbreviation() {
        ProductClient client = new ProductClient(RestClientConfiguration.asUser());
        Product product = Product.builder()
                .name("New Ab re via ted product")
                .abbreviation("abb re viation")
                .description("The newst product of them all. Now with spaces.")
                .build();
        try {
            client.createNew(product);
            fail("Exception should be thrown");
        } catch (ClientException ex) {
            // OK
        }
    }

    @Test
    @InSequence(30)
    public void shouldFailToAddConflictingProduct() throws URISyntaxException, ClientException {
        ProductClient client = new ProductClient(RestClientConfiguration.asUser());

        Product product = Product.builder()
                .name("The Same Thing")
                .abbreviation("TST")
                .description("Let's keep doing the same thing over and over. Nobody will notice.")
                .build();
        Product created = client.createNew(product);
        assertThat(created.getId()).isNotEmpty();
        try {
            client.createNew(product);
            fail("Exception should be thrown");
        } catch (ClientException ex) {
            // OK
        }
    }

    @Test
    public void testGetSpecific() throws ClientException {
        ProductClient client = new ProductClient(RestClientConfiguration.asAnonymous());

        Product dto = client.getSpecific(productId);

        assertThat(dto.getName()).isEqualTo("Project Newcastle Demo Product"); // from DatabaseDataInitializer
        assertThat(dto.getDescription()).isEqualTo("Example Product for Project Newcastle Demo"); // from
                                                                                                  // DatabaseDataInitializer
    }

    @Test
    public void testUpdate() throws ClientException {
        ProductClient client = new ProductClient(RestClientConfiguration.asUser());

        Product dto = client.getSpecific(productId);
        Product toUpdate = dto.toBuilder().name("Updated name").build();
        client.update(productId, toUpdate);
        Product retrieved = client.getSpecific(dto.getId());

        assertThat(retrieved).isEqualTo(toUpdate);
        assertThat(retrieved).isEqualToIgnoringGivenFields(dto, "name");
        assertThat(retrieved.getName()).isEqualTo("Updated name");
    }

    @Test
    public void testPatch() throws ClientException, PatchBuilderException {
        // given
        ProductClient client = new ProductClient(RestClientConfiguration.asUser());
        Product original = client.getSpecific(productId);
        final String newProductAbbreviation = "newAbb";

        // when
        client.patch(productId, new ProductPatchBuilder().replaceAbbreviation(newProductAbbreviation));
        Product patched = client.getSpecific(original.getId());

        // then
        assertThat(patched).isEqualToIgnoringGivenFields(original, "abbreviation");
        assertThat(patched.getAbbreviation()).isEqualTo(newProductAbbreviation);
    }

    @Test
    public void testGetProductVersions() throws ClientException {
        ProductClient client = new ProductClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<ProductVersion> all = client.getProductVersions(productId);

        assertThat(all).hasSize(2).allMatch(v -> v.getProduct().getId().equals(productId));
    }

}
