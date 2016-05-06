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

package org.jboss.pnc.coordinator.builder.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.coordinator.content.ContentIdentityManager;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
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
    public BpmBuildScheduler(Configuration configuration, BpmCompleteListener bpmCompleteListener) {
        this.bpmCompleteListener = bpmCompleteListener;
        this.configuration = configuration;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildResult> onComplete) throws CoreException {
        try {
            ProcessInstance processInstance = startProcess(buildTask);
            logger.info("New component build process started with process instance id {}.", processInstance.getId());
            registerCompleteListener(buildTask.getId(), onComplete);
        } catch (Exception e) {
            throw new CoreException("Error while trying to startBuilding with BpmBuildScheduler.", e);
        }
    }

    private void registerCompleteListener(int taskId, Consumer<BuildResult> onComplete) {
        BpmListener bpmListener = new BpmListener(taskId, onComplete);
        bpmCompleteListener.subscribe(bpmListener);
    }

    private ProcessInstance startProcess(BuildTask buildTask) throws CoreException {
        try {
            KieSession kieSession = createSession(buildTask);
            ProcessInstance processInstance = kieSession.startProcess(getProcessId(buildTask), createParameters(buildTask));
            if (processInstance == null) {
                logger.warn("Failed to create new process instance.");
            } else {
                logger.debug("Created new process instance with id [{}]", processInstance.getId());
            }
            return processInstance;
        } catch (ConfigurationParseException e) {
            throw new CoreException("Could not parse configuration", e);
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not write JSON", e);
        } catch (MalformedURLException e) {
            throw new CoreException("InstanceURL is malformed", e);
        }
    }

    Map<String, Object> createParameters(BuildTask buildTask) throws JsonProcessingException, ConfigurationParseException {
        logger.debug("[{}] Creating parameters", buildTask.getId());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("processParameters", getProcessConfig());
        parameters.put("buildExecutionConfiguration", getBuildExecutionConfiguration(buildTask));
        logger.debug("[{}] Created parameters", parameters);
        return parameters;
    }

    String getProcessConfig()
            throws JsonProcessingException, ConfigurationParseException {

        BpmModuleConfig moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        Map<String, Object> params = new HashMap<>();

        params.put("pncBaseUrl", moduleConfig.getPncBaseUrl());
        params.put("aproxBaseUrl", moduleConfig.getAproxBaseUrl());
        params.put("repourBaseUrl", moduleConfig.getRepourBaseUrl());
        params.put("daBaseUrl", moduleConfig.getDaBaseUrl());
        params.put("communityBuild", Boolean.valueOf(Optional.ofNullable(moduleConfig.getCommunityBuild()).orElse("true")));
        params.put("versionAdjust", Boolean.valueOf(Optional.ofNullable(moduleConfig.getVersionAdjust()).orElse("false")));

        return objectMapper.writeValueAsString(params);
    }

    String getBuildExecutionConfiguration(BuildTask buildTask) {

        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
        String contentId = ContentIdentityManager.getBuildContentId(buildConfiguration.getName());

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                buildTask.getId(),
                contentId,
                buildTask.getUser().getId(),
                buildConfiguration.getBuildScript(),
                buildConfiguration.getName(),
                buildConfiguration.getScmRepoURL(),
                buildConfiguration.getScmRevision(),
                buildConfiguration.getScmMirrorRepoURL(),
                buildConfiguration.getScmMirrorRevision(),
                buildConfiguration.getBuildEnvironment().getBuildType());

        BuildExecutionConfigurationRest buildExecutionConfigurationREST = new BuildExecutionConfigurationRest(buildExecutionConfiguration);

        return buildExecutionConfigurationREST.toString();
    }

    protected KieSession createSession(BuildTask buildTask) throws ConfigurationParseException, MalformedURLException {
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
