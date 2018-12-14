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


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ConnectionInfo {

    String host;
    Integer port;

    BasicAuth basicAuth;

    String bearerToken;

    private ConnectionInfo(Builder builder) {
        host = builder.host;
        port = builder.port;
        basicAuth = builder.basicAuth;
        bearerToken = builder.bearerToken;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class BasicAuth {
        String username;
        String password;

        public BasicAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public static final class Builder {

        private String host;

        private Integer port;

        private BasicAuth basicAuth;

        private String bearerToken;

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder basicAuth(BasicAuth basicAuth) {
            this.basicAuth = basicAuth;
            return this;
        }

        public Builder bearerToken(String bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        public ConnectionInfo build() {
            return new ConnectionInfo(this);
        }
    }
}
