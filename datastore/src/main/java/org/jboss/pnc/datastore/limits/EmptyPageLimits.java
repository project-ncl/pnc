package org.jboss.pnc.datastore.limits;

import com.google.common.base.Preconditions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Contains only the most important data for paging - offset and limit.
 */
public class EmptyPageLimits {

    protected final int pageOffset;
    protected final int pageLimit;

    public EmptyPageLimits(int size, int offset) {
        Preconditions.checkArgument(size > 0, "Page size must be > 0");
        Preconditions.checkArgument(offset >= 0, "Page offset must be > 0");

        pageLimit = size;
        pageOffset = offset;
    }

    public Pageable toPageRequest() {
        return new PageRequest(pageOffset, pageLimit);
    }
}
