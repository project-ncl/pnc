/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

public class ScheduleConflictException extends ScheduleException {
    public ScheduleConflictException(String message) {
        super(message);
    }

    public ScheduleConflictException(Exception e) {
        super(e);
    }

    public ScheduleConflictException(String message, Exception e) {
        super(message, e);
    }
}
