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