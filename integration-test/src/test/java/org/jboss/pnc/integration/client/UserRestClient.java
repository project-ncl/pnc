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
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.UserRest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRestClient extends AbstractRestClient<UserRest> {

    private static final String USER_REST_ENDPOINT = "/pnc-rest/rest/users";

    private static final String USER_BUILDS_ENDPOINT = "/%d/builds";

    public UserRestClient() {
        super(USER_REST_ENDPOINT, UserRest.class);
    }

    public RestResponse<List<BuildRecordRest>> allUserBuilds(int userId, boolean withValidation, int pageIndex, int pageSize, String rsql, String sort) {
        QueryParam rsqlQueryParam = null;
        QueryParam sortQueryParam = null;
        if (rsql != null) {
            rsqlQueryParam = new QueryParam("q", rsql);
        }
        if (sort != null) {
            sortQueryParam = new QueryParam("sort", sort);
        }
        QueryParam pageIndexQueryParam = new QueryParam("pageIndex", Integer.toString(pageIndex));
        QueryParam pageSizeQueryParam = new QueryParam("pageSize", "" + Integer.toString(pageSize));

        String requestUrl = USER_REST_ENDPOINT + String.format(USER_BUILDS_ENDPOINT, userId);

        Response response = get(requestUrl, rsqlQueryParam, sortQueryParam, pageIndexQueryParam, pageSizeQueryParam);

        logger.info("response {} ", response.prettyPrint());

        List<BuildRecordRest> object = new ArrayList<>();
        String responseBody = response.getBody().asString();

        // Response body may be null, if the query did not return any result! This would need to deliver a response with 204
        // status, with no errors
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                List<? extends Map> beforeMappingList = response.jsonPath().getList(CONTENT);
                ObjectMapper objectMapper = new ObjectMapper();
                for (Map obj : beforeMappingList) {
                    // because of the bug in RestAssured - we need to use another mapping library...
                    object.add(objectMapper.convertValue(obj, BuildRecordRest.class));
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

    public RestResponse<List<BuildRecordRest>> allUserBuilds(int userId) {
        return allUserBuilds(userId, true, 0, 50, null, null);
    }
}
