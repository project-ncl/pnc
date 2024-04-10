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
package org.jboss.pnc.client;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.function.Supplier;

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
