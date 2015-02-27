package org.jboss.pnc.datastore.limits;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Factory for creating RSQL friendly paging and sorting criteria.
 */
public interface RSQLPageLimitAndSortingProducer {

    Sort.Direction DEFAULT_SORTING_DIRECTION = Sort.DEFAULT_DIRECTION;
    int DEFAULT_SIZE = 50;
    int DEFAULT_OFFSET = 0;

    /**
     * Creates new Pageable object based on input data.
     *
     * @param size Size of the page
     * @param offset Offset of the page (which page are we looking at)
     * @param rsql RSQL for sorting, example <code>sort=asc=(id,name)</code>
     * @return Pageable object based on input data.
     * @throws java.lang.IllegalArgumentException on argument validation.
     */
    static Pageable fromRSQL(int size, int offset, String rsql) {
        if(rsql == null || rsql.isEmpty()) {
            return new EmptyPageLimits(size, offset).toPageRequest();
        }
        return new RSQLPageLimits(size, offset, rsql).toPageRequest();
    }

}
