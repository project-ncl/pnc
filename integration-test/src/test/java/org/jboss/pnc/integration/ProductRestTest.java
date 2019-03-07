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
package org.jboss.pnc.integration;

import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.ProductRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/23/16
 * Time: 2:13 PM
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductRestTest extends AbstractTest {
    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ProductRestClient productRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(productRestClient == null) {
            productRestClient = new ProductRestClient();
        }
    }

    @Test
    public void shouldAddProduct() throws RestValidationException, URISyntaxException {
        ProductRest dto = new ProductRest();
        dto.setName(randomAlphabetic(20));
        dto.setAbbreviation(randomAlphabetic(3));
        Response response = productRestClient.createNew(dto).getRestCallResponse();
        response.then().statusCode(201);
    }

    @Test
    public void shouldFailToSaveProductWithSpaceInAbbreviation() {
        ProductRest dto = new ProductRest();
        dto.setName(randomAlphabetic(20));
        dto.setAbbreviation("abb re viation");
        Response response = productRestClient.createNew(dto, false).getRestCallResponse();
        response.then().statusCode(400);
    }

    @Test
    public void shouldFailToAddConflictingProduct() throws RestValidationException, URISyntaxException {
        ProductRest dto = new ProductRest();
        dto.setName(randomAlphabetic(20));
        dto.setAbbreviation(randomAlphabetic(3));

        productRestClient.createNew(dto).getRestCallResponse().then().statusCode(201);
        productRestClient.createNew(dto, false).getRestCallResponse().then().statusCode(409);
    }
}
