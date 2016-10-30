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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class BuildConfigurationSetRestClient extends AbstractRestClient<BuildConfigurationSetRest> {

    public static final String BUILD_CONFIGURATION_SET_REST_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/";
    public static final String BUILD_CONFIGURATIONS_SUB_ENDPOINT = BUILD_CONFIGURATION_SET_REST_ENDPOINT + "%d/build-configurations";

    public BuildConfigurationSetRestClient() {
        super(BUILD_CONFIGURATION_SET_REST_ENDPOINT, BuildConfigurationSetRest.class);
    }

    public RestResponse<BuildConfigurationSetRest> trigger(int id, boolean rebuildAll) {
        Response response = request().when().queryParam("rebuildAll", rebuildAll).post(collectionUrl + id + "/build");

        response.then().statusCode(200);

        try {
            return new RestResponse(response, null);
        } catch (Exception e) {
            throw new AssertionError("JSON unmarshalling error", e);
        }
    }

    public RestResponse<List<BuildConfigurationRest>> getBuildConfigurations(int id, boolean withValidation, int pageIndex, int pageSize, String rsql, String sort) {
        QueryParam rsqlQueryParam = null;
        QueryParam sortQueryParam = null;
        if(rsql != null) {
            rsqlQueryParam = new QueryParam("q", rsql);
        }
        if(sort != null) {
            sortQueryParam = new QueryParam("sort", sort);
        }
        QueryParam pageIndexQueryParam = new QueryParam("pageIndex", Integer.toString(pageIndex));
        QueryParam pageSizeQueryParam = new QueryParam("pageSize", "" + Integer.toString(pageSize));
        Response response = get(format(BUILD_CONFIGURATIONS_SUB_ENDPOINT, id), rsqlQueryParam, sortQueryParam, pageIndexQueryParam, pageSizeQueryParam);

        logger.info("response {} ", response.prettyPrint());

        List<BuildConfigurationRest> object = new ArrayList<>();
        String responseBody = response.getBody().asString();

        // Response body may be null, if the query did not return any result! This would need to deliver a response with 204
        // status, with no errors
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                List<? extends Map> beforeMappingList = response.jsonPath().getList(CONTENT);
                ObjectMapper objectMapper = new ObjectMapper();
                for (Map obj : beforeMappingList) {
                    // because of the bug in RestAssured - we need to use another mapping library...
                    object.add(objectMapper.convertValue(obj, BuildConfigurationRest.class));
                }
            } catch (Exception e) {
                logger.error("JSON unmarshalling error", e);
                if (withValidation) {
                    throw new AssertionError("JSON unmarshalling error", e);
                }
            }
        }
        if (withValidation) {
            if (!object.isEmpty()) {
                response.then().statusCode(200);
            } else {
                response.then().statusCode(204);
            }
        }

        return new RestResponse<>(response, object);
    }

    public RestResponse<List<BuildConfigurationRest>> getBuildConfigurations(int id, boolean withValidation) {
        return getBuildConfigurations(id, withValidation, 0, 50, null, null);
    }

    public RestResponse<List<BuildConfigurationRest>> updateBuildConfigurations(int id, List<BuildConfigurationRest> buildConfigurationRests, boolean withValidation) {
        Response response = put(format(BUILD_CONFIGURATIONS_SUB_ENDPOINT, id), buildConfigurationRests);

        if (withValidation) {
            response.then().statusCode(200);
        }

        return new RestResponse<>(response, getBuildConfigurations(id, false).getValue());
    }
}
