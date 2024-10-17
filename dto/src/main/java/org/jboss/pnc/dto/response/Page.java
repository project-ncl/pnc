/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;

/**
 * Collection REST response.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@AllArgsConstructor
public class Page<T> {

    /**
     * Page index.
     */
    private int pageIndex;

    /**
     * Number of records per page.
     */
    private int pageSize;

    /**
     * Total pages provided by this query or -1 if unknown.
     */
    private int totalPages;

    /**
     * Number of all hits (not only this page).
     */
    private int totalHits;

    /**
     * Embedded collection of data.
     */
    private Collection<T> content;

    public Page() {
        content = Collections.emptyList();
    }

    public Page(int pageIndex, int pageSize, int totalHits, Collection<T> content) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalHits / pageSize);
        this.totalHits = totalHits;
        this.content = content;
    }
}
