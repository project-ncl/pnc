/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

/**
 * Thrown the user attempts to run a build when the a build task using the same configuration is already running.
 */
public class BuildConflictException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The id of the build task which conflicts with the new request
     */
    private String buildTaskId;

    public BuildConflictException(String message) {
        super(message);
    }

    public BuildConflictException(String message, String buildTaskId) {
        super(message);
        this.buildTaskId = buildTaskId;
    }

    public String getBuildTaskId() {
        return buildTaskId;
    }
}
