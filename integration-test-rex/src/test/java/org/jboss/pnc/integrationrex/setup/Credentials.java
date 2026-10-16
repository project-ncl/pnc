/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex.setup;

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
