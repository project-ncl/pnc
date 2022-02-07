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
package org.jboss.pnc.facade;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.Operation;

import java.util.Map;

public interface OperationsManager {

    Operation updateProgress(Base32LongID operationId, ProgressStatus status);

    Operation setResult(Base32LongID operationId, OperationResult result);

    DeliverableAnalyzerOperation newDeliverableAnalyzerOperation(String milestoneId, Map<String, String> inputParams);

    Request getOperationCallback(Base32LongID operationId);
}
