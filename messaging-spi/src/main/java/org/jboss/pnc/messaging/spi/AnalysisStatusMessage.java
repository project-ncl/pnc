/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

import java.util.List;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;

import lombok.Getter;

/**
 * @author jakubvanko
 */
@Getter
public class AnalysisStatusMessage implements Message {

    private final String operationId;
    private final String attribute;
    private final String milestoneId;
    private final String status;
    private final String result;
    private final List<String> deliverablesUrls;

    public AnalysisStatusMessage(
            String attribute,
            String milestoneId,
            String status,
            String result,
            List<String> deliverablesUrls) {
        this("", attribute, milestoneId, status, result, deliverablesUrls);
    }

    public AnalysisStatusMessage(
            String operationId,
            String attribute,
            String milestoneId,
            String status,
            String result,
            List<String> deliverablesUrls) {
        this.operationId = operationId;
        this.attribute = attribute;
        this.milestoneId = milestoneId;
        this.status = status;
        this.result = result;
        this.deliverablesUrls = deliverablesUrls;
    }

    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }
}
