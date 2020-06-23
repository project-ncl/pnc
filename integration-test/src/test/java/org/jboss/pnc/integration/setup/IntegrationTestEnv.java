/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.setup;

/**
 * This is the Util class to get test environment, like the http-port, etc.
 *
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 *
 */
public final class IntegrationTestEnv {

    private IntegrationTestEnv() {
        // static utils methods only.
    }

    /**
     * Gets Test Http Port.
     * 
     * @return the http port for REST end points, default to 8080.
     */
    public static int getHttpPort() {
        int defaultHttpPort = Integer.getInteger("jboss.http.port", 8080);
        int offset = Integer.getInteger("jboss.port.offset", 0);
        return defaultHttpPort + offset;
    }

}
