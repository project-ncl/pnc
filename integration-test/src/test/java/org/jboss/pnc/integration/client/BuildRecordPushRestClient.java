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
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.rest.restmodel.response.ResultRest;

public class BuildRecordPushRestClient extends AbstractRestClient<ResultRest[]> {

    public static final String BUILD_RECORD_PUSH_REST_ENDPOINT = "/pnc-rest/rest/build-record-push/";

    public BuildRecordPushRestClient() {
        super(BUILD_RECORD_PUSH_REST_ENDPOINT, ResultRest[].class);
    }

    public RestResponse<ResultRest[]> push(BuildRecordPushRequestRest buildRecordPushRequestRest) {
        return post(BUILD_RECORD_PUSH_REST_ENDPOINT, buildRecordPushRequestRest, true);
    }

    public RestResponse<ResultRest[]> complete(BuildRecordPushResultRest pushResultRest) {
        return post(BUILD_RECORD_PUSH_REST_ENDPOINT + pushResultRest.getBuildRecordId() + "/complete/", pushResultRest, false);
    }

    public BuildRecordPushResultRest getStatus(Integer buildRecordId) {
        Response response = getRestClient().get(BUILD_RECORD_PUSH_REST_ENDPOINT + "status/" + buildRecordId);
        return response.then().extract().body().jsonPath().getObject("", BuildRecordPushResultRest.class);
    }
}
