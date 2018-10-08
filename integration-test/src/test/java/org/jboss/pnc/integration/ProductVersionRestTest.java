/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.ProductRestClient;
import org.jboss.pnc.integration.client.ProductVersionRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductVersionRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static AtomicBoolean isInitialised = new AtomicBoolean(false);

    private static ProductVersionRestClient productVersionRestClient;
    private static BuildConfigurationSetRestClient buildConfigurationSetRestClient;
    private static ProductRestClient productRestClient;

    private static ProductRest productRest1;
    private static ProductVersionRest productVersionRest1;
    private static ProductVersionRest productVersionRest2;

    private BuildConfigurationSetRest buildConfigurationSetRest1;
    private BuildConfigurationSetRest buildConfigurationSetRest2;
    private BuildConfigurationSetRest buildConfigurationSetRest3;



    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(productVersionRestClient == null) {
            productVersionRestClient = new ProductVersionRestClient();
        }
        if(buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }
        if(productVersionRestClient == null) {
            productVersionRestClient = new ProductVersionRestClient();
        }
        if(productRestClient == null) {
            productRestClient = new ProductRestClient();
        }

        if (!isInitialised.getAndSet(true)) {
            productRest1 = new ProductRest();
            productRest1.setName("product-version-rest-test-product-1");
            productRest1.setAbbreviation("PVR");
            productRest1 = productRestClient.createNew(productRest1).getValue();

            productVersionRest1 = new ProductVersionRest();
            productVersionRest1.setVersion("1.0");
            productVersionRest1.setProductId(productRest1.getId());
            productVersionRest1 = productVersionRestClient.createNew(productVersionRest1).getValue();

            productVersionRest2 = new ProductVersionRest();
            productVersionRest2.setVersion("2.0");
            productVersionRest2.setProductId(productRest1.getId());
            productVersionRest2 = productVersionRestClient.createNew(productVersionRest2).getValue();
        }

        buildConfigurationSetRest1 = new BuildConfigurationSetRest();
        buildConfigurationSetRest1.setName("product-version-rest-test-bcset-1");
        buildConfigurationSetRest1.setProductVersionId(productVersionRest1.getId());
        buildConfigurationSetRest1 = buildConfigurationSetRestClient.createNew(buildConfigurationSetRest1).getValue();

        buildConfigurationSetRest2 = new BuildConfigurationSetRest();
        buildConfigurationSetRest2.setName("product-version-rest-test-bcset-2");
        buildConfigurationSetRest2.setProductVersionId(productVersionRest2.getId());
        buildConfigurationSetRest2 = buildConfigurationSetRestClient.createNew(buildConfigurationSetRest2).getValue();

        buildConfigurationSetRest3 = new BuildConfigurationSetRest();
        buildConfigurationSetRest3.setName("product-version-rest-test-bcset-3");
        buildConfigurationSetRest3 = buildConfigurationSetRestClient.createNew(buildConfigurationSetRest3).getValue();
    }

    @After
    public void after() {
        buildConfigurationSetRestClient.delete(buildConfigurationSetRest1.getId());
        buildConfigurationSetRestClient.delete(buildConfigurationSetRest2.getId());
        buildConfigurationSetRestClient.delete(buildConfigurationSetRest3.getId());
    }

    @Test
    public void shouldGetSpecificProductVersion() throws Exception {
        //when
        int productVersionId = productVersionRestClient.firstNotNull().getValue().getId();
        RestResponse<ProductVersionRest> clientResponse = productVersionRestClient.get(productVersionId);

        //then
        assertThat(clientResponse.hasValue()).isEqualTo(true);
    }

    @Test
    public void shouldFailGracefullyOnNonExistentProduct() throws Exception {
        // given
        int nonExistentProductId = 384583;
        ProductVersionRest version = new ProductVersionRest();
        version.setProductId(nonExistentProductId);
        version.setVersion("1.2");
        // when
        RestResponse<ProductVersionRest> response = productVersionRestClient.createNew(version, false);
        // then
        response.getRestCallResponse().then().statusCode(400);
    }

    @Test
    public void shouldCreateNewProductVersion() throws Exception {
        //given
        int productId = productVersionRestClient.firstNotNull().getValue().getProductId();
        
        ProductVersionRest productVersion = new ProductVersionRest();
        productVersion.setProductId(productId);
        productVersion.setVersion("99.0");

        //when
        RestResponse<ProductVersionRest> clientResponse = productVersionRestClient.createNew(productVersion);

        //then
        assertThat(clientResponse.hasValue()).isEqualTo(true);
        assertThat(clientResponse.getValue().getId()).isNotNegative();
    }
    

    @Test
    public void shouldGenerateBrewTagWhenCreatingProductVersion() throws Exception {
        //given
        int productVersionId = productVersionRestClient.firstNotNull().getValue().getProductId();
        ProductRest product = productRestClient.get(productVersionId).getValue();
        
        ProductVersionRest productVersion = new ProductVersionRest();
        productVersion.setProductId(productVersionId);
        productVersion.setVersion("98.0");

        //when
        RestResponse<ProductVersionRest> clientResponse = productVersionRestClient.createNew(productVersion);

        //then
        assertTrue(clientResponse.hasValue());
        assertEquals(product.getAbbreviation().toLowerCase() + "-98.0" + "-pnc",
                clientResponse.getValue().getAttributes().get(ProductVersion.ATTRIBUTE_KEY_BREW_TAG_PREFIX));
    }

    @Test
    public void shouldUpdateProductVersion() throws Exception {
        //given
        ProductVersionRest productVersionRest = productVersionRestClient.firstNotNull().getValue();
        productVersionRest.setVersion("100.0");

        //when
        RestResponse<ProductVersionRest> updateResponse = productVersionRestClient.update(productVersionRest.getId(),
                productVersionRest);

        //then
        assertThat(updateResponse.hasValue()).isEqualTo(true);
        assertThat(updateResponse.getValue().getVersion()).isEqualTo("100.0");
    }

    @Test
    public void shouldUpdateBuildConfigurationSets() {
        //given
        List<BuildConfigurationSetRest> buildConfigurationSetRests = new LinkedList<>();
        buildConfigurationSetRests.add(buildConfigurationSetRest3);

        //when
        RestResponse<List<BuildConfigurationSetRest>> response  = productVersionRestClient.updateBuildConfigurationSets(
                productVersionRest1.getId(), buildConfigurationSetRests);

        //then
        assertThat(response.getValue().stream().map(BuildConfigurationSetRest::getId).collect(Collectors.toList()))
                .containsOnly(buildConfigurationSetRest3.getId());
    }

    @Test
    public void shouldNotUpdateBuildConfigurationSetsWhenOneIsAlreadyAsssociatedWithAnotherProductVersion() {

        //given
        List<BuildConfigurationSetRest> buildConfigurationSetRests = new LinkedList<>();
        buildConfigurationSetRests.add(buildConfigurationSetRest2);

        //when
        RestResponse<List<BuildConfigurationSetRest>> response  = productVersionRestClient.updateBuildConfigurationSets(
                productVersionRest1.getId(), buildConfigurationSetRests, false);

        //then
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(409);
        assertThat(response.getValue().stream().map(BuildConfigurationSetRest::getId).collect(Collectors.toList()))
                .containsOnly(buildConfigurationSetRest1.getId());
    }

    @Test
    public void shouldNotUpdateBuildConfigurationSetsWithNonExistantBuildConfigurationSet() {
        //given
        List<BuildConfigurationSetRest> buildConfigurationSetRests = new LinkedList<>();
        buildConfigurationSetRests.add(new BuildConfigurationSetRest(BuildConfigurationSet.Builder.newBuilder().id(600).name("i-dont-exist").build()));


        //when
        RestResponse<List<BuildConfigurationSetRest>> response  = productVersionRestClient.updateBuildConfigurationSets(
                productVersionRest1.getId(), buildConfigurationSetRests, false);

        //then
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(400);
        assertThat(response.getValue().stream().map(BuildConfigurationSetRest::getId).collect(Collectors.toList()))
                .containsOnly(buildConfigurationSetRest1.getId());
    }


}