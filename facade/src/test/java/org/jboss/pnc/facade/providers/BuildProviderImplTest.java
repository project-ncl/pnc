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
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.remotecoordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Edge;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.Vertex;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.validation.CorruptedDataException;
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
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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
    private BuildFetcher buildFetcher;

    @Mock
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;

    @Mock
    private KeycloakServiceClient keycloakServiceClient;

    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

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
        when(buildCoordinator.getSubmittedBuildTasksBySetId(any())).thenAnswer(inv -> {
            Base32LongID bcsrid = inv.getArgument(0);
            return runningBuilds.stream()
                    .filter(
                            task -> task.getBuildConfigSetRecordId() != null
                                    && task.getBuildConfigSetRecordId().equals(bcsrid))
                    .collect(Collectors.toList());
        });
        when(buildCoordinator.getSubmittedBuildTask(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            return runningBuilds.stream().filter(task -> task.getId().equals(id)).findFirst();
        });
        when(rsqlPredicateProducer.getSortInfo(any(Class.class), any())).thenAnswer(i -> mock(SortInfo.class));

        when(keycloakServiceClient.getAuthToken()).thenReturn(USER_TOKEN);

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .id(new Base32LongID(1L))
                .temporaryBuild(false)
                .build();
        when(buildConfigSetRecordRepository.queryById(any())).thenReturn(buildConfigSetRecord);

        when(buildFetcher.getBuildWithDeps(any()))
                .thenAnswer(invocation -> getBuildWithDependenecies(invocation.getArgument(0)));

        when(buildFetcher.buildExists(any())).thenAnswer(invocation -> {
            Base32LongID id = invocation.getArgument(0);
            return runningBuilds.stream().anyMatch(b -> b.getId().equals(id.getId()))
                    || repositoryList.stream().anyMatch(b -> b.getId().equals((id)));
        });
    }

    private BuildFetcher.BuildWithDeps getBuildWithDependenecies(String id) {
        Optional<BuildFetcher.BuildWithDeps> first = runningBuilds.stream()
                .filter(b -> b.getId().equals(id))
                .map(bt -> new BuildFetcher.BuildWithDeps(buildMapper.fromBuildTask(bt), bt))
                .findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return repositoryList.stream()
                .filter(b -> b.getId().getId().equals((id)))
                .map(b -> new BuildFetcher.BuildWithDeps(buildMapper.toDTO(b), b))
                .findFirst()
                .orElseThrow(() -> new CorruptedDataException("Corrupted"));
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
        Build dto = buildMapper.toDTO(givenBuild);

        when(buildFetcher.getBuildPage(eq(0), eq(10), any(), any(), any(), any())).thenReturn(List.of(dto));
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
        Build build2 = buildMapper.toDTO(mockBuildRecord()); // hit
        Build build1 = buildMapper.fromBuildTask(mockBuildTask()); // hit

        when(buildFetcher.getBuildPage(eq(0), eq(2), any(), any(), any(), any())).thenReturn(List.of(build1, build2));

        // When
        BuildPageInfo pageInfo = new BuildPageInfo(0, 2, "", "", false, false, "");
        Page<Build> builds = provider.getBuilds(pageInfo);

        // Verify
        assertEquals(8, builds.getTotalHits());
        assertEquals(2, builds.getContent().size());
        Iterator<Build> it = builds.getContent().iterator();
        assertEquals(build1.getId(), it.next().getId());
        assertEquals(build2.getId(), it.next().getId());
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
        Build buildRecord1 = buildMapper.toDTO(mockBuildRecord());
        Build buildRecord2 = buildMapper.toDTO(mockBuildRecord());
        Build buildRecord3 = buildMapper.toDTO(mockBuildRecord());

        when(buildFetcher.getBuildPage(eq(0), eq(10), any(), any(), any(), any()))
                .thenReturn(List.of(buildRecord3, buildRecord2, buildRecord1));

        Page<Build> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(3)
                .haveExactly(1, new Condition<>(b -> buildRecord1.getId().equals(b.getId()), "Build present"))
                .haveExactly(1, new Condition<>(b -> buildRecord2.getId().equals(b.getId()), "Build present"))
                .haveExactly(1, new Condition<>(b -> buildRecord3.getId().equals(b.getId()), "Build present"));
    }

    @Test
    public void shouldGetGraphWithDependencies() {
        // With
        Base32LongID configSetRecordId = new Base32LongID(1L);
        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        BuildConfigSetRecord setRecord = mock(BuildConfigSetRecord.class);
        Optional<BuildConfigSetRecord> optional = Optional.of(setRecord);
        when(setRecord.getId()).thenReturn(configSetRecordId);
        when(buildSetTask.getBuildConfigSetRecord()).thenReturn(optional);

        BuildTask task = mockBuildTaskWithSet(buildSetTask);
        Base32LongID taskId = BuildMapper.idMapper.toEntity(task.getId());
        BuildTask taskDep = mockBuildTaskWithSet(buildSetTask);
        Base32LongID taskDepId = BuildMapper.idMapper.toEntity(taskDep.getId());
        BuildTask taskDepDep = mockBuildTaskWithSet(buildSetTask);
        Base32LongID taskDepDepId = BuildMapper.idMapper.toEntity(taskDepDep.getId());

        when(task.getDependencies()).thenReturn(Collections.singleton(taskDep));
        when(taskDep.getDependencies()).thenReturn(Collections.singleton(taskDepDep));

        when(buildFetcher.getGroupBuildContent(eq(configSetRecordId)))
                .thenReturn(Set.of(taskId, taskDepId, taskDepDepId));

        // When
        Graph<Build> graph = provider.getBuildGraphForGroupBuild(configSetRecordId.getId());

        // Then
        assertThat(graph.getVertices()).hasSize(3);
        assertThat(graph.getVertices().values().stream().map(Vertex::getName))
                .containsExactlyInAnyOrder(task.getId(), taskDep.getId(), taskDepDep.getId());
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

        given(temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuild(eq(buildId), any())).willAnswer(invocation -> {
            Result result = new Result(buildIdString, ResultStatus.SUCCESS, "Build was deleted successfully");

            ((Consumer<Result>) invocation.getArgument(1)).accept(result);
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
        Base32LongID id = record.getId();
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
