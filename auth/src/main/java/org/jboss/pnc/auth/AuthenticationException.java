/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

/**
 * This exception is thrown when there's something wrong with the authentication mechanism.
 *
 * It will result in 500 status code for requests.
 *
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 3/23/16 Time: 12:32 PM
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
