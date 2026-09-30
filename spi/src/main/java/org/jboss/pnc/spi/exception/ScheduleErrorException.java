/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

public class ScheduleErrorException extends ScheduleException {
    public ScheduleErrorException(String message) {
        super(message);
    }

    public ScheduleErrorException(Exception e) {
        super(e);
    }

    public ScheduleErrorException(String message, Exception e) {
        super(message, e);
    }
}
