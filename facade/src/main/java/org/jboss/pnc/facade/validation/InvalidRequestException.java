/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import javax.ejb.ApplicationException;

import org.jboss.pnc.spi.exception.BuildRequestException;

@ApplicationException(rollback = true)
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(BuildRequestException e) {
        super(e);
    }
}
