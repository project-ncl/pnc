/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
