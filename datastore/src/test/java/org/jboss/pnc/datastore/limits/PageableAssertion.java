package org.jboss.pnc.datastore.limits;

import org.assertj.core.api.AbstractAssert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PageableAssertion extends AbstractAssert<PageableAssertion, Pageable> {

    protected PageableAssertion(Pageable actual) {
        super(actual, PageableAssertion.class);
    }

    public static PageableAssertion assertThat(Pageable pageable) {
        return new PageableAssertion(pageable);
    }

    public PageableAssertion hasSize(int size) {
        assertEquals(size, actual.getPageSize());
        return this;
    }

    public PageableAssertion hasOffset(int offset) {
        assertEquals(offset, actual.getPageNumber());
        return this;
    }

    public PageableAssertion hasNoSorting() {
        assertNull(actual.getSort());
        return this;
    }

    public PageableAssertion hasSorting(Sort.Direction direction, String... properties) {
        assertNotNull(actual.getSort());
        for(String property : properties) {
            Sort.Order orderFor = actual.getSort().getOrderFor(property);
            assertNotNull(orderFor);
            assertEquals(direction, orderFor.getDirection());
        }
        return this;
    }
}
