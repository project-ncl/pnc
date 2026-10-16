/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.facade.validation.BrewNotSupportedException;
import org.jboss.pnc.rest.api.endpoints.BuildPushesEndpoint;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
@Slf4j
public class BuildPushesEndpointImpl implements BuildPushesEndpoint {

    @Override
    public BuildPushReport getPushReport(String operationId) {
        throw new BrewNotSupportedException();
    }
}
