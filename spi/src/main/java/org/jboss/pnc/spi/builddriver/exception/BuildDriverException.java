/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.builddriver.exception;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-01.
 */
public class BuildDriverException extends Throwable {

    private static final long serialVersionUID = -2381584604707278639L;

    public BuildDriverException(String message) {
        super(message);
    }

    public BuildDriverException(String message, Throwable cause) {
        super(message, cause);
    }
}
