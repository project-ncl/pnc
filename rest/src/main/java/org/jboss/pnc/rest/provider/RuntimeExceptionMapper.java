/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    @Context
    private Providers providers;

    @Override
    public Response toResponse(RuntimeException e) {
        Throwable t = e.getCause();
        ExceptionMapper mapper = providers.getExceptionMapper(t.getClass());
        log.debug(
                "Unwrapping " + t.getClass().getSimpleName()
                        + " from RuntimeException and passing in it to its appropriate ExceptionMapper");

        if (mapper != null) {
            return mapper.toResponse(t);
        }
        return providers.getExceptionMapper(Exception.class).toResponse(e);
    }
}
