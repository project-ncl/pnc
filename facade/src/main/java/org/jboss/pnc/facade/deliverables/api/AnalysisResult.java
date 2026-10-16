/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.deliverables.api;

import java.util.List;

import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.model.Base32LongID;

import lombok.Builder;
import lombok.Value;

/**
 * AnalysisResult which is 1:1 mapping with {@link org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult}, of course
 * with IDs mapped as are in entities.
 */
@Value
@Builder(builderClassName = "Builder", toBuilder = true)
public class AnalysisResult {

    Base32LongID deliverableAnalyzerOperationId;

    List<FinderResult> results;

    boolean wasRunAsScratchAnalysis;
}
