/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade;

import java.util.Map;

import org.jboss.pnc.api.dto.OperationOutcome;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.Operation;

public interface OperationsManager {

    Operation updateProgress(Base32LongID operationId, ProgressStatus status);

    Operation setResult(Base32LongID operationId, OperationOutcome operationOutcome);

    DeliverableAnalyzerOperation newDeliverableAnalyzerOperation(String milestoneId, Map<String, String> inputParams);

    Request getOperationCallback(Base32LongID operationId);

    BuildPushOperation newBuildPushOperation(BuildRecord build, Map<String, String> inputParams);
}
