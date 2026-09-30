/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dingroguclient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;

public interface DingroguClient {
    Request startBuildProcessInstance(RemoteBuildTask buildTask, List<Request.Header> headers, String correlationId);

    void submitDeliverablesAnalysis(DingroguDeliverablesAnalysisDTO dto);

    void submitBuildPush(DingroguBuildPushDTO dto);

    void submitRepositoryCreation(DingroguRepositoryCreationDTO dto);

    Request cancelProcessInstance(List<Request.Header> headers, String correlationId);

    void submitCancelProcessInstance(String correlationId);

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
