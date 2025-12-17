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
package org.jboss.pnc.causewayclient;

import org.jboss.pnc.api.causeway.dto.untag.TaggedBuild;
import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.auth.DefaultServiceAccountClient;
import org.jboss.pnc.auth.KeycloakServiceClient;
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

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category(DebugTest.class)
public class CausewayClientRemoteTest {

    KeycloakServiceClient keycloakServiceClient;
    ServiceAccountClient serviceAccountClient;

    private CausewayClient causewayClient;

    public CausewayClientRemoteTest() throws IOException, ConfigurationParseException {
        SystemConfig systemConfig = SystemConfigMock.withKeycloakServiceAccount();
        keycloakServiceClient = new DefaultKeycloakServiceClient(systemConfig);
        serviceAccountClient = new DefaultServiceAccountClient(keycloakServiceClient);

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
