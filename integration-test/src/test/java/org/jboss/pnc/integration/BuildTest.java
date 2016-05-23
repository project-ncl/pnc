/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildTest {

    private static BuildConfigurationRestClient buildConfigurationRestClient;
    private static BuildConfigurationSetRestClient buildConfigurationSetRestClient;
    private static BuildRecordRestClient buildRecordRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(BuildTest.class);
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(buildConfigurationRestClient == null) {
            buildConfigurationRestClient = new BuildConfigurationRestClient();
        }
        if(buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }
        if(buildRecordRestClient == null) {
            buildRecordRestClient = new BuildRecordRestClient();
        }
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() throws Exception {
        //given
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.firstNotNull().getValue();

        //when
        RestResponse<BuildConfigurationRest> triggeredConfiguration = buildConfigurationRestClient.trigger(buildConfiguration.getId(), true);
        Integer buildRecordId = ResponseUtils.getIdFromLocationHeader(triggeredConfiguration.getRestCallResponse());

        //then
        assertThat(triggeredConfiguration.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildRecordId, false).hasValue(), 2,
                TimeUnit.MINUTES);
    }

    @Test
    public void shouldTriggerBuildSetAndFinishWithoutProblems() throws Exception {
        //given
        BuildConfigurationSetRest buildConfigurationSet = buildConfigurationSetRestClient.firstNotNull().getValue();

        //when
        RestResponse<BuildConfigurationSetRest> response = buildConfigurationSetRestClient.trigger(buildConfigurationSet.getId(), true);
        Integer buildRecordSetId = ResponseUtils.getIdFromLocationHeader(response.getRestCallResponse());

        //then
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(200);
        assertThat(buildRecordSetId).isNotNull();
    }
}
