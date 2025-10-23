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
import org.jboss.pnc.dto.response.Validation;
import org.jboss.pnc.dto.response.Validations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Validations.Builder detailBuilder = Validations.builder();
        for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
            String attribute = constraintViolation.getPropertyPath().toString();
            String message = constraintViolation.getMessage();
            Object value = constraintViolation.getInvalidValue();
            detailBuilder.validation(new Validation(attribute, message, value));
        }

        Response.ResponseBuilder builder = Response.status(BAD_REQUEST);
        return builder.entity(
                new ErrorResponse("VALIDATION", "Constraint violation: " + e.getMessage(), detailBuilder.build()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
