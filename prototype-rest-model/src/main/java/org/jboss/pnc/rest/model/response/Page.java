/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.Collections;

import lombok.Data;

/**
 * Collection REST response.
 *
 * @author Sebastian Laskawiec
 */
@ApiModel(description = "Results with additional Paging information")
@Data
public class Page<T> {

    @ApiModelProperty("Page index")
    private final int pageIndex;

    @ApiModelProperty("Number of records per page")
    private final int pageSize;

    @ApiModelProperty("Total pages provided by this query or -1 if unknown")
    private final int totalPages;

    @ApiModelProperty("Embedded collection of data")
    private final Collection<T> content;

    /**
     * Creates new empty page.
     */
    public Page() {
        this.pageIndex = 0;
        this.pageSize = 0;
        this.totalPages = 0;
        this.content = Collections.emptyList();
    }

    public Page(int pageIndex, int pageSize, int totalPages, Collection<T> content) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.content = Collections.unmodifiableCollection(content);
    }

}
