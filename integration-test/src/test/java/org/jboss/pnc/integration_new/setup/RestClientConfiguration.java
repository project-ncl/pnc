/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new.setup;

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.integration.env.IntegrationTestEnv;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestClientConfiguration {

    public enum AuthenticateAs {
        NONE, USER, SYSTEM_USER
    }

    public static Configuration getConfiguration(AuthenticateAs authAs) {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        if (AuthenticateAs.SYSTEM_USER.equals(authAs)) {
            builder.basicAuth(new Configuration.BasicAuth("system", "system.1234"));
        } else if (AuthenticateAs.USER.equals(authAs)) {
            builder.basicAuth(new Configuration.BasicAuth("user", "pass.1234"));
        }
        builder.protocol("http");
        builder.host("localhost");
        builder.port(IntegrationTestEnv.getHttpPort());
        return builder.build();
    }

    public static Configuration asAnonymous(){
        return getConfiguration(AuthenticateAs.NONE);
    }

    public static Configuration asUser() {
        return getConfiguration(AuthenticateAs.USER);
    }

    public static Configuration asSystem(){
        return getConfiguration(AuthenticateAs.SYSTEM_USER);
    }

}
