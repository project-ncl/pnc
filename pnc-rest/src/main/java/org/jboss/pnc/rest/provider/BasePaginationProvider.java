package org.jboss.pnc.rest.provider;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.pnc.rest.pagination.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public abstract class BasePaginationProvider<K, T> {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 15;

    public abstract Function<? super T, ? extends K> mapper();

    public abstract String getDefaultSortingField();

    public Pagination<K> transform(Page<T> page) {
        List<K> content = null;
        if (page.getContent().isEmpty()) {
            content = Collections.emptyList();
        } else {
            content = page.getContent().stream().map(mapper()).collect(Collectors.toList());
        }
        Order order = page.getSort().iterator().next();
        return new Pagination<K>(content, page.getNumberOfElements(), page.getTotalElements(), page.getNumber() + 1,
                page.getTotalPages(), page.getSize(), order.getDirection().equals(Direction.ASC), order.getProperty());
    }

    public Integer parsePageSize(Integer pageSize) {
        if (pageSize != null) {
            pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public Sort.Direction extractSortingDirectionQueryParamValue(String sorting) {

        if (sorting != null && sorting.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        } else {
            return Sort.Direction.ASC;
        }
    }

    public String extractSortingFieldQueryParamValue(String field) {

        if (field != null && !"".equalsIgnoreCase(field)) {
            return field;
        } else {
            return getDefaultSortingField();
        }
    }

    public PageRequest buildPageRequest(Integer pageIndex, Integer pageSize, String field, String sorting) {

        pageSize = parsePageSize(pageSize);

        if (pageIndex == null) {
            pageIndex = 1;
        }

        return new PageRequest(pageIndex - 1, pageSize, extractSortingDirectionQueryParamValue(sorting),
                extractSortingFieldQueryParamValue(field));
    }

    public boolean noPaginationRequired(Integer pageIndex, Integer pageSize, String field, String sorting) {
        return (pageIndex == null && pageSize == null && field == null && sorting == null);
    }

}
