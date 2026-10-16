/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

public interface PageInfo {
    int getPageSize();

    int getPageOffset();

    default int getElementOffset() {
        return getPageSize() * getPageOffset();
    }
}
