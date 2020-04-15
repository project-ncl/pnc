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

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.EnvironmentRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.BuildEnvironmentEndpoint;
import org.jboss.pnc.rest.provider.BuildEnvironmentProvider;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class EnvironmentRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(BuildEnvironmentProvider.class);
        restWar.addClass(BuildEnvironmentEndpoint.class);
        restWar.addClass(BuildEnvironmentRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(3)
    public void shouldQueryForEnvironment() {
        // given
        EnvironmentRestClient client = new EnvironmentRestClient();

        // when
        RestResponse<List<BuildEnvironmentRest>> allNonDeprecated = client.all(true, 0, 50, "deprecated==false", "");
        // then
        BuildEnvironmentRest nonDeprecatedEnv = allNonDeprecated.getValue().get(0);
        Assertions.assertThat(nonDeprecatedEnv.isDeprecated()).isFalse();

        // when
        RestResponse<List<BuildEnvironmentRest>> allDeprecated = client.all(true, 0, 50, "deprecated==true", "");
        // then
        BuildEnvironmentRest deprecatedEnv = allDeprecated.getValue().get(0);
        Assertions.assertThat(deprecatedEnv.isDeprecated()).isTrue();
    }

}
