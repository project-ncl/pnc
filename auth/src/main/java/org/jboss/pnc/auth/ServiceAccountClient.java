/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

/**
 * Interface to take care of service account authentication.
 */
public interface ServiceAccountClient {

    /**
     * Specify the value for the HTTP header: "Authorization"
     *
     * Will support Bearer (OIDC), Basic (LDAP), and anything else in the future
     * 
     * @return
     */
    String getAuthHeaderValue();
}
