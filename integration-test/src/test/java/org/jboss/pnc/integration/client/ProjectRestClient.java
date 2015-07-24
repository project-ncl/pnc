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
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import java.io.IOException;

public class ProjectRestClient extends AbstractRestClient {

    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";

    private int projectId;

    private ProjectRestClient() {

    }

    public static ProjectRestClient firstNotNull() throws IOException, ConfigurationParseException {
        ProjectRestClient ret = new ProjectRestClient();
        ret.initAuth();

        ret.get(PROJECT_REST_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> ret.projectId = Integer.valueOf(value)));

        return ret;
    }

    public ClientResponse createNew(ProjectRest projectRest) {
        Response response = post(PROJECT_REST_ENDPOINT, projectRest);

        return new ClientResponse(this, response.getStatusCode(), getLocationFromHeader(response));
    }

    public ClientResponse update(Integer id, ProjectRest projectRest) {
        Response response = put(PROJECT_REST_ENDPOINT + id, projectRest);

        return new ClientResponse(this, response.getStatusCode(), null);
    }

    public int getProjectId() {
        return projectId;
    }
}
