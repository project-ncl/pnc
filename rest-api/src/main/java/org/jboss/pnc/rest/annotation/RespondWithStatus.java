/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;
import javax.ws.rs.core.Response;

/**
 * Annotation to override default 2xx response HTTP status.
 * 
 * @author jbrazdil
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface RespondWithStatus {

    Response.Status value();
}
