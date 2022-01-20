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

import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jboss.pnc.rest.configuration.SwaggerConstants;

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
