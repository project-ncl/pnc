/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.resteasy.spi.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Mapper that catches all exception and extracts the http status code from the JAXRS/RESTEASY runtime exception.
 * Status code extraction is required for proper response error codes eg. 404.
 */
@Provider
public class AllOtherExceptionsMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Response toResponse(Exception e) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        Response response = null;

        if (e instanceof WebApplicationException) {
            response = ((WebApplicationException) e).getResponse();
            logger.debug("An exception occurred when processing REST response", e);
        } else if (e instanceof Failure) { //Resteasy support
            Failure failure = ((Failure)e);
            if (failure.getErrorCode() > 0) {
                status = failure.getErrorCode();
            }
            response = failure.getResponse();
            logger.debug("An exception occurred when processing REST response", e);
        } else {
            logger.error("An exception occurred when processing REST response", e);
        }

        Response.ResponseBuilder builder;

        if (response != null) {
            builder = Response.status(response.getStatus());
//            b.entity(response.getEntity());

            //copy headers
            for (String headerName : response.getMetadata().keySet()) {
                List<Object> headerValues = response.getMetadata().get(headerName);
                for (Object headerValue : headerValues) {
                    builder.header(headerName, headerValue);
                }
            }
        } else {
            builder = Response.status(status);
        }

        builder.entity(new ErrorResponseRest(e)).type(MediaType.APPLICATION_JSON);

        return builder.build();

    }
}
