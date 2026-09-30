/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dingroguclient;

import java.util.List;

import org.jboss.pnc.api.dto.Request;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
@Builder
public class DingroguDeliverablesAnalysisDTO {
    String deliverablesAnalyzerUrl;
    String orchUrl;

    List<String> urls;
    String config;
    boolean scratch;

    String operationId;
    // callback for operationId
    Request callback;
}
