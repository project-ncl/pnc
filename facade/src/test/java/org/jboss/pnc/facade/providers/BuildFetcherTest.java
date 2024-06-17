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

import lombok.Value;
import org.assertj.core.api.Condition;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.facade.validation.CorruptedDataException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildFetcherTest {

    public static final Comparator<Build> DEFAULT_COMPARATOR = Comparator.comparing(Build::getSubmitTime).reversed();
    private final Map<Base32LongID, BuildRecord> buildRecordDB = new HashMap<>();
    private final Map<IdRev, BuildConfigurationAudited> buildConfigurationAuditedDB = new HashMap<>();
    private final List<BuildTask> submitedBuildTasks = new ArrayList<>();
    private final SortInfo<BuildRecord> sortByTime = Mockito.mock(SortInfo.class);
    @Mock
    private BuildRecordRepository repository;
    @Mock
    private BuildMapper mapper;
    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;
    @Mock
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;
    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    @Mock
    private BuildCoordinator buildCoordinator;
    @InjectMocks
    private BuildFetcher buildFetcher;
    private MockedStatic<BuildRecordPredicates> buildRecordPredicatesMockedStatic;

    private static Build mapBuildToDTO(BuildRecord buildRecord) {
        return Build.builder()
                .id(buildRecord.getId().getId())
                .submitTime(buildRecord.getSubmitTime().toInstant())
                .build();
    }

    @Before
    public void beforeTest() throws RemoteRequestException, MissingDataException {
        buildRecordPredicatesMockedStatic = Mockito.mockStatic(BuildRecordPredicates.class);
        buildRecordPredicatesMockedStatic.when(() -> BuildRecordPredicates.withIds(Mockito.any()))
                .thenAnswer(FakePredicate::new);
        Mockito.lenient()
                .when(repository.queryWithPredicates(Mockito.any(FakePredicate.class)))
                .thenAnswer(invocation -> {
                    FakePredicate<BuildRecord> fakePredicate = invocation.getArgument(0);
                    Set<Base32LongID> ids = fakePredicate.getIds();
                    return ids.stream().map(buildRecordDB::get).filter(Objects::nonNull).collect(Collectors.toList());
                });
        Mockito.when(
                repository.queryWithPredicates(
                        Mockito.any(PageInfo.class),
                        Mockito.any(SortInfo.class),
                        Mockito.any(Predicate[].class)))
                .thenAnswer(invocation -> {
                    PageInfo pageInfo = invocation.getArgument(0);
                    SortInfo<BuildRecord> sortInfo = invocation.getArgument(1);
                    Stream<BuildRecord> stream = buildRecordDB.values().stream();
                    if (sortInfo == sortByTime) {
                        stream = stream.sorted(Comparator.comparing(BuildRecord::getSubmitTime).reversed());
                    }
                    if (sortInfo != null) {
                        stream = stream.skip(pageInfo.getElementOffset()).limit(pageInfo.getPageSize());
                    }
                    return stream.collect(Collectors.toList());
                });

        Mockito.when(buildConfigurationAuditedRepository.queryById(Mockito.anySet())).thenAnswer(invocationOnMock -> {
            Set<IdRev> ids = invocationOnMock.getArgument(0);
            return ids.stream()
                    .map(buildConfigurationAuditedDB::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(BuildConfigurationAudited::getIdRev, Function.identity()));
        });
        Mockito.when(mapper.getIdMapper()).thenReturn(BuildMapper.idMapper);
        Mockito.when(mapper.toDTO(Mockito.any()))
                .thenAnswer(invocationOnMock -> mapBuildToDTO(invocationOnMock.getArgument(0)));
        Mockito.when(mapper.toDTOWithoutBCR(Mockito.any()))
                .thenAnswer(invocationOnMock -> mapBuildToDTO(invocationOnMock.getArgument(0)));
        Mockito.when(mapper.fromBuildTask(Mockito.any())).thenAnswer(invocationOnMock -> {
            BuildTask buildTask = invocationOnMock.getArgument(0);
            return Build.builder().id(buildTask.getId()).build();
        });

        Mockito.when(buildCoordinator.getSubmittedBuildTasks()).thenReturn(submitedBuildTasks);
    }

    @After
    public void afterTest() {
        buildRecordPredicatesMockedStatic.close();
    }

    @Test
    public void testPrimeBuildDependenciesFromDB() {
        String parentIDString = "10";
        String childIDString = "20";
        Base32LongID parentID = new Base32LongID(parentIDString);
        Base32LongID childID = new Base32LongID(childIDString);
        BuildRecord parent = mockBuildRecord(parentID, childID);
        mockBuildRecord(childID);

        buildFetcher.precacheAllBuildsDeps(parentID);

        Mockito.reset(repository); // clear "db", so we know all builds were pre-cached

        BuildFetcher.BuildWithDeps parentBuildWithDependencies = buildFetcher.getBuildWithDeps(parentIDString);
        assertThat(parentBuildWithDependencies.getBuild()).isNotNull();
        assertThat(parentBuildWithDependencies.getBuild().getId()).isEqualTo(parentIDString);
        assertThat(parentBuildWithDependencies.getDependencies()).containsExactly(childIDString);

        BuildFetcher.BuildWithDeps childBuildWithDependencies = buildFetcher.getBuildWithDeps(childIDString);
        assertThat(childBuildWithDependencies.getBuild()).isNotNull();
        assertThat(childBuildWithDependencies.getBuild().getId()).isEqualTo(childIDString);
    }

    @Test
    public void testPrimeBuildDependenciesFromTaskAndDb() {
        String parentIDString = "10";
        String childIDString = "20";
        Base32LongID parentID = new Base32LongID(parentIDString);
        BuildTask childTask = mockBuildTask(childIDString);
        mockBuildTask(parentIDString, childTask);
        mockBuildRecord(new Base32LongID(childIDString));

        buildFetcher.fetchRunningBuilds();
        buildFetcher.precacheAllBuildsDeps(parentID);

        Mockito.reset(repository); // clear "db", so we know all builds were pre-cached

        BuildFetcher.BuildWithDeps parentBuildWithDependencies = buildFetcher.getBuildWithDeps(parentIDString);
        assertThat(parentBuildWithDependencies.getBuild()).isNotNull();
        assertThat(parentBuildWithDependencies.getBuild().getId()).isEqualTo(parentIDString);
        assertThat(parentBuildWithDependencies.getDependencies()).containsExactly(childIDString);

        BuildFetcher.BuildWithDeps childBuildWithDependencies = buildFetcher.getBuildWithDeps(childIDString);
        assertThat(childBuildWithDependencies.getBuild()).isNotNull();
        assertThat(childBuildWithDependencies.getBuild().getId()).isEqualTo(childIDString);
    }

    @Test
    public void testPrimeBuildDependenciesBuildNotFound() {
        String exsitingBuildIDString = "10";
        String nonExistingBuildIDString = "999";

        Base32LongID exsitingBuildID = new Base32LongID(exsitingBuildIDString);
        mockBuildRecord(exsitingBuildID);

        BuildFetcher.BuildWithDeps parentBuildWithDependencies = buildFetcher.getBuildWithDeps(exsitingBuildIDString);
        assertThat(parentBuildWithDependencies.getBuild()).isNotNull();

        assertThatThrownBy(() -> buildFetcher.getBuildWithDeps(nonExistingBuildIDString))
                .isInstanceOf(CorruptedDataException.class);
    }

    @Test
    public void testBuildAdHocCaching() {
        String exsitingBuildIDString = "10";
        String exsitingBuild2IDString = "20";

        mockBuildRecord(new Base32LongID(exsitingBuildIDString));
        mockBuildRecord(new Base32LongID(exsitingBuild2IDString));

        // when db is full, build should be loaded from db when it's not cached
        BuildFetcher.BuildWithDeps existingBuildFromDB = buildFetcher.getBuildWithDeps(exsitingBuildIDString);
        assertThat(existingBuildFromDB.getBuild()).isNotNull();

        Mockito.reset(repository); // clear "db"

        // when db is empty, build should still be loaded, because it's in cache
        BuildFetcher.BuildWithDeps existingBuildFromCache = buildFetcher.getBuildWithDeps(exsitingBuildIDString);
        assertThat(existingBuildFromCache.getBuild()).isNotNull();

        // not-cached build will not be found, when DB is empty
        assertThatThrownBy(() -> buildFetcher.getBuildWithDeps(exsitingBuild2IDString))
                .isInstanceOf(CorruptedDataException.class);
    }

    @Test
    public void testBuildExists() throws RemoteRequestException, MissingDataException {
        Base32LongID buildId = new Base32LongID(10L);
        mockBuildRecord(buildId);

        boolean found = buildFetcher.buildExists(buildId);

        assertThat(found).isTrue();
        Mockito.verify(buildCoordinator).getSubmittedBuildTasks();
        Mockito.verify(repository).queryById(Mockito.eq(buildId));
    }

    @Test
    public void testBuildExistsShouldReturnFalseWhenBuildDoesNotExist() {
        boolean found = buildFetcher.buildExists(new Base32LongID(354L));
        assertThat(found).isFalse();
    }

    @Test
    public void testGetGroupBuildContent() throws RemoteRequestException, MissingDataException {
        long groupID = 100L;
        Base32LongID build1ID = new Base32LongID(10L);
        Base32LongID build2ID = new Base32LongID(12L);
        Base32LongID build3ID = new Base32LongID(14L);
        BuildTask buildTask1 = mockBuildTask(build1ID.getId());
        BuildTask buildTask2 = mockBuildTask(build2ID.getId());
        BuildRecord buildRecord2 = mockBuildRecord(build2ID);
        BuildRecord buildRecord3 = mockBuildRecord(build3ID);
        BuildConfigSetRecord group = BuildConfigSetRecord.Builder.newBuilder()
                .temporaryBuild(false)
                .id(groupID)
                .buildRecords(Set.of(buildRecord2, buildRecord3))
                .build();

        Mockito.when(buildCoordinator.getSubmittedBuildTasksBySetId(Mockito.eq(groupID)))
                .thenReturn(List.of(buildTask1, buildTask2));
        Mockito.when(buildConfigSetRecordRepository.queryById(groupID)).thenReturn(group);

        Set<Base32LongID> foundID = buildFetcher.getGroupBuildContent(groupID);

        assertThat(foundID).contains(build1ID, build2ID, build3ID);
        Mockito.verify(buildCoordinator).getSubmittedBuildTasksBySetId(Mockito.eq(groupID));
        Mockito.verify(repository, Mockito.never()).queryById(Mockito.any());
    }

    private static class CalledOnlyWithBuildId implements ArgumentMatcher<FakePredicate<BuildRecord>> {

        private final Base32LongID buildID;

        public CalledOnlyWithBuildId(Base32LongID buildID) {
            this.buildID = buildID;
        }

        @Override
        public boolean matches(FakePredicate predicate) {
            Set<Base32LongID> ids = predicate.getIds();
            return ids.contains(buildID) && ids.size() == 1;
        }
    }

    @Test
    public void testGetGroupBuildContentShouldReturnEmptySetWhenTheGroupBuildDoesNotExist() {
        Set<Base32LongID> foundID = buildFetcher.getGroupBuildContent(354L);

        assertThat(foundID).isEmpty();
    }

    @Test
    public void testGetLatePage() {
        BuildRecord first = null;
        BuildRecord last = null;
        for (int i = 0; i <= 3000; i++) {
            BuildRecord build = mockBuildRecord(new Base32LongID(100L + i));
            if (i == (3000 - 2000)) {
                first = build;
            }
            if (i == (3000 - 2049)) {
                last = build;
            }
        }
        assertThat(first).isNotNull();
        assertThat(last).isNotNull();

        List<Build> all = buildFetcher
                .getBuildPage(40, 50, List.of(), new Predicate[0], sortByTime, DEFAULT_COMPARATOR);

        BuildRecord finalFirst = first;
        BuildRecord finalLast = last;
        assertThat(all).hasSize(50)
                .haveExactly(
                        1,
                        new Condition<>(
                                b -> finalFirst.getSubmitTime().toInstant().equals(b.getSubmitTime()),
                                "Build submitted " + finalFirst.getSubmitTime().toInstant() + " present"))
                .haveExactly(
                        1,
                        new Condition<>(
                                b -> finalLast.getSubmitTime().toInstant().equals(b.getSubmitTime()),
                                "Build submitted " + finalLast.getSubmitTime().toInstant() + " present"));
    }

    @Test
    public void testGetBuildsPages() {
        List<Build> runningBuilds = new ArrayList<>();
        // Prepare
        String build8ID = "10";
        mockBuildRecord(new Base32LongID(build8ID));
        String build7ID = "11";
        runningBuilds.add(mockBuild(build7ID));
        String build6ID = "12";
        mockBuildRecord(new Base32LongID(build6ID));
        String build5ID = "13";
        runningBuilds.add(mockBuild(build5ID));
        String build4ID = "14";
        mockBuildRecord(new Base32LongID(build4ID));
        String build3ID = "15";
        runningBuilds.add(mockBuild(build3ID));
        String build2ID = "16";
        mockBuildRecord(new Base32LongID(build2ID));
        String build1ID = "17";
        runningBuilds.add(mockBuild(build1ID));

        testPage(0, 2, runningBuilds, build1ID, build2ID);
        testPage(1, 2, runningBuilds, build3ID, build4ID);
        testPage(2, 2, runningBuilds, build5ID, build6ID);
        testPage(3, 2, runningBuilds, build7ID, build8ID);

        testPage(1, 3, runningBuilds, build4ID, build5ID, build6ID);
        testPage(2, 3, runningBuilds, build7ID, build8ID);

        testPage(2, 10, runningBuilds);
    }

    private void testPage(int idx, int size, List<Build> runningBuilds, String... ids) {
        List<Build> builds = buildFetcher
                .getBuildPage(idx, size, runningBuilds, new Predicate[0], sortByTime, DEFAULT_COMPARATOR);

        assertThat(builds).hasSize(ids.length);
        assertThat(builds).extracting(BuildRef::getId).hasSameElementsAs(Arrays.asList(ids));
    }

    private BuildTask mockBuildTask(String parentIDString, BuildTask... childs) {
        BuildTask buildTask = new BuildTask(null, null, null, null, null, parentIDString, null, null, null, null);
        for (BuildTask child : childs) {
            buildTask.addDependency(child);
        }
        submitedBuildTasks.add(buildTask);
        return buildTask;
    }

    private static Build mockBuild(String build7ID) {
        try {
            Thread.sleep(1L); // make sure new start time is in the next millisecond
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Build.builder().id(build7ID).submitTime(Instant.now()).build();
    }

    private BuildRecord mockBuildRecord(Base32LongID buildId, Base32LongID... childIDs) {
        try {
            Thread.sleep(1L); // make sure new start time is in the next millisecond
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        IdRev buildConfigIDRev = mockBuildConfig((int) buildId.getLongId());
        BuildRecord build = BuildRecord.Builder.newBuilder()
                .id(buildId)
                .buildConfigurationAuditedId(buildConfigIDRev.id)
                .buildConfigurationAuditedRev(buildConfigIDRev.rev)
                .submitTime(new Date())
                .dependencyBuildRecordIds(childIDs)
                .build();
        buildRecordDB.put(buildId, build);
        Mockito.when(repository.queryById(buildId)).thenReturn(build);
        return build;
    }

    private IdRev mockBuildConfig(Integer id) {
        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder().id(id).build();
        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .rev(43)
                .build();
        IdRev idRev = buildConfigurationAudited.getIdRev();
        buildConfigurationAuditedDB.put(idRev, buildConfigurationAudited);
        return idRev;
    }

    @Value
    public static class FakePredicate<T> implements Predicate<T> {
        Set<Base32LongID> ids;

        public FakePredicate(InvocationOnMock invocation) {
            ids = new HashSet<>(invocation.getArgument(0));
        }

        @Override
        public javax.persistence.criteria.Predicate apply(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            throw new IllegalStateException("This is fake");
        }
    }
}
