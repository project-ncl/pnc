/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Endpoint for serving the Web UI runtime configuration parameters.
 *
 * @author Alex Creasy
 */
@Api(value = "/ui-configuration", description = "PNC Web UI configuration parameters")
@Path("/ui-configuration")
@Produces(MediaType.APPLICATION_JSON)
public class UIConfigurationEndpoint {

    private UIModuleConfig uiConfig;

    public UIConfigurationEndpoint() {}

    @Inject
    public UIConfigurationEndpoint(Configuration configuration) throws ConfigurationParseException {
        this.uiConfig = configuration.getModuleConfig(new PncConfigProvider<UIModuleConfig>(UIModuleConfig.class));
    }

    @ApiOperation(value = "Gets the Web UI configuration")
    @GET
    public Response get() {
        return Response.ok().entity(JsonOutputConverterMapper.apply(uiConfig)).build();
    }

}
