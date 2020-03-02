/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import java.util.List;

import static java.lang.String.*;

public class ProjectRestClient extends AbstractRestClient<ProjectRest> {

    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    private static final String BUILD_CONFIGURATION_SUB_ENDPOINT = PROJECT_REST_ENDPOINT + "%d/build-configurations";

    public ProjectRestClient() {
        super(PROJECT_REST_ENDPOINT, ProjectRest.class);
    }

    public RestResponse<List<BuildConfigurationRest>> getBuildConfigurations(
            int id,
            boolean withValidation,
            int pageIndex,
            int pageSize,
            String rsql,
            String sort) {
        return all(
                BuildConfigurationRest.class,
                format(BUILD_CONFIGURATION_SUB_ENDPOINT, id),
                withValidation,
                pageIndex,
                pageSize,
                rsql,
                sort);
    }

    public RestResponse<List<BuildConfigurationRest>> getBuildConfigurations(int id, boolean withValidation) {
        return getBuildConfigurations(id, withValidation, 0, 50, null, null);
    }
}
