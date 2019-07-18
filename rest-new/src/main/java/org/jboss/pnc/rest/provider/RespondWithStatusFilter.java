/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.rest.annotation.RespondWithStatus;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;

@Provider
public class RespondWithStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {

        // for any 2xx status code, this gets activated
        // Skip filter for Http Method OPTIONS to not break CORS
        if (containerResponseContext.getStatus() / 100 == 2 &&
            containerResponseContext.getEntityAnnotations() != null) {
            for (Annotation annotation : containerResponseContext.getEntityAnnotations()) {

                if (annotation instanceof RespondWithStatus) {
                    containerResponseContext.setStatus(((RespondWithStatus) annotation).value().getStatusCode());
                    break;
                }
            }
        }
    }
}
