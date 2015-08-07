/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.client.ClientResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductVersionRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ProductVersionRestClient productVersionRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() throws Exception {
        productVersionRestClient = ProductVersionRestClient.instance();
    }

    @Test
    public void shouldGetSpecificProductVersion() throws Exception {
        //when
        int productVersionId = productVersionRestClient.firstNotNull().get().getId();
        Optional<ProductVersionRest> clientResponse = productVersionRestClient.get(productVersionId);

        //then
        assertThat(clientResponse.isPresent()).isEqualTo(true);
    }

    @Test
    public void shouldCreateNewProductVersion() throws Exception {
        //given
        int productId = ProductRestClient.instance().firstNotNull().get().getId();

        ProductVersionRest productVersion = new ProductVersionRest();
        productVersion.setProductId(productId);
        productVersion.setVersion("1.0");

        //when
        Optional<ProductVersionRest> clientResponse = productVersionRestClient.createNew(productVersion);

        //then
        assertThat(clientResponse.isPresent()).isEqualTo(true);
        assertThat(clientResponse.get().getId()).isNotNegative();
    }

    @Test
    public void shouldUpdateProductVersion() throws Exception {
        //given
        ProductVersionRest productVersionRest = productVersionRestClient.firstNotNull().get();
        productVersionRest.setVersion("2.0");

        //when
        ClientResponse updateResponse = productVersionRestClient.update(productVersionRest.getId(), productVersionRest);
        Optional<ProductVersionRest> returnedProductVersion = productVersionRestClient.get(productVersionRest.getId());

        //then
        assertThat(updateResponse.getHttpCode()).isEqualTo(200);
        assertThat(returnedProductVersion.get().getVersion()).isEqualTo("2.0");
    }
}
