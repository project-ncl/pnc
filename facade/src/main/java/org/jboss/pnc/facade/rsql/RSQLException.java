/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql;

import javax.ejb.ApplicationException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationException(rollback = true)
public class RSQLException extends RuntimeException {

    public RSQLException() {
    }

    public RSQLException(String message) {
        super(message);
    }

    public RSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSQLException(Throwable cause) {
        super(cause);
    }

}
