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
package org.jboss.pnc.integration.client;

import com.jayway.restassured.response.Response;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;

import java.io.IOException;
import java.util.Optional;

public class ProductVersionRestClient extends AbstractRestClient {

    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product-versions/";

    private ProductVersionRestClient() {

    }

    public static ProductVersionRestClient instance() throws IOException, ConfigurationParseException {
        ProductVersionRestClient ret = new ProductVersionRestClient();
        ret.initAuth();
        return ret;
    }

    public Optional<ProductVersionRest> firstNotNull() {
        return Optional.ofNullable(get(PRODUCT_VERSION_REST_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().body().as(ProductVersionRest[].class)[0]);
    }

    public Optional<ProductVersionRest> get(int id) throws IOException, ConfigurationParseException {
        return Optional.ofNullable(
                get(PRODUCT_VERSION_REST_ENDPOINT + id).then().statusCode(200).extract().body().as(ProductVersionRest.class));
    }

    public Optional<ProductVersionRest> createNew(ProductVersionRest productVersion) {
        return Optional.ofNullable(
                post(PRODUCT_VERSION_REST_ENDPOINT, productVersion)
                        .then()
                        .statusCode(201)
                        .extract().body().as(ProductVersionRest.class));
    }


    public ClientResponse update(Integer buildConfigurationId, ProductVersionRest productVersion) {
        Response post = put(PRODUCT_VERSION_REST_ENDPOINT + buildConfigurationId, productVersion);
        return new ClientResponse(this, post.getStatusCode(), null);
    }
}
