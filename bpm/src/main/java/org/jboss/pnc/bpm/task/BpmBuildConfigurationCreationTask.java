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
package org.jboss.pnc.bpm.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.ToString;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.rest.restmodel.bpm.BpmBuildConfigurationCreationRest;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jakub Senko
 */
@ToString(callSuper = true)
public class BpmBuildConfigurationCreationTask extends BpmTask {

    private static final Logger logger = LoggerFactory.getLogger(BpmBuildConfigurationCreationTask.class);

    private final BpmBuildConfigurationCreationRest taskData;

    public BpmBuildConfigurationCreationTask(BpmBuildConfigurationCreationRest taskData) {
        this.taskData = taskData;
    }

    @Override
    protected Map<String, Object> getProcessParameters() throws CoreException {
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("bc_name", taskData.getName());
            params.put("bc_description", taskData.getDescription());
            params.put("bc_buildScript", taskData.getBuildScript());
            params.put("bc_scmRepoURL", taskData.getScmRepoURL());
            params.put("bc_scmRevision", taskData.getScmRevision());
            params.put("bc_scmExternalRepoURL", taskData.getScmExternalRepoURL());
            params.put("bc_scmExternalRevision", taskData.getScmExternalRevision());
            params.put("bc_projectId", taskData.getProjectId());
            params.put("bc_buildEnvironmentId", taskData.getBuildEnvironmentId());
            params.put("bc_dependencyIds", taskData.getDependencyIds());
            params.put("bc_productVersionId", MAPPER.writeValueAsString(taskData.getProductVersionId()));
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not serialize " + taskData.getProductVersionId(), e);
        }
        logger.debug("Created parameters {} for {}.", params, this);
        return params;
    }

    @Override
    protected String getProcessId() {
        return config.getBcCreationProcessId();
    }

}
