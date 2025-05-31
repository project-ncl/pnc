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
package org.jboss.pnc.rest.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.rest.api.endpoints.BuildPushesEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
@Slf4j
public class BuildPushesEndpointImpl implements BuildPushesEndpoint {

    @Inject
    private BrewPusher brewPusher;

    @Override
    public BuildPushReport getPushReport(String operationId) {
        BuildPushReport brewPushResult = brewPusher.getBrewPushReport(operationId);
        if (brewPushResult == null) {
            throw new NotFoundException("Build Push Report with id " + operationId + " not found.");
        }
        return brewPushResult;
    }
}
