/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.wildfly.security.http.oidc.OidcSecurityContext;

public class CallerTokenTest {

    @Test
    public void theValidatedOidcTokenIsTheCallerToken() {
        HttpServletRequest request = request(
                Map.of(OidcSecurityContext.class.getName(), new OidcSecurityContext("caller-token", null, null, null)));

        assertThat(CallerToken.of(request)).contains("caller-token");
    }

    @Test
    public void callersWithoutAnOidcTokenHaveNone() {
        assertThat(CallerToken.of(request(Map.of()))).isEmpty();
        assertThat(
                CallerToken.of(
                        request(
                                Map.of(
                                        OidcSecurityContext.class.getName(),
                                        new OidcSecurityContext(" ", null, null, null)))))
                .isEmpty();
        assertThat(CallerToken.of(null)).isEmpty();
    }

    /** A request with only attributes: the Authorization header itself is never read. */
    private static HttpServletRequest request(Map<String, Object> attributes) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                CallerTokenTest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getAttribute")) {
                        return attributes.get((String) args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
