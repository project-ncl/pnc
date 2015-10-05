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

package org.jboss.pnc.core.builder.coordinator.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.mockito.ArgumentCaptor;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BpmBuildSchedulerTest {

    @Test
    public void shouldInvokeBpmEngineWithCorrectParameters() throws Exception {
        //given
        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                .name("name")
                .buildScript("mvn clean install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .project(Project.Builder.newBuilder().name("test").id(1).build())
                .id(1)
                .scmRepoURL("http://github.com/test/me")
                .scmRevision("master")
                .description("test")
                .build();

        BuildTask buildTask = BuildTask.build(buildConfiguration, null, null, null, null, 1, mock(BuildSetTask.class), new Date(), true);

        BpmModuleConfig bpmConfiguration = mock(BpmModuleConfig.class);
        doReturn("http://localhost/aprox").when(bpmConfiguration).getAproxBaseUrl();
        doReturn("http://localhost/pnc").when(bpmConfiguration).getPncBaseUrl();
        doReturn("http://localhost/jenkins").when(bpmConfiguration).getJenkinsBaseUrl();
        doReturn("http://localhost/repour").when(bpmConfiguration).getRepourBaseUrl();
        doReturn("http://localhost/da").when(bpmConfiguration).getDaBaseUrl();
        doReturn("http://localhost/bpm").when(bpmConfiguration).getBpmInstanceUrl();
        doReturn("deplymentId").when(bpmConfiguration).getDeploymentId();
        doReturn("processId").when(bpmConfiguration).getProcessId();
        doReturn("password").when(bpmConfiguration).getPassword();
        doReturn("username").when(bpmConfiguration).getUsername();

        Configuration configurationStub = mock(Configuration.class);
        doReturn(bpmConfiguration).when(configurationStub).getModuleConfig(any());

        KieSession sessionForVerification = mock(KieSession.class);
        Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>)(Class)Map.class;
        ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(mapClass);

        BpmBuildScheduler testedScheduler = new BpmBuildScheduler(configurationStub, null) {
            @Override
            KieSession createSession(BuildTask buildTask) throws ConfigurationParseException, MalformedURLException {
                return sessionForVerification;
            }
        };

        //when
        testedScheduler.startProcess(buildTask, 1);

        //then
        verify(sessionForVerification).startProcess(eq("processId"), parameters.capture());
        assertThat(parameters.getValue().get("pncBaseUrl")).isEqualTo("http://localhost/pnc");
        assertThat(parameters.getValue().get("jenkinsBaseUrl")).isEqualTo("http://localhost/jenkins");
        assertThat(parameters.getValue().get("aproxBaseUrl")).isEqualTo("http://localhost/aprox");
        assertThat(parameters.getValue().get("repourBaseUrl")).isEqualTo("http://localhost/repour");
        assertThat(parameters.getValue().get("daBaseUrl")).isEqualTo("http://localhost/da");

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode paramsJSON = objectMapper.readTree(parameters.getValue().get("paramsJSON").toString());
        JsonNode buildRequestJSON = objectMapper.readTree(parameters.getValue().get("buildRequestJSON").toString());

        assertThat(paramsJSON.at("/GAV").asText()).isEqualTo("org.jboss.pnc:test:1.0.0-SNAPSHOT");
        assertThat(paramsJSON.at("/Description").asText()).isEqualTo("test");
        assertThat(paramsJSON.at("/SCM").asText()).isEqualTo("http://github.com/test/me");
        assertThat(paramsJSON.at("/Tag").asText()).isEqualTo("master");
        assertThat(paramsJSON.at("/JavaVersion").asText()).isEqualTo("null");
        assertThat(paramsJSON.at("/MavenVersion").asText()).isEqualTo("null");
        assertThat(paramsJSON.at("/BuildCommand").asText()).isEqualTo("mvn clean install");
        assertThat(paramsJSON.at("/CommandLineParams").asText()).isEqualTo("null");
        assertThat(paramsJSON.at("/BuildArtifactsRequired").asText()).isEqualTo("");
        assertThat(paramsJSON.at("/CommunityBuild").asText()).isEqualTo("true");
        assertThat(paramsJSON.at("/EnvironmentId").asText()).isEqualTo("1");
        assertThat(paramsJSON.at("/PatchBuild").asText()).isEqualTo("false");
        assertThat(paramsJSON.at("/ProjectId").asText()).isEqualTo("1");

        assertThat(buildRequestJSON.at("/buildTaskId").asText()).isEqualTo("1");
        assertThat(buildRequestJSON.at("/buildTaskSetId").asText()).isEqualTo("1");
        assertThat(buildRequestJSON.at("/buildConfigurationRevision").asText()).isEqualTo("null");
        assertThat(buildRequestJSON.at("/buildRecordSetIdsCSV").asText()).isEqualTo("");
        assertThat(buildRequestJSON.at("/buildConfigSetRecordId").asText()).isEqualTo("null");
        assertThat(buildRequestJSON.at("/buildContentId").asText()).isNotEmpty();
        assertThat(buildRequestJSON.at("/submitTimeMillis").asText()).isNotEmpty();
        assertThat(buildRequestJSON.at("/pncUsername").asText()).isEqualTo("null");
        assertThat(buildRequestJSON.at("/pncUserLoginToken").asText()).isEqualTo("null");
    }

}