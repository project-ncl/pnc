/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.Collection;
import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Data;

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
