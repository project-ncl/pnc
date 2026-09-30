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
