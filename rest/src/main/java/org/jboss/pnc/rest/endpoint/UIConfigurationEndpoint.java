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
