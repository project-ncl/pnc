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
package org.jboss.pnc.dingroguclient;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface DingroguClient {
    Request startBuildProcessInstance(RemoteBuildTask buildTask, List<Request.Header> headers, String correlationId);

    void submitDeliverablesAnalysis(DingroguDeliverablesAnalysisDTO dto);

    void submitBuildPush(DingroguBuildPushDTO dto);

    void submitRepositoryCreation(DingroguRepositoryCreationDTO dto);

    Request cancelProcessInstance(List<Request.Header> headers, String correlationId);

    DingroguBuildWorkDTO createDTO(RemoteBuildTask buildTask, String correlationId);

    static List<Request.Header> addMdcValues(List<Request.Header> headers) {

        List<Request.Header> result = null;
        if (headers != null) {
            result = new ArrayList<>(headers);
        } else {
            result = new ArrayList<>();
        }

        // Add MDC values, always
        List<Request.Header> mdcHeaders = MDCUtils.getHeadersFromMDC()
                .entrySet()
                .stream()
                .map(entry -> new Request.Header(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        result.addAll(mdcHeaders);
        return result;
    }
}
