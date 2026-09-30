/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.exception;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class CoreException extends Exception {
    public CoreException(String message) {
        super(message);
    }

    public CoreException(Exception e) {
        super(e);
    }

    public CoreException(String message, Exception e) {
        super(message, e);
    }
}
