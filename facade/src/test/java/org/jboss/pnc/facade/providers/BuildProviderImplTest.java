/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.BuildMapper;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jboss.pnc.facade.rsql.RSQLProducer;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;


/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildProviderImplTest {

    @Mock
    private BuildRecordRepository repository;

    @Mock
    private BuildCoordinator buildCoordinator;

    @Mock
    private BuildMapper mapper;
    
    @Mock
    private PageInfoProducer pageInfoProducer;
    
    @Mock
    private SortInfoProducer sortInfoProducer;

    @Mock
    protected RSQLProducer rsqlPredicateProducer;

    @InjectMocks
    private BuildProviderImpl provider;

    private static int id = 1;

    private final List<BuildTask> runningBuilds = new ArrayList<>();

    private final List<BuildRecord> finishedBuilds = new ArrayList<>();
    
    

    @Before
    public void prepareMock() throws ReflectiveOperationException, IllegalArgumentException {
        AbstractProvider.class.getDeclaredField("pageInfoProducer").set(provider, pageInfoProducer);
        AbstractProvider.class.getDeclaredField("rsqlPredicateProducer").set(provider, rsqlPredicateProducer);
        when(mapper.toDTO(any())).thenAnswer((InvocationOnMock invocation) -> {
            BuildRecord build = invocation.getArgument(0);
            return Build.builder().id(build.getId()).submitTime(build.getSubmitTime().toInstant()).build();
        });
        when(mapper.fromBuildTask(any())).thenAnswer((InvocationOnMock invocation) -> {
            BuildTask build = invocation.getArgument(0);
            return Build.builder()
                    .id(build.getId())
                    .submitTime(build.getSubmitTime().toInstant())
                    .build();
        });
        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(runningBuilds);
        when(repository.queryWithPredicatesUsingCursor(any(PageInfo.class), any(SortInfo.class), any()))
                .thenAnswer(this::withFinishedBuilds);
        when(repository.queryWithPredicates(any(PageInfo.class), any(SortInfo.class), any()))
                .thenAnswer(this::withFinishedBuilds);
        when(repository.count(any())).thenAnswer(i -> finishedBuilds.size());
        when(pageInfoProducer.getPageInfo(anyInt(), anyInt())).thenAnswer(this::withPageInfo);
        when(sortInfoProducer.getSortInfo(any(String.class))).thenAnswer(i -> mock(SortInfo.class));
        when(sortInfoProducer.getSortInfo(any(), any())).thenAnswer(i -> mock(SortInfo.class));
        when(rsqlPredicateProducer.getSortInfo(any(), any())).thenAnswer(i -> mock(SortInfo.class));
    }

    private PageInfo withPageInfo(InvocationOnMock inv) {
        int offset = inv.getArgument(0);
        int size = inv.getArgument(1);
        return new PageInfo() {
            @Override
            public int getPageSize() {
                return size;
            }
            
            @Override
            public int getPageOffset() {
                return offset;
            }
        };
    }

    private List<BuildRecord> withFinishedBuilds(InvocationOnMock invocation) {
        PageInfo pageInfo = invocation.getArgument(0);
        int first = pageInfo.getPageOffset();
        int last = first + pageInfo.getPageSize();
        if (last > finishedBuilds.size()) {
            last = finishedBuilds.size();
        }
        if (first > last) {
            first = last;
        }
        List<BuildRecord> ret = new ArrayList<>(finishedBuilds);
        Collections.reverse(ret);
        return ret.subList(first, last);
    }

    private BuildTask mockBuildTask() {
        BuildTask bt = mock(BuildTask.class);
        when(bt.getId()).thenReturn(id++);
        when(bt.getSubmitTime()).thenReturn(new Date(id * 100));

        runningBuilds.add(bt);
        return bt;
    }

    private BuildRecord mockBuildRecord() {
        BuildRecord br = mock(BuildRecord.class);
        when(br.getId()).thenReturn(id++);
        when(br.getSubmitTime()).thenReturn(new Date(id * 100));

        finishedBuilds.add(br);
        return br;
    }

    @Test
    public void testGetLatestRunningBuild() {
        // Prepare
        mockBuildTask();
        mockBuildTask();
        mockBuildRecord();
        BuildTask latestRunning = mockBuildTask();
        mockBuildRecord();

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 10, "", "", true, true);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(1, builds.getTotalHits());
        assertEquals((Integer)latestRunning.getId(), builds.getContent().iterator().next().getId());
    }

    @Test
    public void testGetLatestBuild() {
        // Prepare
        mockBuildTask();
        mockBuildTask();
        mockBuildRecord();
        mockBuildTask();
        BuildRecord latestBuild = mockBuildRecord();

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 10, "", "", true, false);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(1, builds.getTotalHits());
        assertEquals(latestBuild.getId(), builds.getContent().iterator().next().getId());
    }

    @Test
    public void testGetRunningBuilds() {
        // Prepare
        mockBuildRecord();
        mockBuildTask(); // hit
        mockBuildRecord();
        mockBuildTask(); // hit
        mockBuildRecord();
        BuildTask build2 = mockBuildTask(); // hit
        mockBuildRecord();
        BuildTask build1 = mockBuildTask(); // hit

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(4, builds.getTotalHits());
        assertEquals(2, builds.getContent().size());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals((Integer)build1.getId(), it.next().getId());
        assertEquals((Integer)build2.getId(), it.next().getId());
    }

    @Test
    public void testGetBuilds() {
        // Prepare
        mockBuildRecord(); // hit
        mockBuildTask(); // hit
        mockBuildRecord(); // hit
        mockBuildTask(); // hit
        mockBuildRecord(); // hit
        mockBuildTask(); // hit
        BuildRecord build2 = mockBuildRecord(); // hit
        BuildTask build1 = mockBuildTask(); // hit

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, false);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(8, builds.getTotalHits());
        assertEquals(2, builds.getContent().size());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals((Integer)build1.getId(), it.next().getId());
        assertEquals(build2.getId(), it.next().getId());
    }
    
    
    @Test
    public void testGetBuildsPages() {
        // Prepare
        BuildRecord build8 = mockBuildRecord();
        BuildTask build7 = mockBuildTask();
        BuildRecord build6 = mockBuildRecord();
        BuildTask build5 = mockBuildTask();
        BuildRecord build4 = mockBuildRecord();
        BuildTask build3 = mockBuildTask();
        BuildRecord build2 = mockBuildRecord();
        BuildTask build1 = mockBuildTask();

        testPage(0, 2, build1.getId(), build2.getId());
        testPage(1, 2, build3.getId(), build4.getId());
        testPage(2, 2, build5.getId(), build6.getId());
        testPage(3, 2, build7.getId(), build8.getId());

        testPage(1, 3, build4.getId(), build5.getId(), build6.getId());
        testPage(2, 3, build7.getId(), build8.getId());

        testPage(2, 10);
    }

    private void testPage(int idx, int size, Integer... ids){
        BuildPageInfo pageInfo = new BuildPageInfo(idx, size, "", "", false, false);
        Page<Build> builds = provider.getBuilds(pageInfo);

        Iterator<Build> it = builds.getContent().iterator();
        for (Integer id : ids) {
            assertEquals(id, it.next().getId());
        }
        assertFalse(it.hasNext());
    }
    
    public void testBuildIterator() {
        SortInfo sortInfo = mock(SortInfo.class);
        Predicate<BuildRecord> predicate = mock(Predicate.class);
        mockRepository(sortInfo, predicate);

        BuildProviderImpl.BuildIterator bit;
        List<Integer> ret;

        bit = provider.new BuildIterator(1, 10, 1, predicate, sortInfo);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            Build next = bit.next();
            System.out.println("next: " + next);
            ret.add(next.getId());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(1, 10, 10, predicate, sortInfo);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(bit.next().getId());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(1, 10, 100, predicate, sortInfo);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(bit.next().getId());
        }
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ret);

        bit = provider.new BuildIterator(7, 12, 100, predicate, sortInfo);
        ret = new ArrayList<>();
        while (bit.hasNext()) {
            ret.add(bit.next().getId());
        }
        assertEquals(Arrays.asList(7, 8, 9, 10, 11, 12), ret);
    }

    private void mockRepository(SortInfo sortInfo, Predicate<BuildRecord> predicate) {
        when(repository.queryWithPredicatesUsingCursor(any(), same(sortInfo), same(predicate)))
                .thenAnswer((InvocationOnMock invocation) -> {
                    PageInfo pageInfo = invocation.getArgument(0);

                    return IntStream.range(pageInfo.getPageOffset(), pageInfo.getPageOffset() + pageInfo.getPageSize())
                            .mapToObj(BuildProviderImplTest::mockBuildRecord)
                            .collect(Collectors.toList());

                });
    }

    private static BuildRecord mockBuildRecord(int i) {
        BuildRecord br = mock(BuildRecord.class);
        when(br.getId()).thenReturn(i);
        return br;
    }
}
