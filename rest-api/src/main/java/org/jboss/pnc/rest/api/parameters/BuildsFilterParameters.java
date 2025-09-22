/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.api.parameters;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

/**
 * Parameters for filtering build lists.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
public class BuildsFilterParameters {

    /**
     * {@value SwaggerConstants#LATEST_BUILD_DESC}
     */
    @Parameter(description = SwaggerConstants.LATEST_BUILD_DESC)
    @QueryParam("latest")
    @DefaultValue("false")
    private boolean latest;

    /**
     * {@value SwaggerConstants#RUNNING_BUILDS_DESC}
     */
    @Parameter(description = SwaggerConstants.RUNNING_BUILDS_DESC)
    @QueryParam("running")
    @DefaultValue("false")
    private boolean running;

    /**
     * {@value SwaggerConstants#BC_NAME_FILTER_DESC}
     */
    @Parameter(description = SwaggerConstants.BC_NAME_FILTER_DESC)
    @QueryParam("buildConfigName")
    @DefaultValue("")
    private String buildConfigName;
}
