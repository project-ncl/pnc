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
package org.jboss.pnc.integration.setup;

import java.util.Base64;
import java.util.function.BiFunction;

/**
 *
 * @author jbrazdil
 */
public enum Credentials {

    ADMIN("admin", "user.1234"),
    SYSTEM_USER("system", "system.1234"),
    USER("demo-user", "pass.1234"),
    USER2("user", "pass.1234"),
    NONE(null, null);

    private final String name;
    private final String pass;

    private Credentials(String name, String pass) {
        this.name = name;
        this.pass = pass;
    }

    private String encodedCredentials() {
        return Base64.getEncoder().encodeToString((name + ":" + pass).getBytes());
    }

    public <T> T createAuthHeader(BiFunction<String, String, T> creator) {
        return creator.apply("Authorization", "Basic " + encodedCredentials());
    }

    public <T> T passCredentials(BiFunction<String, String, T> creator) {
        return creator.apply(name, pass);
    }

}
