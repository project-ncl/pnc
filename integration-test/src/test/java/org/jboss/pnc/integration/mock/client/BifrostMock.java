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
package org.jboss.pnc.integration.mock.client;

import org.jboss.pnc.api.bifrost.dto.Line;
import org.jboss.pnc.api.bifrost.dto.MetaData;
import org.jboss.pnc.api.bifrost.enums.Direction;
import org.jboss.pnc.api.bifrost.enums.Format;
import org.jboss.pnc.api.bifrost.rest.Bifrost;
import org.jboss.pnc.api.dto.ComponentVersion;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
