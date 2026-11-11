/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;

public class ReqourClientTest {

    @Test
    public void queryValuesAreEncodedCompletely() {
        String uri = ReqourClient
                .withValues(UriBuilder.fromUri("http://reqour/scm/diff"), "base", List.of("feature/{x}", "a b&c=d"))
                .build()
                .toString();

        assertThat(uri).isEqualTo("http://reqour/scm/diff?base=feature%2F%7Bx%7D&base=a+b%26c%3Dd");
    }

    @Test
    public void reqourTimeoutsAreGatewayTimeoutsAndOtherFailuresGatewayErrors() {
        javax.ws.rs.WebApplicationException timeout = ReqourClient.unreachable(
                "/scm/diff",
                new javax.ws.rs.ProcessingException(new RuntimeException(new java.net.SocketTimeoutException("read"))));
        assertThat(timeout.getResponse().getStatus()).isEqualTo(504);
        assertThat(timeout.getMessage()).isEqualTo("Reqour did not answer in time.");

        javax.ws.rs.WebApplicationException refused = ReqourClient
                .unreachable(
                        "/scm/diff",
                        new javax.ws.rs.ProcessingException(new java.net.ConnectException("refused")));
        assertThat(refused.getResponse().getStatus()).isEqualTo(502);
        assertThat(refused.getMessage()).isEqualTo("Reqour cannot be reached.");
    }
}
