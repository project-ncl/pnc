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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.core.builder.coordinator.BuildScheduler;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildStatus;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(BpmBuildScheduler.class);
    private final int AUTHENTICATION_TIMEOUT_S = 20;

    private BpmCompleteListener bpmCompleteListener;

    private Configuration configuration;

    @Override
    public String getId() {
        return "bpm-build-scheduler";
    }

    @Deprecated
    public BpmBuildScheduler() { //CDI workaround
    }

    @Inject
    public BpmBuildScheduler(Configuration configuration, BpmCompleteListener bpmCompleteListener) throws MalformedURLException {
        this.bpmCompleteListener = bpmCompleteListener;
        this.configuration = configuration;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildStatus> onComplete) throws CoreException {
        ProcessInstance processInstance = startProcess(buildTask, Optional.of(buildTask)
                .map(BuildTask::getBuildSetTask)
                .map(BuildSetTask::getId)
                .orElse(null));
        logger.info("New component build process started with process instance id {}.", processInstance.getId());
        registerCompleteListener(buildTask.getId(), onComplete);
    }

    private void registerCompleteListener(int taskId, Consumer<BuildStatus> onComplete) {
        BpmListener bpmListener = new BpmListener(taskId, onComplete);
        bpmCompleteListener.subscribe(bpmListener);
    }

    ProcessInstance startProcess(BuildTask buildTask, Integer buildTaskSetId) throws CoreException {
        try {
            KieSession kieSession = createSession(buildTask);
            ProcessInstance processInstance = kieSession.startProcess(getProcessId(buildTask), createParameters(buildTask, buildTaskSetId));
            logger.debug("Crated new process instance with id [{}]", processInstance.getId());
            return processInstance;
        } catch (ConfigurationParseException e) {
            throw new CoreException("Could not parse configuration", e);
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not write JSON", e);
        } catch (MalformedURLException e) {
            throw new CoreException("InstanceURL is malformed", e);
        }
    }

    Map<String, Object> createParameters(BuildTask buildTask, Integer buildTaskSetId) throws JsonProcessingException, ConfigurationParseException {
        logger.debug("[{}] Creating parameters", buildTask.getId());
        Map<String, Object> parameters = new HashMap<>();
        fillBuildRequest(parameters, buildTask, buildTaskSetId);
        fillUrls(parameters);
        fillComponentParameters(parameters, buildTask);
        logger.debug("[{}] Created parameters", parameters);
        return parameters;
    }

    void fillComponentParameters(Map<String, Object> parameters, BuildTask buildTask) throws JsonProcessingException {
        /*
        "GAV" : "org.artificer:artificer:1.0.0.Beta1",
        "Description" : "a test",
        "SCM" : "https://github.com/ArtificerRepo/artificer.git",
        "Tag" : "artificer-1.0.0.Beta1-IDP-1",
        "JavaVersion" : "1.7.0_55",
        "MavenVersion" : "3.1.1",
        "BuildCommand" : "mvn -Pgenerate-docs",
        "CommandLineParams" : "-Xmx950m -XX:MaxPermSize=256m -Dmaven.artifact.threads=5",
        "BuildArtifactsRequired" : "JBoss EAP 6.4",
        "CommunityBuild" : "false",
        "PatchBuild" : "false",
        "ProjectId" : 1,
        "EnvironmentId" : 1
         */
        Map<String, Object> params = new HashMap<>();
        params.put("GAV", buildTask.getBuildConfiguration().getName());
        params.put("Description", buildTask.getBuildConfiguration().getDescription());
        params.put("SCM", buildTask.getBuildConfiguration().getScmRepoURL());
        params.put("Tag", buildTask.getBuildConfiguration().getScmRevision());//no such field in PNC, we have only revision
        params.put("JavaVersion", null);//no such field in PNC - We use Environment id...
        params.put("MavenVersion", null);//no such field in PNC
        params.put("BuildCommand", buildTask.getBuildConfiguration().getBuildScript());
        params.put("CommandLineParams", null);//no such field in PNC
        params.put("BuildArtifactsRequired", buildTask.getBuildConfiguration().getAllDependencies().stream()
                .map(bc -> bc.getName())
                .collect(Collectors.toList()));//Is it correct?
        params.put("CommunityBuild", "true");//hardcoded?
        params.put("EnvironmentId", Optional.of(buildTask.getBuildConfiguration())
                .map(BuildConfiguration::getBuildEnvironment)
                .map(BuildEnvironment::getId)
                .orElse(null));
        params.put("PatchBuild", "false");//hardcoded?
        params.put("ProjectId", Optional.of(buildTask.getBuildConfiguration())
                .map(BuildConfiguration::getProject)
                .map(Project::getId)
                .orElse(null));
        parameters.put("paramsJSON", objectMapper.writeValueAsString(params));
    }

    void fillUrls(Map<String, Object> parameters) throws ConfigurationParseException {
        BpmModuleConfig moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        parameters.put("pncBaseUrl", moduleConfig.getPncBaseUrl());
        parameters.put("jenkinsBaseUrl", moduleConfig.getJenkinsBaseUrl());
        parameters.put("aproxBaseUrl", moduleConfig.getAproxBaseUrl());
        parameters.put("repourBaseUrl", moduleConfig.getRepourBaseUrl());
        parameters.put("daBaseUrl", moduleConfig.getDaBaseUrl());
    }

    void fillBuildRequest(Map<String, Object> parameters, BuildTask buildTask, Integer buildTaskSetId)
            throws JsonProcessingException {
        Map<String, Object> buildRequest = new HashMap<>();
        buildRequest.put("buildTaskId", buildTask.getId());
        buildRequest.put("buildTaskSetId", buildTaskSetId);
        buildRequest.put("buildConfiguration",buildTask.getBuildConfiguration().getId());
        buildRequest.put("buildConfigurationRevision",
                Optional.of(buildTask)
                        .map(BuildTask::getBuildConfigurationAudited)
                        .map(BuildConfigurationAudited::getIdRev)
                        .map(IdRev::getRev)
                        .orElse(null));
        buildRequest.put("buildRecordSetIdsCSV", StringUtils.toCVS(buildTask.getBuildRecordSetIds()));
        buildRequest.put("buildConfigSetRecordId", buildTask.getBuildConfigSetRecordId());
        buildRequest.put("buildContentId", ContentIdentityManager.getBuildContentId(buildTask.getBuildConfiguration()));
        buildRequest.put("submitTimeMillis", buildTask.getSubmitTime().getTime());
        buildRequest.put("pncUsername", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getUsername)
                .orElse(null));
        buildRequest.put("pncUserLoginToken", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getLoginToken)
                .filter(token -> !"no-token".equals(token))
                .orElse(null));
        buildRequest.put("pncUserEmail", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getEmail)
                .orElse(null));

        parameters.put("buildRequestJSON", objectMapper.writeValueAsString(buildRequest));
    }

    KieSession createSession(BuildTask buildTask) throws ConfigurationParseException, MalformedURLException {
        logger.debug("[{}] creating KIE session", buildTask.getId());
        BpmModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        String instanceUrl = config.getBpmInstanceUrl();
        String deploymentId = config.getDeploymentId();
        String bpmEndpointUser = config.getUsername();
        String bpmEndpointPassword = config.getPassword();

        logger.debug("[{}] Session parameters InstanceURL: {} deploymentId: {} User: {}", buildTask.getId(), instanceUrl, deploymentId, bpmEndpointUser);


        RuntimeEngine restSessionFactory = RemoteRestRuntimeEngineFactory.newBuilder()
                .addDeploymentId(deploymentId)
                .addUrl(new URL(instanceUrl))
                .addUserName(bpmEndpointUser)
                .addPassword(bpmEndpointPassword)
                .addTimeout(AUTHENTICATION_TIMEOUT_S)
                .build();

        return restSessionFactory.getKieSession();
    }

    String getProcessId(BuildTask buildTask) throws ConfigurationParseException {
        BpmModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        String processId = config.getProcessId();
        logger.debug("[{}] Getting processId: {} ", buildTask.getId(), processId);
        return processId;
    }
}
