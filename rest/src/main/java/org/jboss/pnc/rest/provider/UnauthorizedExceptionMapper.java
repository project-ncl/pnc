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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBAccessException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<EJBAccessException> {
    private static final Logger log = LoggerFactory.getLogger(UnauthorizedExceptionMapper.class);

    @Override
    public Response toResponse(EJBAccessException exception) {
        log.info("A user is trying to access restricted resource", exception);

        String errorMessage = "Insufficient privileges: the required role to access the resource is missing in the provided JWT.";
        Response.ResponseBuilder builder = Response.status(FORBIDDEN);
        builder.entity(
                ErrorResponse.builder()
                        .errorType(exception.getClass().getSimpleName())
                        .errorMessage(errorMessage)
                        .details(exception.getMessage())
                        .build())
                .type(MediaType.APPLICATION_JSON);

        return builder.build();
    }
}
