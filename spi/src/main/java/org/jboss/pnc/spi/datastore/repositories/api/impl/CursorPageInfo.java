/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;

import com.google.common.base.Preconditions;

public class CursorPageInfo implements PageInfo {

    protected final int pageSize;
    protected final int elementOffset;

    public CursorPageInfo(int elementOffset, int pageSize) {
        Preconditions.checkArgument(elementOffset >= 0, "Element offset must be >= 0");
        Preconditions.checkArgument(pageSize >= 0, "Page size must be >= 0");
        this.pageSize = pageSize;
        this.elementOffset = elementOffset;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getPageOffset() {
        return elementOffset / pageSize;
    }

    @Override
    public int getElementOffset() {
        return elementOffset;
    }
}
