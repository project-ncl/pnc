/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.parameters;

import javax.ws.rs.QueryParam;

import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Parameters for queriing and sorting lists.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageParameters extends PaginationParameters {

    /**
     * {@value SwaggerConstants#SORTING_DESCRIPTION}
     */
    @Parameter(description = SwaggerConstants.SORTING_DESCRIPTION)
    @QueryParam(SwaggerConstants.SORTING_QUERY_PARAM)
    private String sort;

    /**
     * {@value SwaggerConstants#QUERY_DESCRIPTION}
     */
    @Parameter(description = SwaggerConstants.QUERY_DESCRIPTION)
    @QueryParam(SwaggerConstants.QUERY_QUERY_PARAM)
    private String q;

}
