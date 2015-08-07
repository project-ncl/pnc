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

import java.io.IOException;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;

import com.jayway.restassured.response.Response;

public class BuildConfigurationRestClient extends AbstractRestClient {

    private static final String BUILD_CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";

    private Integer buildConfigurationId;

    private BuildConfigurationRestClient() {

    }

    public static BuildConfigurationRestClient empty() throws IOException, ConfigurationParseException {
        BuildConfigurationRestClient ret = new BuildConfigurationRestClient();
        ret.initAuth();
        return ret;
    }

    public static BuildConfigurationRestClient firstNotNull() throws IOException, ConfigurationParseException {
        BuildConfigurationRestClient ret = empty();

        ret.get(BUILD_CONFIGURATION_REST_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> ret.buildConfigurationId = Integer.valueOf(value)));

        return ret;
    }

    public ClientResponse get(int id) throws IOException, ConfigurationParseException {
        Response response = get(BUILD_CONFIGURATION_REST_ENDPOINT + id);
        return new ClientResponse(this, response.getStatusCode(), null);
    }

    public ClientResponse createNew(BuildConfigurationRest buildConfiguration) {
        Response post = post(BUILD_CONFIGURATION_REST_ENDPOINT, buildConfiguration);
        return new ClientResponse(this, post.getStatusCode(), getLocationFromHeader(post));
    }


    public ClientResponse update(Integer buildConfigurationId, BuildConfigurationRest buildConfiguration) {
        Response post = put(BUILD_CONFIGURATION_REST_ENDPOINT + buildConfigurationId, buildConfiguration);
        return new ClientResponse(this, post.getStatusCode(), null);
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }
}
