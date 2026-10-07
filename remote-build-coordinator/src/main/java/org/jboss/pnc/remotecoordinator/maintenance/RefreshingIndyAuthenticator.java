/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.commonjava.indy.client.core.auth.IndyClientAuthenticator;
import org.commonjava.util.jhttpc.JHttpCException;
import org.jboss.pnc.auth.KeycloakServiceClient;

@ApplicationScoped
public class RefreshingIndyAuthenticator extends IndyClientAuthenticator {

    @Inject
    KeycloakServiceClient tokenClient;

    @Override
    public HttpClientBuilder decorateClientBuilder(HttpClientBuilder builder) throws JHttpCException {
        builder.addInterceptorFirst((HttpRequestInterceptor) (httpRequest, httpContext) -> {
            final Header header = new BasicHeader("Authorization", String.format("Bearer %s", getFreshAccessToken()));
            httpRequest.addHeader(header);
        });
        return builder;
    }

    private String getFreshAccessToken() {
        return tokenClient.getAuthToken();
    }
}
