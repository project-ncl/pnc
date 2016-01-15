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

package org.jboss.pnc.rest.restmodel.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Collections;

/**
 * Collection REST response.
 *
 * @author Sebastian Laskawiec
 */
@XmlRootElement
@ApiModel(description = "Results with additional Paging information")
public class Page<T> {

    @ApiModelProperty("Page index")
    private Integer pageIndex;

    @ApiModelProperty("Number of records per page")
    private Integer pageSize;

    @ApiModelProperty("Total pages provided by this query or -1 if unknown")
    private Integer totalPages;

    @ApiModelProperty("Embedded collection of data")
    private Collection<T> content;

    public Page() {
    }

    public Page(CollectionInfo<T> collectionInfo) {
        this.pageIndex = collectionInfo.getPageIndex();
        this.pageSize = collectionInfo.getPageSize();
        this.totalPages = collectionInfo.getTotalPages();
        this.content = Collections.unmodifiableCollection(collectionInfo.getContent());
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Collection<T> getContent() {
        return content;
    }

    public void setContent(Collection<T> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Page{" +
                "pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", content=" + content +
                '}';
    }
}
