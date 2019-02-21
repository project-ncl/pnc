/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.provider.collection;

import java.util.Collection;
import java.util.Collections;

/**
 * JPA Collection info
 *
 * @author Sebastian Laskawiec
 */
public class CollectionInfo<T> {

    private final Integer pageIndex;
    private final Integer pageSize;
    private final Integer totalPages;
    private final Collection<T> content;

    public CollectionInfo(Integer pageIndex, Integer pageSize, Integer totalPages, Collection<T> content) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.content = Collections.unmodifiableCollection(content);
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Collection<T> getContent() {
        return content;
    }
}
