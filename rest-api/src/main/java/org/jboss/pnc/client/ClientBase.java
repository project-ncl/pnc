/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClientBase {

    protected final String BASE_PATH = "/pnc-rest/rest";

    protected final ResteasyClient client;

    protected final ResteasyWebTarget target;

    protected ClientBase(ConnectionInfo connectionInfo) {
        client = new ResteasyClientBuilder().build();
        target = client.target(connectionInfo.getProtocol() + "://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + BASE_PATH);
        ConnectionInfo.BasicAuth basicAuth = connectionInfo.getBasicAuth();
        if (basicAuth != null) {
            target.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        }
        String bearerToken = connectionInfo.getBearerToken();
        if (bearerToken != null && !bearerToken.equals("")) {
            target.register(new BearerAuthentication(bearerToken));
        }
    }


}
