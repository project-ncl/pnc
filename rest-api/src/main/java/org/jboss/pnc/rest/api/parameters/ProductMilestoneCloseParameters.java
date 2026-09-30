/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.parameters;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

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
