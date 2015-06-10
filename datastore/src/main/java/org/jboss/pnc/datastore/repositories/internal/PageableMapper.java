package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageableMapper {

    public static PageRequest map(PageInfo pageInfo, SortInfo sortInfo) {
        return new PageRequest(getPageOffset(pageInfo), getPageSize(pageInfo), getSort(sortInfo));
    }

    private static Sort getSort(SortInfo sortInfo) {
        if(sortInfo == null) {
            sortInfo = new DefaultSortInfo();
        }
        Sort.Direction direction = sortInfo.getDirection() == SortInfo.SortingDirection.ASC ? Sort.Direction.ASC :
                Sort.Direction.DESC;
        return new Sort(direction, sortInfo.getFields());
    }

    private static int getPageOffset(PageInfo pageInfo) {
        if(pageInfo == null) {
            pageInfo = new DefaultPageInfo();
        }
        return pageInfo.getPageOffset();
    }

    private static int getPageSize(PageInfo pageInfo) {
        if(pageInfo == null) {
            pageInfo = new DefaultPageInfo();
        }
        return pageInfo.getPageSize();
    }
}
