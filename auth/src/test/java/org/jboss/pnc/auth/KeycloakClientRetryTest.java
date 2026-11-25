/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NoHttpResponseException;
import org.jboss.pnc.auth.keycloakutil.util.HttpUtil.RequestFailedException;
import org.junit.Test;

public class KeycloakClientRetryTest {

    @Test
    public void aDroppedConnectionIsRetriedOnce() {
        for (IOException dropped : new IOException[] {
                new SocketException("Connection reset"),
                new NoHttpResponseException("sso failed to respond") }) {
            AtomicInteger calls = new AtomicInteger();

            String result = KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
                if (calls.incrementAndGet() == 1) {
                    throw new RequestFailedException("Failed to send request - " + dropped.getMessage(), dropped);
                }
                return "token";
            });

            assertThat(result).isEqualTo("token");
            assertThat(calls).hasValue(2);
        }
    }

    @Test
    public void aSecondFailureIsThrown() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RequestFailedException("Failed to send request", new SocketException("Connection reset"));
        })).hasMessage("Failed to send request");
        assertThat(calls).hasValue(2);
    }

    @Test
    public void timeoutsAreNotRetried() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RequestFailedException("Failed to send request", new SocketTimeoutException("Read timed out"));
        })).isInstanceOf(RequestFailedException.class);
        assertThat(calls).hasValue(1);
    }

    @Test
    public void errorsAnsweredByTheServerAreNotRetried() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RuntimeException("Invalid client credentials [unauthorized_client]");
        })).hasMessage("Invalid client credentials [unauthorized_client]");
        // an error answer whose body cannot be read has an I/O cause, but it was answered by the server
        assertThatThrownBy(() -> KeycloakClient.retryOnceOnIoError("https://sso/token", () -> {
            calls.incrementAndGet();
            throw new RuntimeException("Failed to read error response", new SocketException("Connection reset"));
        })).hasMessage("Failed to read error response");
        assertThat(calls).hasValue(2);
    }
}
