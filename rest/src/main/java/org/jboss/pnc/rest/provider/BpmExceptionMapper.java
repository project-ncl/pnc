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

import org.jboss.pnc.bpm.BpmException;
import org.jboss.pnc.bpm.InvalidReferenceException;
import org.jboss.pnc.bpm.MissingInternalReferenceException;
import org.jboss.pnc.bpm.NoEntityException;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BpmExceptionMapper implements ExceptionMapper<BpmException> {

    private static final Logger logger = LoggerFactory.getLogger(BpmExceptionMapper.class);

    @Override
    public Response toResponse(BpmException e) {
        Response.StatusType status = Response.Status.BAD_REQUEST;
        if (e instanceof MissingInternalReferenceException) {
            status = Response.Status.CONFLICT;
            logger.debug("Invalid reference in a REST call.", e);
        } else if (e instanceof InvalidReferenceException) {
            status = Response.Status.CONFLICT;
            logger.debug("Unexpected entity data provided in a REST call.", e);
        } else if (e instanceof NoEntityException) {
            logger.debug("Entity not found", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            logger.warn("An error occurred when processing REST call", e);
        }
        return Response.status(status).entity(new ErrorResponse(e)).build();
    }
}
