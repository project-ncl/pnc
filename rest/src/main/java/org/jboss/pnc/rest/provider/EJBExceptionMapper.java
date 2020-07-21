/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    private static final Logger log = LoggerFactory.getLogger(EJBExceptionMapper.class);

    @Context
    private Providers providers;

    @Override
    public Response toResponse(EJBException exception) {
        Throwable t = exception.getCause();
        ExceptionMapper mapper = providers.getExceptionMapper(t.getClass());
        log.debug(
                "Unwrapping " + t.getClass().getSimpleName()
                        + " from EJBException and passing in it to its appropriate ExceptionMapper");

        if (mapper != null) {
            return mapper.toResponse(t);
        } else {
            log.error("Could not find exception mapper for exception " + t.getClass().getSimpleName(), t);

            Response.ResponseBuilder builder = Response.status(INTERNAL_SERVER_ERROR);
            return builder.entity(new ErrorResponse(exception)).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
