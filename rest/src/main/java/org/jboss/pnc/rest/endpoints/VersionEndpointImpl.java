/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.environmentdriver.BuildInformationConstants;
import org.jboss.pnc.rest.api.endpoints.VersionEndpoint;

@ApplicationScoped
public class VersionEndpointImpl implements VersionEndpoint {

    @Override
    public ComponentVersion getCurrentVersion() {
        return ComponentVersion.builder()
                .name("PNC-Orch")
                .version(BuildInformationConstants.VERSION)
                .commit(BuildInformationConstants.COMMIT_HASH)
                .builtOn(ZonedDateTime.parse(BuildInformationConstants.BUILD_TIME))
                .build();
    }
}
