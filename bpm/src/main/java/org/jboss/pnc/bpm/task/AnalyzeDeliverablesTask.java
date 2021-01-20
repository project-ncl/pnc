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
package org.jboss.pnc.bpm.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.ToString;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.AnalyzeDeliverablesBpmRequest;
import org.jboss.pnc.spi.exception.CoreException;

import java.io.Serializable;
import java.util.HashMap;

@ToString(callSuper = true)
public class AnalyzeDeliverablesTask extends BpmTask {

    private final AnalyzeDeliverablesBpmRequest request;

    private final Request callback;

    public AnalyzeDeliverablesTask(AnalyzeDeliverablesBpmRequest request, String accessToken, Request callback) {
        super(accessToken);
        this.request = request;
        this.callback = callback;
    }

    @Override
    public String getProcessId() {
        return config.getDeliverablesProcessId();
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("pncBaseUrl", globalConfig.getPncUrl());
            params.put("delAnalUrl", globalConfig.getDelAnalUrl());
            params.put("callback", callback);
            if (isJsonEncodedProcessParameters()) {
                params.put("taskData", MAPPER.writeValueAsString(request));
            } else {
                params.put("taskData", request);
            }
            return params;
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not get the parameters.", e);
        }
    }
}
