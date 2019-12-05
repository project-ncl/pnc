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
package org.jboss.pnc.rest.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.invoke.MethodHandles;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;

@Provider
public class ValidationExceptionExceptionMapper implements ExceptionMapper<DTOValidationException> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Response toResponse(DTOValidationException e) {
        Response.StatusType status = Response.Status.BAD_REQUEST;
        if (e instanceof ConflictedEntryException) {
            status = Response.Status.CONFLICT;
            logger.debug("A ConflictedEntry error occurred when processing REST call", e);
        }if (e instanceof EmptyEntityException) {
            logger.debug("Entity not found", e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            logger.warn("A validation error occurred when processing REST call", e);
        }
        return Response
                .status(status)
                .entity(new ErrorResponse(e, e.getRestModelForException().orElse(null)))
                .build();
    }
}
