/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.mock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.remotecoordinator.rexclient.RexQueueHttpClient;
import org.jboss.pnc.rex.dto.responses.LongResponse;

@ApplicationScoped
@Alternative
@RestClient
public class RexQueueHttpClientMock implements RexQueueHttpClient {
    @Override
    public void setConcurrent(@NotNull @Min(0L) Long amount) {
    }

    @Override
    public void setConcurrentNamed(String name, @NotNull @Min(0L) Long amount) {
    }

    @Override
    public LongResponse getConcurrent() {
        return LongResponse.builder().number(5L).build();
    }

    @Override
    public LongResponse getConcurrentNamed(String name) {
        return LongResponse.builder().number(5L).build();
    }

    @Override
    public LongResponse getRunning() {
        return LongResponse.builder().number(0L).build();
    }

    @Override
    public LongResponse getRunningNamed(String name) {
        return LongResponse.builder().number(0L).build();
    }
}
