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
package org.jboss.pnc.facade.util;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ResultStatus;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultStatusMapper {

    public OperationResult mapResultStatusToOperationResult(ResultStatus resultStatus) {
        final OperationResult operationResult;
        switch (resultStatus) {
            case SUCCESS:
                operationResult = OperationResult.SUCCESSFUL;
                break;
            case FAILED:
                operationResult = OperationResult.FAILED;
                break;
            case SYSTEM_ERROR:
                operationResult = OperationResult.SYSTEM_ERROR;
                break;
            case CANCELLED:
                operationResult = OperationResult.CANCELLED;
                break;
            case TIMED_OUT:
                operationResult = OperationResult.TIMEOUT;
                break;
            default:
                operationResult = null;
                break;
        }
        return operationResult;
    }
}
