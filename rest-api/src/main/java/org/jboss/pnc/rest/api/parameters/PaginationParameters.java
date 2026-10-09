/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.api.parameters;

import javax.validation.constraints.Max;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import org.jboss.pnc.rest.configuration.Constants;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

/**
 * Parameters for pagination of results.
 * 
 * @author jbrazdil
 */
@Data
public class PaginationParameters {

    /**
     * {@value SwaggerConstants#PAGE_INDEX_DESCRIPTION}
     */
    @Parameter(description = SwaggerConstants.PAGE_INDEX_DESCRIPTION)
    @QueryParam(value = SwaggerConstants.PAGE_INDEX_QUERY_PARAM)
    @DefaultValue(value = SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE)
    @PositiveOrZero
    protected int pageIndex;

    /**
     * {@value SwaggerConstants#PAGE_SIZE_DESCRIPTION}
     */
    @Parameter(description = SwaggerConstants.PAGE_SIZE_DESCRIPTION)
    @QueryParam(value = SwaggerConstants.PAGE_SIZE_QUERY_PARAM)
    @DefaultValue(value = SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE)
    @PositiveOrZero
    @Max(value = Constants.MAX_PAGE_SIZE)
    protected int pageSize;

}
