/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client;

import org.jboss.pnc.dto.Project;
import org.jboss.pnc.rest.api.endpoints.ProjectEndpoint;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class ProjectClient {

    static final String BASE_URL = "http://localhost:8080/";

    static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";

    ResteasyClient client = new ResteasyClientBuilder().build();

    ResteasyWebTarget target = client.target(getEndpointUrl());

    private ConnectionInfo connectionInfo;

    public ProjectClient(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getEndpointUrl() {
        return "http://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + PROJECT_REST_ENDPOINT;
    }

    private ProjectEndpoint getEndpoint() {
        return target.proxy(ProjectEndpoint.class);
    }

    Optional<Project> getSpecific(int id) {
        Response response = getEndpoint().getSpecific(id);
        Optional<Project> result = processResponse(response);
        return result;
    }

    Optional<Project> createNew(Project project) {
        Response response = getEndpoint().createNew(project);
        Optional<Project> result = processResponse(response);
        return result;
    }

    private Optional<Project> processResponse(Response response) {
        Optional<Project> result;
        if (response.getStatus() == 200) {
            result = Optional.ofNullable(response.readEntity(Project.class));
        } else {
            result = Optional.empty();
        }
        response.close();
        return result;
    }
}
