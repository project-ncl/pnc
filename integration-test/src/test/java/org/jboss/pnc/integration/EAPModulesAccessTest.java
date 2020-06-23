/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.buildagent.client.BuildAgentHttpClient;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class EAPModulesAccessTest {

    private static final Logger logger = LoggerFactory.getLogger(EAPModulesAccessTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive ear = Deployments.testEarForInContainerTest(EAPModulesAccessTest.class);
        return ear;
    }

    /**
     * Requires access to undertow-core because of HttpClient usage
     */
    @Test
    public void shouldBeAbleToCreteBuildAgentClient() throws BuildAgentClientException {
        BuildAgentClient client = new BuildAgentHttpClient("http://localhost/", null, "POST");
        Assert.assertNotNull(client);
    }

}
