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

package org.jboss.pnc.coordinator.test.builder.bpm;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.coordinator.builder.bpm.BpmBuildScheduler;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.mockito.ArgumentCaptor;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BpmBuildSchedulerTest {

    @Ignore //TODO what exactly are we testing here?
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

        BuildConfigurationAudited buildConfigurationAudited = new BuildConfigurationAudited() {
            private IdRev idRev;

            @Override
            public void setId(IdRev idRev) {
                this.idRev = idRev;
            }

            @Override
            public IdRev getIdRev() {
                return idRev;
            }
        };
        buildConfigurationAudited.setId(new IdRev(1, 1));

        User user = User.Builder.newBuilder().username("demo").id(1).build();
        user.setLoginToken("no-token");

        BuildTask buildTask = BuildTask.build(buildConfiguration, buildConfigurationAudited, user,
                null, 1, mock(BuildSetTask.class), new Date(), true);

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
        doReturn("false").when(bpmConfiguration).getCommunityBuild();

        Configuration configurationStub = mock(Configuration.class);
        doReturn(bpmConfiguration).when(configurationStub).getModuleConfig(any());

        KieSession sessionForVerification = mock(KieSession.class);
        Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>)(Class)Map.class;
        ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(mapClass);

        BpmBuildScheduler testedScheduler = new BpmBuildScheduler(configurationStub, null) {
            @Override
            protected KieSession createSession(BuildTask buildTask) throws ConfigurationParseException, MalformedURLException {
                return sessionForVerification;
            }
        };

        //when
        testedScheduler.startBuilding(buildTask, (br) -> {
        });

        //then
        verify(sessionForVerification).startProcess(eq("processId"), parameters.capture());


//        assertThat(parameters.getValue().get("pncBaseUrl")).isEqualTo("http://localhost/pnc");
//        assertThat(parameters.getValue().get("jenkinsBaseUrl")).isEqualTo("http://localhost/jenkins");
//        assertThat(parameters.getValue().get("aproxBaseUrl")).isEqualTo("http://localhost/aprox");
//        assertThat(parameters.getValue().get("repourBaseUrl")).isEqualTo("http://localhost/repour");
//        assertThat(parameters.getValue().get("daBaseUrl")).isEqualTo("http://localhost/da");


    }

}
