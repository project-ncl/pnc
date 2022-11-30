/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildIteratorTest {

    @Mock
    private BuildRecordRepository repository;

    @Mock
    private BuildMapper mapper;

    @InjectMocks
    private BuildProviderImpl provider;

    @Before
    public void prepareMock() {
        when(mapper.toDTO(any())).thenAnswer((InvocationOnMock invocation) -> {
            BuildRecord build = invocation.getArgument(0);
            return Build.builder().id(BuildMapper.idMapper.toDto(build.getId())).build();
        });
    }

    @Test
    public void testBuildIterator() {
        SortInfo sortInfo = mock(SortInfo.class);
        Predicate<BuildRecord> predicate = mock(Predicate.class);
        mockRepository(sortInfo, predicate);

        BuildProviderImpl.BuildIterator bit;
        List<Integer> ret;

        bit = provider.new BuildIterator(1, 10, 1, sortInfo, predicate);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            Build next = bit.next();
            System.out.println("next: " + next);
            ret.add(Integer.valueOf(next.getId()));
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(1, 10, 10, sortInfo, predicate);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(Integer.valueOf(bit.next().getId()));
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(1, 10, 100, sortInfo, predicate);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(Integer.valueOf(bit.next().getId()));
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(7, 12, 100, sortInfo, predicate);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(Integer.valueOf(bit.next().getId()));
        }
        assertEquals(Arrays.asList(7, 8, 9, 10, 11, 12), ret);
    }

    private void mockRepository(SortInfo sortInfo, Predicate<BuildRecord> predicate) {
        when(repository.queryWithPredicatesUsingCursor(any(), same(sortInfo), same(predicate)))
                .thenAnswer((InvocationOnMock invocation) -> {
                    PageInfo pageInfo = invocation.getArgument(0);

                    return LongStream.range(pageInfo.getPageOffset(), pageInfo.getPageOffset() + pageInfo.getPageSize())
                            .mapToObj(Base32LongID::new)
                            .map(BuildIteratorTest::mockBuildRecord)
                            .collect(Collectors.toList());

                });
    }

    private static BuildRecord mockBuildRecord(Base32LongID i) {
        BuildRecord br = mock(BuildRecord.class);
        when(br.getId()).thenReturn(i);
        return br;
    }
}
