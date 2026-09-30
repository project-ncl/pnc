/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ResultStatus;

@ApplicationScoped
public class ResultStatusMapper {

    public OperationResult toOperationResult(ResultStatus resultStatus) {
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
