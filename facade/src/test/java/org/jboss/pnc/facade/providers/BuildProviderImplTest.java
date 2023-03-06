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

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Condition;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Vertex;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.CorruptedDataException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BuildProviderImplTest extends AbstractBase32LongIDProviderTest<BuildRecord> {

    private static final int CURRENT_USER = randInt(1000, 100000);

    private static final String USER_TOKEN = "token";

    private final Logger logger = LoggerFactory.getLogger(BuildProviderImplTest.class);

    private static int buildConfigNum = 1;

    @Mock
    private BuildRecordRepository repository;

    @Mock
    private BuildCoordinator buildCoordinator;

    @Mock
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Mock
    private UserService userService;

    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    private User user;

    @InjectMocks
    private BuildProviderImpl provider;

    private final List<BuildTask> runningBuilds = new ArrayList<>();

    private static final AtomicInteger intId = new AtomicInteger();

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository repository() {
        return repository;
    }

    @Before
    public void prepareMock() throws Exception {
        when(repository.findByIdFetchProperties(any())).thenAnswer(inv -> {
            Base32LongID id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });

        when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(runningBuilds);
        when(buildCoordinator.getSubmittedBuildTasksBySetId(anyLong())).thenAnswer(inv -> {
            long bcsrid = inv.getArgument(0);
            return runningBuilds.stream()
                    .filter(
                            task -> task.getBuildConfigSetRecordId() != null
                                    && task.getBuildConfigSetRecordId().equals(bcsrid))
                    .collect(Collectors.toList());
        });
        when(rsqlPredicateProducer.getSortInfo(any(), any())).thenAnswer(i -> mock(SortInfo.class));

        user = mock(User.class);
        when(user.getLoginToken()).thenReturn(USER_TOKEN);
        when(userService.currentUser()).thenReturn(user);

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .id(1L)
                .temporaryBuild(false)
                .build();
        when(buildConfigSetRecordRepository.queryById(any())).thenReturn(buildConfigSetRecord);
    }

    private BuildTask mockBuildTask() {
        return mockBuildTask("buildConfigNumero" + (buildConfigNum++));
    }

    private BuildTask mockBuildTask(String buildConfigName) {
        BuildConfigurationAudited bca = mock(BuildConfigurationAudited.class);
        when(bca.getName()).thenReturn(buildConfigName);

        BuildTask bt = mock(BuildTask.class);
        when(bt.getId()).thenReturn(getNextId().getId());
        when(bt.getSubmitTime()).thenReturn(new Date());
        when(bt.getBuildConfigurationAudited()).thenReturn(bca);

        try {
            Thread.sleep(1L); // make sure there are no two builds with the same start date
        } catch (InterruptedException e) {
            logger.error("I can get no sleep.", e);
        }
        runningBuilds.add(bt);
        return bt;
    }

    private BuildRecord mockBuildRecord() {
        return mockBuildRecord(getNextId(), new Long[0], new Long[0]);
    }

    private BuildRecord mockBuildRecord(Base32LongID buildRecordId, Long[] dependencies, Long[] dependents) {
        Integer buildConfigurationId = intId.incrementAndGet();
        Base32LongID[] depcies = Arrays.stream(dependencies).map(Base32LongID::new).toArray(Base32LongID[]::new);
        Base32LongID[] depts = Arrays.stream(dependents).map(Base32LongID::new).toArray(Base32LongID[]::new);
        BuildRecord br = BuildRecord.Builder.newBuilder()
                .id(buildRecordId)
                .dependencyBuildRecordIds(depcies)
                .dependentBuildRecordIds(depts)
                .submitTime(new Date())
                .buildConfigurationAudited(
                        BuildConfigurationAudited.Builder.newBuilder()
                                .rev(1)
                                .buildConfiguration(
                                        BuildConfiguration.Builder.newBuilder()
                                                .id(buildConfigurationId)
                                                .name(buildConfigurationId.toString())
                                                .build())
                                .build())
                .buildConfigurationAuditedId(buildConfigurationId)
                .buildConfigurationAuditedRev(1)
                .build();
        try {
            Thread.sleep(1L); // make sure there are no two builds with the same start date
        } catch (InterruptedException e) {
            logger.error("I can get no sleep.", e);
        }
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
        BuildPageInfo pageInfo = new BuildPageInfo(0, 10, "", "", true, true, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(1, builds.getTotalHits());
        assertEquals(latestRunning.getId(), builds.getContent().iterator().next().getId());
    }

    @Test
    public void testGetLatestBuild() {
        // Prepare
        mockBuildTask();
        mockBuildTask();
        mockBuildRecord();
        mockBuildTask();
        BuildRecord latestBuild = mockBuildRecord();
        logger.debug("Task id: {}", latestBuild.getId());

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 10, "", "", true, false, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(1, builds.getTotalHits());
        assertEquals(BuildMapper.idMapper.toDto(latestBuild.getId()), builds.getContent().iterator().next().getId());
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
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(4, builds.getTotalHits());
        assertEquals(2, builds.getContent().size());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(build1.getId(), it.next().getId());
        assertEquals(build2.getId(), it.next().getId());
    }

    @Test
    public void testFilterRunningBuildsByBuildConfigName() {
        // Given
        mockBuildRecord();
        mockBuildTask();

        String givenBcName = "bcName";
        BuildTask givenBT = mockBuildTask(givenBcName);

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, givenBcName);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(1, builds.getTotalHits());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(givenBT.getId(), it.next().getId());
    }

    @Test
    public void testFilterLikeRunningBuildsByBuildConfigName() {
        // Given
        mockBuildRecord();
        mockBuildTask();

        String givenBcName = "VeryLongAndComplicatedBcName";
        String givenBcNamePattern = "*LongAndComplicated*";
        BuildTask givenBT = mockBuildTask(givenBcName);

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, givenBcNamePattern);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(1, builds.getTotalHits());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(givenBT.getId(), it.next().getId());
    }

    @Test
    public void testFailFilterLikeRunningBuildsByBuildConfigName() {
        // Given
        mockBuildRecord();
        mockBuildTask();

        String givenBcName = "VeryLongAndComplicatedBcName";
        String givenBcNamePattern = "LongAndComplicated*";
        BuildTask givenBT = mockBuildTask(givenBcName);

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, givenBcNamePattern);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(0, builds.getTotalHits());
    }

    @Test
    public void testFailFilterLikeRunningBuildsByBuildConfigNameWithWildcard() {
        // Given
        mockBuildRecord();
        mockBuildTask();

        String givenBcName = "VeryLongAndComplicatedBcName";
        String givenBcNamePattern = "LongAndComplicated%";
        BuildTask givenBT = mockBuildTask(givenBcName);

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, givenBcNamePattern);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(0, builds.getTotalHits());
    }

    @Test
    public void testFilterLikeRunningBuildsByBuildConfigNameAsReceivedFromUI() {
        // Given
        mockBuildRecord();
        mockBuildTask();

        String givenBcName = "VeryLongAndComplicatedBcName";
        // UI filters always append and prepend %
        String givenBcNamePattern = "%LongAndComplicated*%";
        BuildTask givenBT = mockBuildTask(givenBcName);

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, true, givenBcNamePattern);
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(1, builds.getTotalHits());
    }

    @Test
    public void testFilterFinishedBuildsByBuildConfigName() {
        // Given
        Base32LongID givenIdAndBcName = new Base32LongID(85792L);

        mockBuildTask();
        mockBuildTask();
        BuildRecord givenBuild = mockBuildRecord(givenIdAndBcName, new Long[0], new Long[0]);

        when(buildConfigurationAuditedRepository.searchIdRevForBuildConfigurationName(givenIdAndBcName.toString()))
                .thenReturn(Stream.of(givenBuild.getBuildConfigurationAuditedIdRev()).collect(Collectors.toList()));

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 10, "", "", false, false, givenIdAndBcName.toString());
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Then
        assertEquals(1, builds.getTotalHits());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(BuildMapper.idMapper.toDto(givenBuild.getId()), it.next().getId());
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
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, false, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(8, builds.getTotalHits());
        assertEquals(2, builds.getContent().size());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(build1.getId(), it.next().getId());
        assertEquals(BuildMapper.idMapper.toDto(build2.getId()), it.next().getId());
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

        testPage(0, 2, new Base32LongID(build1.getId()), build2.getId());
        testPage(1, 2, new Base32LongID(build3.getId()), build4.getId());
        testPage(2, 2, new Base32LongID(build5.getId()), build6.getId());
        testPage(3, 2, new Base32LongID(build7.getId()), build8.getId());

        testPage(1, 3, build4.getId(), new Base32LongID(build5.getId()), build6.getId());
        testPage(2, 3, new Base32LongID(build7.getId()), build8.getId());

        testPage(2, 10);
    }

    private void testPage(int idx, int size, Base32LongID... ids) {
        BuildPageInfo pageInfo = new BuildPageInfo(idx, size, "", "", false, false, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        Iterator<Build> it = builds.getContent().iterator();
        for (Base32LongID id : ids) {
            assertEquals(BuildMapper.idMapper.toDto(id), it.next().getId());
        }
        assertFalse(it.hasNext());
    }

    public void testBuildIterator() {
        SortInfo<BuildRecord> sortInfo = mock(SortInfo.class);
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

        Build specific = provider.getSpecific(BuildMapper.idMapper.toDto(record.getId()));
        assertThat(specific.getId()).isEqualTo(BuildMapper.idMapper.toDto(record.getId()));
        assertThat(specific.getSubmitTime()).isEqualTo(record.getSubmitTime().toInstant());
    }

    @Test
    public void testGetSpecificRunning() {
        BuildTask task = mockBuildTask();

        Build specific = provider.getSpecific(task.getId());
        assertThat(specific.getId()).isEqualTo(task.getId());
        assertThat(specific.getSubmitTime()).isEqualTo(task.getSubmitTime().toInstant());
    }

    @Test
    public void testGetAll() throws InterruptedException {
        BuildRecord buildRecord1 = mockBuildRecord();
        Thread.sleep(1L); // make sure new start time is in the next millisecond
        BuildRecord buildRecord2 = mockBuildRecord();
        Thread.sleep(1L); // make sure new start time is in the next millisecond
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
        Long configSetRecordId = 1L;
        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        BuildConfigSetRecord setRecord = mock(BuildConfigSetRecord.class);
        Optional<BuildConfigSetRecord> optional = Optional.of(setRecord);
        when(setRecord.getId()).thenReturn(configSetRecordId);
        when(buildSetTask.getBuildConfigSetRecord()).thenReturn(optional);

        BuildTask task = mockBuildTaskWithSet(buildSetTask);
        BuildTask taskDep = mockBuildTaskWithSet(buildSetTask);
        BuildTask taskDepDep = mockBuildTaskWithSet(buildSetTask);

        when(task.getDependencies()).thenReturn(Collections.singleton(taskDep));
        when(taskDep.getDependencies()).thenReturn(Collections.singleton(taskDepDep));

        // When
        Graph<Build> graph = provider.getBuildGraphForGroupBuild(Long.toString(configSetRecordId));

        // Then
        assertThat(graph.getVertices()).hasSize(3);
        List<String> buildTaskIDsOrderedByBCName = Stream.of(task, taskDep, taskDepDep)
                .sorted(Comparator.comparing(t -> t.getBuildConfigurationAudited().getName()))
                .map(t -> t.getId())
                .collect(Collectors.toList());
        assertThat(graph.getVertices().values().stream().map(Vertex::getName))
                .containsExactlyElementsOf(buildTaskIDsOrderedByBCName);
    }

    @Test(expected = EmptyEntityException.class)
    public void shouldThrowAnExceptionWhenTheGroupDoesNotExist() {
        // Given some group
        when(buildConfigSetRecordRepository.queryById(42L)).thenReturn(null);

        // When getting non-existing group
        Graph<Build> graph = provider.getBuildGraphForGroupBuild("42");

        // Then should throw
    }

    @Test
    public void dependencyGraphTest() {
        // given
        BuildTask bt100002 = mock(BuildTask.class);
        when(bt100002.getId()).thenReturn("100002");
        BuildTask bt110000 = mock(BuildTask.class);
        when(bt110000.getId()).thenReturn("110000");
        when(bt110000.getDependencies()).thenReturn(Collections.emptySet());
        when(bt110000.getDependants()).thenReturn(Collections.singleton(bt100002));
        runningBuilds.add(bt110000);

        mockBuildRecord(new Base32LongID(100000L), new Long[] { 100002L }, new Long[] {});
        mockBuildRecord(new Base32LongID(100001L), new Long[] { 100002L }, new Long[] {});

        BuildRecord currentBuild = mockBuildRecord(
                new Base32LongID(100002L),
                new Long[] { 100003L, 100005L, 100006L },
                new Long[] { 100000L, 100001L, Long.valueOf(bt110000.getId()) });

        BuildRecord buildRecord100003 = mockBuildRecord(
                new Base32LongID(100003L),
                new Long[] { 100004L },
                new Long[] { 100002L });
        mockBuildRecord(new Base32LongID(100004L), new Long[] {}, new Long[] { 100003L });
        mockBuildRecord(new Base32LongID(100005L), new Long[] {}, new Long[] { 100002L });
        mockBuildRecord(new Base32LongID(100006L), new Long[] {}, new Long[] { 100002L });

        // when
        Graph<Build> dependencyGraph = provider.getDependencyGraph(bt100002.getId());

        // then
        logger.info("Graph: {}", dependencyGraph.toString());
        assertEquals(8, dependencyGraph.getVertices().size());

        Vertex<Build> vertex = getBuildVertexByName(dependencyGraph, BuildMapper.idMapper.toDto(currentBuild.getId()));
        Build build = vertex.getData();
        assertEquals(BuildMapper.idMapper.toDto(currentBuild.getId()), build.getId());
        assertEquals(4, getOutgoingEdges(dependencyGraph, vertex).count());
        assertEquals(3, getIncommingEdges(dependencyGraph, vertex).count());

        Vertex<Build> vertex3 = getBuildVertexByName(
                dependencyGraph,
                BuildMapper.idMapper.toDto(buildRecord100003.getId()));
        assertEquals(1, getOutgoingEdges(dependencyGraph, vertex3).count());
        assertEquals(1, getIncommingEdges(dependencyGraph, vertex3).count());

        // then from running build
        Graph<Build> dependencyGraphFromRunning = provider.getDependencyGraph(bt110000.getId());
        Vertex<Build> runningVertex = getBuildVertexByName(dependencyGraphFromRunning, bt110000.getId());
        assertEquals(1, getOutgoingEdges(dependencyGraphFromRunning, runningVertex).count());
        assertEquals(1, getIncommingEdges(dependencyGraphFromRunning, runningVertex).count());
    }

    @Test(expected = CorruptedDataException.class)
    public void shouldThrowCorruptedDataExceptionTest() {
        // given
        mockBuildRecord(new Base32LongID(200000L), new Long[] {}, new Long[] {});
        BuildRecord buildRecord = mockBuildRecord(
                new Base32LongID(200001L),
                new Long[] { 200000L, 220000L },
                new Long[] {});

        // when
        Graph<Build> dependencyGraph = provider.getDependencyGraph(BuildMapper.idMapper.toDto(buildRecord.getId()));
    }

    protected Stream<Edge<Build>> getIncommingEdges(Graph<Build> dependencyGraph, Vertex<Build> vertex2) {
        return dependencyGraph.getEdges().stream().filter(e -> e.getTarget().equals(vertex2.getName()));
    }

    protected Stream<Edge<Build>> getOutgoingEdges(Graph<Build> dependencyGraph, Vertex<Build> vertex2) {
        return dependencyGraph.getEdges().stream().filter(e -> e.getSource().equals(vertex2.getName()));
    }

    protected Vertex<Build> getBuildVertexByName(Graph<Build> dependencyGraph, String name) {
        return dependencyGraph.getVertices()
                .values()
                .stream()
                .filter(v -> v.getName().equals(name))
                .findAny()
                .orElse(null);
    }

    @Test
    public void shouldPerformCallbackAfterDeletion() throws Exception {
        // given
        final Base32LongID buildId = new Base32LongID(88L);
        final String buildIdString = BuildMapper.idMapper.toDto(buildId);
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
        BuildConfigSetRecord record = buildSetTask.getBuildConfigSetRecord().get();
        Long id = record.getId();
        when(task.getBuildConfigSetRecordId()).thenReturn(id);
        when(task.getUser()).thenReturn(mock(User.class));
        return task;
    }

    private void mockRepository(SortInfo<BuildRecord> sortInfo, Predicate<BuildRecord> predicate) {
        when(repository.queryWithPredicates(any(), same(sortInfo), same(predicate)))
                .thenAnswer((InvocationOnMock invocation) -> {
                    PageInfo pageInfo = invocation.getArgument(0);

                    return LongStream.range(pageInfo.getPageOffset(), pageInfo.getPageOffset() + pageInfo.getPageSize())
                            .mapToObj(Base32LongID::new)
                            .map(BuildProviderImplTest::mockBuildRecord)
                            .collect(Collectors.toList());

                });
    }

    private static BuildRecord mockBuildRecord(Base32LongID i) {
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
