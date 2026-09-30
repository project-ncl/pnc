/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.rest.annotation.RespondWithStatus;

@Provider
public class RespondWithStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(
            ContainerRequestContext containerRequestContext,
            ContainerResponseContext containerResponseContext) throws IOException {

        // for any 2xx status code, this gets activated
        // Skip filter for Http Method OPTIONS to not break CORS
        if (containerResponseContext.getStatus() / 100 == 2
                && containerResponseContext.getEntityAnnotations() != null) {
            for (Annotation annotation : containerResponseContext.getEntityAnnotations()) {

                if (annotation instanceof RespondWithStatus) {
                    containerResponseContext.setStatus(((RespondWithStatus) annotation).value().getStatusCode());
                    break;
                }
            }
        }
    }
}
