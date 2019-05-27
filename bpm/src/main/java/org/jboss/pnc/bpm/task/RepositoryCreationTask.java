/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.bpm.model.RepositoryCreationProcess;
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

    public RepositoryCreationTask(RepositoryCreationProcess repositoryCreationProcessRest, String accessToken) {
        setAccessToken(accessToken);
        this.repositoryCreationProcessRest = repositoryCreationProcessRest;
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("pncBaseUrl", config.getPncBaseUrl());
            params.put("repourBaseUrl", config.getRepourBaseUrl());
            params.put("taskData", MAPPER.writeValueAsString(repositoryCreationProcessRest));
            return params;
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not get the parameters.", e);
        }
    }

    @Override
    protected String getProcessId() {
        return config.getBcCreationProcessId();
    }

}
