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
package org.jboss.pnc.remotecoordinator.maintenance;

import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.commonjava.indy.client.core.auth.IndyClientAuthenticator;
import org.commonjava.util.jhttpc.JHttpCException;
import org.jboss.pnc.auth.KeycloakServiceClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
