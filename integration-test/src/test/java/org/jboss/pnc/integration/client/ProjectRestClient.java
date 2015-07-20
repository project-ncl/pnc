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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

public class ProjectRestClient extends AbstractRestClient {

    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    private static final String PROJECT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/projects/%d";

    private int projectId;

    private ProjectRestClient() {

    }

    public static ProjectRestClient firstNotNull() throws IOException, ConfigurationParseException {
        ProjectRestClient ret = new ProjectRestClient();
        ret.initAuth();

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(PROJECT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> ret.projectId = Integer.valueOf(value)));
        return ret;
    }

    public ClientResponse createNew(ProjectRest projectRest) {
        Response post = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).body(projectRest).when().post(PROJECT_REST_ENDPOINT);

        String location = post.getHeader("Location");
        Integer idFromLocation = null;
        if(location != null) {
            idFromLocation = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        }
        return new ClientResponse(this, post.getStatusCode(), idFromLocation);
    }

}
