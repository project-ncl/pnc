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
 * Parameters for filtering group builds lists.
 *
 * @author Adam Krídl &lt;akridl@redhat.com&gt;
 */
@Data
public class GroupBuildsFilterParameters {

    /**
     * {@value SwaggerConstants#LATEST_GROUP_BUILD_DESC}
     */
    @Parameter(description = SwaggerConstants.LATEST_GROUP_BUILD_DESC)
    @QueryParam("latest")
    @DefaultValue("false")
    private boolean latest;
}
