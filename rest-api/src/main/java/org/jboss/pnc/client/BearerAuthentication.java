/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.io.IOException;
import java.util.function.Supplier;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
public class BearerAuthentication implements ClientRequestFilter {

    private Supplier<String> tokenSupplier;

    /**
     * Not used anymore in pnc-rest-client. Candidate for removal in PNC 3.0+
     * 
     * @param token
     */
    @Deprecated
    public BearerAuthentication(String token) {
        this.tokenSupplier = () -> token;
    }

    public BearerAuthentication(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    /**
     * Not used anymore in pnc-rest-client. Candidate for removal in PNC 3.0+
     * 
     * @param token
     */
    @Deprecated
    public void setToken(String token) {
        this.tokenSupplier = () -> token;
    }

    public void setTokenSupplier(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSupplier.get());
    }
}
