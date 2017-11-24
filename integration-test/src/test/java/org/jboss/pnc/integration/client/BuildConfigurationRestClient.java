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
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.spi.BuildOptions;

public class BuildConfigurationRestClient extends AbstractRestClient<BuildConfigurationRest> {

    private static final String BUILD_CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";

    public BuildConfigurationRestClient() {
        super(BUILD_CONFIGURATION_REST_ENDPOINT, BuildConfigurationRest.class);
    }

    public RestResponse<BuildConfigurationRest> trigger(int id, BuildOptions options) {
        Response response = request().when()
                .queryParam("temporaryBuild", options.isTemporaryBuild())
                .queryParam("forceRebuild", options.isForceRebuild())
                .queryParam("buildDependencies", options.isBuildDependencies())
                .queryParam("keepPodOnFailure", options.isKeepPodOnFailure())
                .queryParam("timestampAlignment", options.isTimestampAlignment())
                .post(collectionUrl + id + "/build");

        response.then().statusCode(200);

        try {
            return new RestResponse<>(response, null);
        } catch (Exception e) {
            throw new AssertionError("JSON unmarshalling error", e);
        }
    }

}
