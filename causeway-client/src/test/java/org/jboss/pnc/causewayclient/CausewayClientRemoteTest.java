/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.causewayclient;

import java.io.IOException;

import org.jboss.pnc.api.causeway.dto.untag.TaggedBuild;
import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.auth.DefaultLDAPServiceClient;
import org.jboss.pnc.auth.DefaultServiceAccountClient;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.auth.LDAPServiceClient;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.mock.common.GlobalModuleGroupMock;
import org.jboss.pnc.mock.common.SystemConfigMock;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category(DebugTest.class)
public class CausewayClientRemoteTest {

    KeycloakServiceClient keycloakServiceClient;
    LDAPServiceClient ldapServiceClient;
    ServiceAccountClient serviceAccountClient;

    private CausewayClient causewayClient;

    public CausewayClientRemoteTest() throws IOException, ConfigurationParseException {
        SystemConfig systemConfig = SystemConfigMock.withKeycloakServiceAccount();
        ldapServiceClient = new DefaultLDAPServiceClient(systemConfig);
        keycloakServiceClient = new DefaultKeycloakServiceClient(systemConfig);
        serviceAccountClient = new DefaultServiceAccountClient(systemConfig, keycloakServiceClient, ldapServiceClient);

        GlobalModuleGroup globalConfig = GlobalModuleGroupMock.get();
        causewayClient = new DefaultCausewayClient(globalConfig);
    }

    @Test
    public void shouldUntagBrewBuilds() {
        UntagRequest untagRequest = prepareUntagRequest("", 1);
        boolean accepted = causewayClient.untagBuild(untagRequest, serviceAccountClient.getAuthHeaderValue());

        Assert.assertTrue(accepted);
    }

    private UntagRequest prepareUntagRequest(String tagPrefix, int brewBuildId) {
        TaggedBuild taggedBuild = new TaggedBuild(tagPrefix, brewBuildId);
        return new UntagRequest(null, taggedBuild);
    }
}
