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
package org.jboss.pnc.bpm.task;

import lombok.ToString;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.RepositoryCreationProcess;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ToString(callSuper = true)
public class RepositoryCreationTask extends BpmTask {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCreationTask.class);

    /**
     * The RepositoryCreationRest.BuildConfigurationRest
     */
    private final RepositoryCreationProcess repositoryCreationProcessRest;
    private JobNotificationType jobType;

    public RepositoryCreationTask(RepositoryCreationProcess repositoryCreationProcessRest, String accessToken) {
        super(accessToken);
        this.repositoryCreationProcessRest = repositoryCreationProcessRest;
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pncBaseUrl", globalConfig.getPncUrl());
        params.put("repourBaseUrl", globalConfig.getExternalRepourUrl());
        params.put("jobType", jobType.toString());
        params.put("taskData", repositoryCreationProcessRest);
        return params;
    }

    public void setJobType(JobNotificationType jobType) {
        this.jobType = jobType;
    }
}
