/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Status of generic result of some operation or task.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum ResultStatus {
    /**
     * The operation was successful.
     */
    SUCCESS(true),
    /**
     * The operation failed.
     */
    FAILED(false),
    /**
     * The operation timed-out.
     */
    TIMED_OUT(false),
    /**
     * The operation failed unexpectedly.
     */
    SYSTEM_ERROR(false);

    private boolean success;

    ResultStatus(boolean success) {
        this.success = success;
    }

    /**
     * Returns true if the operation resulted is successful.
     */
    public boolean isSuccess() {
        return success;
    }
}
