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

import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class RSQLPageLimitsTest {

    @Test
    public void shouldReturnCustomLimits() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = null;

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSize(12).hasOffset(13).hasNoSorting();
    }

    @Test
    public void shouldParseSimpleAscendingRSQL() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = "sort=asc=id";

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSorting(Sort.Direction.ASC, "id");
    }

    @Test
    public void shouldParseSimpleDescendingRSQL() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = "sort=desc=id";

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSorting(Sort.Direction.DESC, "id");
    }

    @Test
    public void shouldParseComplicatedAscendingRSQL() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = "sort=asc=(id,name)";

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSorting(Sort.Direction.ASC, "id", "name");
    }

    @Test
    public void shouldParseComplicatedDescendingRSQL() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = "sort=desc=(id,name)";

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSorting(Sort.Direction.DESC, "id", "name");
    }

    @Test
    public void shouldAllowSortingStringWithoutSortAtTheBeginning() throws Exception {
        //given
        int size = 12;
        int offset = 13;
        String sorting = "=desc=(id,name)";

        //when
        Pageable testedLimits = RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);

        //then
        PageableAssertion.assertThat(testedLimits).hasSorting(Sort.Direction.DESC, "id", "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNegativeSize() throws Exception {
        //given
        int size = -12;
        int offset = 0;
        String sorting = null;

        //when
        RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNegativeOffset() throws Exception {
        //given
        int size = 1;
        int offset = -112;
        String sorting = null;

        //when
        RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectZeroSize() throws Exception {
        //given
        int size = 0;
        int offset = 0;
        String sorting = null;

        //when
        RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectWrongRSQL() throws Exception {
        //given
        int size = 1;
        int offset = 1;
        String sorting = "Yeah, this is a wrong rsql";

        //when
        RSQLPageLimitAndSortingProducer.fromRSQL(size, offset, sorting);
    }

}