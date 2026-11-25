/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class KeycloakClientRetryTest {

    @Test
    public void aConnectionResetIsRetriedOnce() {
        AtomicInteger calls = new AtomicInteger();

        String result = KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            if (calls.incrementAndGet() == 1) {
                throw new RuntimeException(
                        "Failed to send request - Connection reset",
                        new SocketException("Connection reset"));
            }
            return "token";
        });

        assertThat(result).isEqualTo("token");
        assertThat(calls).hasValue(2);
    }

    @Test
    public void aSecondIoErrorIsThrown() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RuntimeException("Failed to send request", new SocketException("Connection reset"));
        })).hasMessage("Failed to send request");
        assertThat(calls).hasValue(2);
    }

    @Test
    public void errorsAnsweredByTheServerAreNotRetried() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RuntimeException("Invalid client credentials [unauthorized_client]");
        })).hasMessage("Invalid client credentials [unauthorized_client]");
        assertThat(calls).hasValue(1);
    }
}
