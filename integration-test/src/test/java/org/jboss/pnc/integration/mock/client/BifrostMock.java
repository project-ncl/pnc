/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock.client;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.jboss.pnc.api.bifrost.dto.Line;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.api.bifrost.enums.Direction;
import org.jboss.pnc.api.bifrost.enums.Format;
import org.jboss.pnc.api.bifrost.rest.Bifrost;
import org.jboss.pnc.api.dto.ComponentVersion;

@ApplicationScoped
public class BifrostMock implements Bifrost {

    @Override
    public Response getAllLines(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Format format,
            Integer maxLines,
            Integer tailLines,
            Integer batchSize,
            Integer batchDelay,
            boolean follow,
            String timeoutProbeString) {
        return null;
    }

    @Override
    public List<Line> getLines(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Integer maxLines,
            Integer batchSize) throws IOException {
        return null;
    }

    @Override
    public MetaData getMetaData(
            String matchFilters,
            String prefixFilters,
            Line afterLine,
            Direction direction,
            Integer maxLines,
            Integer batchSize) throws IOException {
        return null;
    }

    @Override
    public ComponentVersion getVersion() {
        return null;
    }
}
