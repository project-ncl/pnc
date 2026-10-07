/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

public class ScheduleException extends CoreException {
    public ScheduleException(String message) {
        super(message);
    }

    public ScheduleException(Exception e) {
        super(e);
    }

    public ScheduleException(String message, Exception e) {
        super(message, e);
    }
}
