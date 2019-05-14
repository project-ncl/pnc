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
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import java.util.Collection;

public class BuildRecordRestClient extends AbstractRestClient<BuildRecordRest> {

    public static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/build-records/";

    public BuildRecordRestClient() {
        super(BUILD_RECORD_REST_ENDPOINT, BuildRecordRest.class);
    }

    public BuildRecordRestClient(AuthenticateAs authenticateAs) {
        super(BuildRecordRestClient.BUILD_RECORD_REST_ENDPOINT, BuildRecordRest.class, authenticateAs);
    }

    public Collection<ArtifactRest> getBuiltArtifacts(int buildRecordId) {
        Response response = getRestClient().get(BUILD_RECORD_REST_ENDPOINT + buildRecordId + "/built-artifacts");
        return getCollection(response, ArtifactRest.class, true);
    }

    public void setBuiltArtifacts(Integer buildRecordId, Collection<Integer> artifactIds) {
        getRestClient().put(BUILD_RECORD_REST_ENDPOINT + buildRecordId + "/built-artifacts", artifactIds);
    }

    public Collection<ArtifactRest> getDependentArtifacts(int buildRecordId) {
        Response response = getRestClient().get(BUILD_RECORD_REST_ENDPOINT + buildRecordId + "/dependency-artifacts");
        return getCollection(response, ArtifactRest.class, true);
    }

    public void setDependentArtifacts(Integer buildRecordId, Collection<Integer> artifactIds) {
        getRestClient().put(BUILD_RECORD_REST_ENDPOINT + buildRecordId + "/dependency-artifacts", artifactIds);
    }

    public Collection<BuildRecordRest> getAllByStatusAndLogContaining(
            BuildStatus status,
            String logSubstring,
            boolean withValidation) {
        QueryParam statusParam = new QueryParam("status", status.toString());
        QueryParam searchParam = new QueryParam("search", logSubstring);

        Response response = getRestClient().get(BUILD_RECORD_REST_ENDPOINT + "with-status-and-log", searchParam, statusParam);
        logger.info("response {} ", response.prettyPrint());

        return getCollection(response, BuildRecordRest.class, withValidation);
    }
}
