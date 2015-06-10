package org.jboss.pnc.spi.datastore.repositories.api.impl;

import com.google.common.base.Preconditions;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;

public class DefaultPageInfo implements PageInfo {

    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_OFFSET = 50;

    protected final int pageSize;
    protected final int pageOffset;

    public DefaultPageInfo(int pageOffset, int pageSize) {
        Preconditions.checkArgument(pageOffset >= 0, "Page offset must be >= 0");
        Preconditions.checkArgument(pageSize > 0, "Page size must be > 0");
        this.pageSize = pageSize;
        this.pageOffset = pageOffset;
    }

    public DefaultPageInfo() {
        this(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_OFFSET);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getPageOffset() {
        return pageOffset;
    }
}
