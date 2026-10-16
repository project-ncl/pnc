/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import org.jboss.pnc.enums.ResultStatus;

import lombok.Data;

/**
 * Result of the build deletion operation sent by callback.
 */
@Data
public class DeleteOperationResult {

    /**
     * Build id.
     */
    private String id;

    /**
     * Status of the deletion operation.
     */
    private ResultStatus status;

    /**
     * Result message.
     */
    private String message;
}
