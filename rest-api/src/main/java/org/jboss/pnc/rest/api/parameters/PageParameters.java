package org.jboss.pnc.rest.api.parameters;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class PageParameters {

    @Parameter(description = PAGE_INDEX_DESCRIPTION)
    @QueryParam(PAGE_INDEX_QUERY_PARAM)
    @DefaultValue(PAGE_INDEX_DEFAULT_VALUE)
    private int pageIndex;

    @Parameter(description = PAGE_SIZE_DESCRIPTION)
    @QueryParam(PAGE_SIZE_QUERY_PARAM)
    @DefaultValue(PAGE_SIZE_DEFAULT_VALUE)
    private int pageSize;

    @Parameter(description = SORTING_DESCRIPTION)
    @QueryParam(SORTING_QUERY_PARAM)
    private String sort;

    @Parameter(description = QUERY_DESCRIPTION)
    @QueryParam(QUERY_QUERY_PARAM)
    private String q;

}
