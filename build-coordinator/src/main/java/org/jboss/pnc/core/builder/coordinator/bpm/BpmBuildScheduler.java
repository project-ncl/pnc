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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.core.builder.coordinator.BuildScheduler;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.BuildStatus;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BpmBuildScheduler.class);

    private BpmModuleConfig config;

    private BpmCompleteListener bpmCompleteListener;

    private final String instanceUrl;
    private final String deploymentId;
    private final String processId;

    private final String user;
    private final String password;

    @Override
    public String getId() {
        return "bpm-build-scheduler";
    }

    @Deprecated
    public BpmBuildScheduler() { //CDI workaround
        instanceUrl = null;
        deploymentId = null;
        processId = null;
        user = null;
        password = null;
    }

    @Inject
    public BpmBuildScheduler(Configuration configuration, BpmCompleteListener bpmCompleteListener) throws MalformedURLException, ConfigurationParseException {
        config = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));

        this.bpmCompleteListener = bpmCompleteListener;

        instanceUrl = config.getBpmInstanceUrl();
        deploymentId = config.getDeploymentId();
        processId = config.getProcessId();

        user = config.getUsername();
        password = config.getPassword();
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildStatus> onComplete) throws CoreException {

        ProcessInstance processInstance = startProcess(buildTask, buildTask.getBuildSetTask().getId());
        logger.info("New component build process started with process instance id {}.", processInstance.getId());
        registerCompleteListener(buildTask.getId(), onComplete);
    }

    private void registerCompleteListener(int taskId, Consumer<BuildStatus> onComplete) {
        BpmListener bpmListener = new BpmListener(taskId, onComplete);
        bpmCompleteListener.subscribe(bpmListener);
    }

    private ProcessInstance startProcess(BuildTask buildTask, Integer buildTaskSetId) throws CoreException {
        RemoteRestRuntimeEngineFactory restSessionFactory;
        try {
            restSessionFactory = new RemoteRestRuntimeEngineFactory(deploymentId, new URL(instanceUrl), user, password);
        } catch (MalformedURLException e) {
            throw new CoreException("Invalid bpm server url.", e);
        }

        RemoteRuntimeEngine engine = restSessionFactory.newRuntimeEngine();
        KieSession kieSession = engine.getKieSession();

        BuildConfigurationAudited configurationAudited = buildTask.getBuildConfigurationAudited();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("buildTaskId",buildTask.getId() );
        parameters.put("buildTaskSetId", buildTaskSetId);
        parameters.put("buildConfigurationRevision", configurationAudited.getRev());
        parameters.put("buildRecordSetIdsCSV", StringUtils.toCVS(buildTask.getBuildRecordSetIds()));
        parameters.put("buildConfigSetRecordId", buildTask.getBuildConfigSetRecordId());
        parameters.put("buildContentId", ContentIdentityManager.getBuildContentId(buildTask.getBuildConfiguration()));
        parameters.put("submitTimeMillis", buildTask.getSubmitTime().getTime());

        return kieSession.startProcess(processId, parameters);
    }
}
