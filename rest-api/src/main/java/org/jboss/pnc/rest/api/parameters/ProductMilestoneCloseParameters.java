/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

@Data
public class ProductMilestoneCloseParameters {

    /**
     * {@value SwaggerConstants#LATEST_MILESTONE_CLOSE_DESC}
     */
    @Parameter(description = SwaggerConstants.LATEST_MILESTONE_CLOSE_DESC)
    @QueryParam("latest")
    @DefaultValue("false")
    private boolean latest;
    /**
     * {@value SwaggerConstants#RUNNING_MILESTONE_CLOSE_DESC}
     */
    @Parameter(description = SwaggerConstants.RUNNING_MILESTONE_CLOSE_DESC)
    @QueryParam("running")
    @DefaultValue("false")
    private boolean running;
}
