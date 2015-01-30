package org.jboss.pnc.rest.pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Pagination<T> implements Serializable {

    private static final long serialVersionUID = -3324845107485449792L;
    private final List<T> content = new ArrayList<T>();
    private int pageElements;
    private long totalElements;
    private int pageIndex;
    private int totalPages;
    private int pageSize;
    private Boolean ascending;
    private String sortingField;

    public Pagination(List<T> content, int pageElements, long totalElements, int pageIndex, int totalPages, int pageSize,
            Boolean ascending, String sortingField) {

        super();
        this.content.addAll(content);
        this.pageElements = pageElements;
        this.totalElements = totalElements;
        this.pageIndex = pageIndex;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.ascending = ascending;
        this.sortingField = sortingField;
    }

    public int getPageElements() {
        return pageElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public String getSortingField() {
        return sortingField;
    }

    public List<T> getContent() {
        return content;
    }

    public boolean isFirstPage() {
        return pageIndex == 1;
    }

    public boolean isLastPage() {
        return pageIndex == totalPages;
    }

    public boolean isPrevPage() {
        return pageIndex > 1;
    }

    public boolean isNextPage() {
        return pageIndex < totalPages;
    }

    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public String toString() {
        return "Pagination [content=" + content + ", pageElements=" + pageElements + ", totalElements=" + totalElements
                + ", pageIndex=" + pageIndex + ", totalPages=" + totalPages + ", pageSize=" + pageSize + ", ascending="
                + ascending + ", sortingField=" + sortingField + "]";
    }

}
