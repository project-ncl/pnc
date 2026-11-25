/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.AccessTokenResponse;

/**
 * Through the real {@code HttpUtil.doPost}: a token request answered with a TCP reset (what a pooled connection the
 * server had dropped gives) is sent again.
 */
public class KeycloakClientConnectionResetTest {

    private static final String TOKEN_RESPONSE = "{\"access_token\":\"token\",\"token_type\":\"Bearer\",\"expires_in\":300}";

    private ServerSocket server;
    private Thread serverThread;
    private final AtomicInteger connections = new AtomicInteger();

    @Before
    public void start() throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        serverThread = new Thread(() -> {
            while (!server.isClosed()) {
                try (Socket socket = server.accept()) {
                    readRequest(socket);
                    if (connections.incrementAndGet() == 1) {
                        socket.setSoLinger(true, 0); // close with a reset
                    } else {
                        byte[] body = TOKEN_RESPONSE.getBytes(StandardCharsets.UTF_8);
                        OutputStream out = socket.getOutputStream();
                        out.write(
                                ("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: " + body.length
                                        + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
                        out.write(body);
                        out.flush();
                    }
                } catch (IOException e) {
                    // closed
                }
            }
        }, "reset-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @After
    public void stop() throws Exception {
        server.close();
        serverThread.join(5000);
    }

    @Test
    public void aResetTokenRequestIsSentAgain() {
        AccessTokenResponse response = KeycloakClient.getAuthTokensBySecret(
                "http://127.0.0.1:" + server.getLocalPort(),
                "realm",
                "client",
                "secret",
                false);

        assertThat(response.getToken()).isEqualTo("token");
        assertThat(connections).hasValue(2);
    }

    /** Reads the request line, headers and body, so that the client has sent the whole request. */
    private static void readRequest(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        int length = 0;
        for (String line = in.readLine(); line != null && !line.isEmpty(); line = in.readLine()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                length = Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }
        for (int i = 0; i < length; i++) {
            in.read();
        }
    }
}
