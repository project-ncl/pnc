/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Condition;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildProviderImplTest extends AbstractProviderTest<BuildRecord> {

    private static final int CURRENT_USER = randInt(1000, 100000);

    private static final String USER_TOKEN = "token";

    @Mock
    private BuildRecordRepository repository;

    @Mock
    private BuildCoordinator buildCoordinator;

    @Mock
    private SortInfoProducer sortInfoProducer;

    @Mock
    private UserService userService;

    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private User user;

    @InjectMocks
    private BuildProviderImpl provider;

    private final List<BuildTask> runningBuilds = new ArrayList<>();

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository repository() {
        return repository;
    }

    @Before
    public void prepareMock() throws ReflectiveOperationException, IllegalArgumentException {
        when(repository.queryWithPredicatesUsingCursor(any(PageInfo.class), any(SortInfo.class), any()))
                .thenAnswer(new ListAnswer(repositoryList));
        when(repository.findByIdFetchAllProperties(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });
        when(repository.findByIdFetchProperties(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });

        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(runningBuilds);
        when(sortInfoProducer.getSortInfo(any(String.class))).thenAnswer(i -> mock(SortInfo.class));
        when(sortInfoProducer.getSortInfo(any(), any())).thenAnswer(i -> mock(SortInfo.class));
        when(rsqlPredicateProducer.getSortInfo(any(), any())).thenAnswer(i -> mock(SortInfo.class));

        user = mock(User.class);
        when(user.getId()).thenReturn(CURRENT_USER);
        when(user.getLoginToken()).thenReturn(USER_TOKEN);
        when(userService.currentUser()).thenReturn(user);
    }

    private BuildTask mockBuildTask() {
        BuildTask bt = mock(BuildTask.class);
        when(bt.getId()).thenReturn(entityId++);
        when(bt.getSubmitTime()).thenReturn(new Date(entityId * 100));

        runningBuilds.add(bt);
        return bt;
    }

    private BuildRecord mockBuildRecord() {
        BuildRecord br = BuildRecord.Builder.newBuilder().id(entityId++).submitTime(new Date(entityId * 100)).build();
        repositoryList.add(0, br);
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
        assertEquals(Integer.toString(latestRunning.getId()), builds.getContent().iterator().next().getId());
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
        assertEquals(latestBuild.getId().toString(), builds.getContent().iterator().next().getId());
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
        assertEquals(Integer.toString(build1.getId()), it.next().getId());
        assertEquals(Integer.toString(build2.getId()), it.next().getId());
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
        assertEquals(String.valueOf(build1.getId()), it.next().getId());
        assertEquals(String.valueOf(build2.getId()), it.next().getId());
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

    private void testPage(int idx, int size, Integer... ids) {
        BuildPageInfo pageInfo = new BuildPageInfo(idx, size, "", "", false, false);
        Page<Build> builds = provider.getBuilds(pageInfo);

        Iterator<Build> it = builds.getContent().iterator();
        for (Integer id : ids) {
            assertEquals(id.toString(), it.next().getId());
        }
        assertFalse(it.hasNext());
    }

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

    @Test
    public void testStore() {
        try {
            provider.store(null);
            fail("Creating build must be unsupported.");
        } catch (UnsupportedOperationException ex) {
            // ok
        }
    }

    @Test
    public void testGetSpecificFinished() {
        BuildRecord record = mockBuildRecord();

        Build specific = provider.getSpecific(Integer.toString(record.getId()));
        assertThat(specific.getId()).isEqualTo(record.getId().toString());
        assertThat(specific.getSubmitTime()).isEqualTo(record.getSubmitTime().toInstant());
    }

    @Test
    public void testGetSpecificRunning() {
        BuildTask task = mockBuildTask();

        Build specific = provider.getSpecific(Integer.toString(task.getId()));
        assertThat(specific.getId()).isEqualTo(Integer.toString(task.getId()));
        assertThat(specific.getSubmitTime()).isEqualTo(task.getSubmitTime().toInstant());
    }

    @Test
    public void testGetAll() {
        BuildRecord buildRecord1 = mockBuildRecord();
        BuildRecord buildRecord2 = mockBuildRecord();
        BuildRecord buildRecord3 = mockBuildRecord();
        Page<Build> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(3)
                .haveExactly(
                        1,
                        new Condition<>(
                                b -> buildRecord1.getSubmitTime().toInstant().equals(b.getSubmitTime()),
                                "Build present"))
                .haveExactly(
                        1,
                        new Condition<>(
                                b -> buildRecord2.getSubmitTime().toInstant().equals(b.getSubmitTime()),
                                "Build present"))
                .haveExactly(
                        1,
                        new Condition<>(
                                b -> buildRecord3.getSubmitTime().toInstant().equals(b.getSubmitTime()),
                                "Build present"));
    }

    @Test
    public void shouldGetGraphWithDependencies() {
        // With
        Integer buildSetTaskId = 1;
        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        when(buildSetTask.getId()).thenReturn(buildSetTaskId);

        BuildTask task = mockBuildTaskWithSet(buildSetTask);
        BuildTask taskDep = mockBuildTaskWithSet(buildSetTask);
        BuildTask taskDepDep = mockBuildTaskWithSet(buildSetTask);

        when(task.getDependencies()).thenReturn(asSet(taskDep));
        when(taskDep.getDependencies()).thenReturn(asSet(taskDepDep));

        // When
        Graph<BuildTask> graph = provider.getRunningBuildGraphForGroupBuild(buildSetTaskId);

        // Then
        assertThat(graph.getVerticies()).hasSize(3);
        assertThat(graph.getVerticies().stream().map(Vertex::getName)).containsExactly(
                String.valueOf(task.getId()),
                String.valueOf(taskDep.getId()),
                String.valueOf(taskDepDep.getId()));

    }

    @Test
    public void shouldPerformCallbackAfterDeletion() throws Exception {
        // given
        final int buildId = 88;
        final String buildIdString = String.valueOf(buildId);
        final String callbackUrl = "http://localhost:8088/callback";
        WireMockServer wireMockServer = new WireMockServer(8088);
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/callback")).willReturn(aResponse().withStatus(200)));

        given(temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(eq(buildId), eq(USER_TOKEN), any()))
                .willAnswer(invocation -> {
                    Result result = new Result(buildIdString, ResultStatus.SUCCESS, "Build was deleted successfully");

                    ((Consumer<Result>) invocation.getArgument(2)).accept(result);
                    return true;
                });

        // when
        boolean result = provider.delete(buildIdString, callbackUrl);

        // then
        assertThat(result).isTrue();
        wireMockServer.verify(
                1,
                postRequestedFor(urlEqualTo("/callback"))
                        .withRequestBody(matchingJsonPath("$.id", equalTo(buildIdString))));
        wireMockServer.stop();
    }

    private BuildTask mockBuildTaskWithSet(BuildSetTask buildSetTask) {
        BuildTask task = mockBuildTask();
        when(task.getBuildSetTask()).thenReturn(buildSetTask);
        when(task.getUser()).thenReturn(mock(User.class));
        return task;
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

    private Set<BuildTask> asSet(BuildTask task) {
        Set<BuildTask> set = new HashSet<>();
        set.add(task);
        return set;
    }
}
