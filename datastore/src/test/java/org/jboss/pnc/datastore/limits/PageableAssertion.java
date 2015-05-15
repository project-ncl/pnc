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
