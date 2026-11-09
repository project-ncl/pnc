/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.common.json.moduleconfig.BuildTriggeringConfig;
import org.jboss.pnc.common.json.moduleconfig.BuildTriggeringConfig.CategoryConfig;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.facade.util.HibernateLazyInitializer;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.exception.BuildTriggeringException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests the project allow-list ({@code allowedProjects}) of {@link BuildTriggeringConfig} as enforced by
 * {@link BuildTriggererImpl} on all triggering paths. The allow-list applies to persistent builds only.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BuildTriggererImplTest {

    private static final String ALLOWED_PROJECT = "Bouncycastle";
    private static final String OTHER_PROJECT = "termd";

    @Mock
    private UserService user;

    @Mock
    private BuildCoordinator buildCoordinator;

    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;

    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Mock
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Mock
    private HibernateLazyInitializer hibernateLazyInitializer;

    @Mock
    private GenericSettingProvider genericSettingProvider;

    @Spy
    private BuildTriggeringConfig buildTriggeringConfig = new BuildTriggeringConfig();

    @InjectMocks
    private BuildTriggererImpl buildTriggerer;

    private int nextId = 1;

    @Before
    public void setUp() {
        buildTriggeringConfig.addCategoryConfig(BuildCategory.STANDARD.name(), new CategoryConfig(true, true, null));
        buildTriggeringConfig.addCategoryConfig(
                BuildCategory.LIGHTWELL.name(),
                new CategoryConfig(true, true, Set.of(ALLOWED_PROJECT)));
        buildTriggeringConfig.addCategoryConfig(
                BuildCategory.LIGHTWELL_NOVEL.name(),
                new CategoryConfig(true, true, Set.of(ALLOWED_PROJECT)));
        // persistent builds disabled altogether, regardless of the project list
        buildTriggeringConfig.addCategoryConfig(
                BuildCategory.LIGHTWELL_UPSTREAM.name(),
                new CategoryConfig(false, true, Set.of(ALLOWED_PROJECT)));
        // an empty allowedProjects list means no restriction
        buildTriggeringConfig.addCategoryConfig(BuildCategory.SERVICE.name(), new CategoryConfig(true, true, Set.of()));
    }

    @Test
    public void shouldTriggerPersistentLightwellBuildOfAllowedProject() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL);
        mockSingleBuildResult(bc.getId());

        String buildId = buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), persistent());

        assertThat(buildId).isEqualTo("build-" + bc.getId());
    }

    @Test
    public void shouldRejectPersistentLightwellBuildOfNotAllowedProject() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(OTHER_PROJECT, BuildCategory.LIGHTWELL_NOVEL);

        assertThatThrownBy(() -> buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), persistent()))
                .isInstanceOf(BuildTriggeringException.class)
                .hasMessageContaining("persistent builds of project '" + OTHER_PROJECT + "'")
                .hasMessageContaining(BuildCategory.LIGHTWELL_NOVEL.name());
        verify(buildCoordinator, never()).buildConfig(any(), any(), any());
    }

    @Test
    public void shouldTriggerTemporaryLightwellBuildOfNotAllowedProject() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(OTHER_PROJECT, BuildCategory.LIGHTWELL_NOVEL);
        mockSingleBuildResult(bc.getId());

        String buildId = buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), temporary());

        assertThat(buildId).isEqualTo("build-" + bc.getId());
    }

    @Test
    public void shouldNotRestrictProjectsWhenAllowedProjectsIsEmpty() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(OTHER_PROJECT, BuildCategory.SERVICE);
        mockSingleBuildResult(bc.getId());

        String buildId = buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), persistent());

        assertThat(buildId).isEqualTo("build-" + bc.getId());
    }

    @Test
    public void shouldNotRestrictProjectsOfCategoryWithoutAllowedProjects() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(OTHER_PROJECT, BuildCategory.STANDARD);
        mockSingleBuildResult(bc.getId());

        String buildId = buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), persistent());

        assertThat(buildId).isEqualTo("build-" + bc.getId());
    }

    @Test
    public void shouldCheckBuildKindBeforeProject() {
        // persistent builds of LIGHTWELL_UPSTREAM are disabled, even for an allowed project
        BuildConfiguration bc = mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL_UPSTREAM);

        assertThatThrownBy(() -> buildTriggerer.triggerBuild(bc.getId(), OptionalInt.empty(), persistent()))
                .isInstanceOf(BuildTriggeringException.class)
                .hasMessageContaining("Triggering persistent builds is currently disabled");
    }

    @Test
    public void shouldUseProjectOfRequestedRevision() throws Exception {
        // the current revision belongs to the allowed project, but the requested revision does not
        BuildConfiguration bc = mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL);
        BuildConfigurationAudited audited = audited(bc, 7, OTHER_PROJECT, BuildCategory.LIGHTWELL);
        when(buildConfigurationAuditedRepository.queryById(new IdRev(bc.getId(), 7))).thenReturn(audited);

        assertThatThrownBy(() -> buildTriggerer.triggerBuild(bc.getId(), OptionalInt.of(7), persistent()))
                .isInstanceOf(BuildTriggeringException.class)
                .hasMessageContaining("project '" + OTHER_PROJECT + "'");
        verify(buildCoordinator, never()).buildConfigurationAudited(any(), any(), any());
    }

    @Test
    public void shouldTriggerRequestedRevisionOfAllowedProject() throws Exception {
        BuildConfiguration bc = mockBuildConfiguration(OTHER_PROJECT, BuildCategory.LIGHTWELL);
        BuildConfigurationAudited audited = audited(bc, 7, ALLOWED_PROJECT, BuildCategory.LIGHTWELL);
        when(buildConfigurationAuditedRepository.queryById(new IdRev(bc.getId(), 7))).thenReturn(audited);
        BuildSetTask buildSetTask = buildSetTaskFor(bc.getId());
        when(buildCoordinator.buildConfigurationAudited(any(), any(), any())).thenReturn(buildSetTask);

        String buildId = buildTriggerer.triggerBuild(bc.getId(), OptionalInt.of(7), persistent());

        assertThat(buildId).isEqualTo("build-" + bc.getId());
    }

    @Test
    public void shouldRejectPersistentGroupBuildContainingNotAllowedLightwellProject() throws Exception {
        BuildConfigurationSet group = mockGroup(
                mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL),
                mockBuildConfiguration(OTHER_PROJECT, BuildCategory.STANDARD),
                mockBuildConfiguration(OTHER_PROJECT, BuildCategory.LIGHTWELL));

        assertThatThrownBy(() -> buildTriggerer.triggerGroupBuild(group.getId(), Optional.empty(), persistent()))
                .isInstanceOf(BuildTriggeringException.class)
                .hasMessageContaining("project '" + OTHER_PROJECT + "'")
                .hasMessageContaining(BuildCategory.LIGHTWELL.name());
        verify(buildCoordinator, never()).buildSet(any(), any(), any(), any());
    }

    @Test
    public void shouldTriggerPersistentGroupBuildWithAllowedLightwellProjects() throws Exception {
        BuildConfigurationSet group = mockGroup(
                mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL),
                mockBuildConfiguration(OTHER_PROJECT, BuildCategory.STANDARD));
        mockGroupBuildResult();

        String groupBuildId = buildTriggerer.triggerGroupBuild(group.getId(), Optional.empty(), persistent());

        assertThat(groupBuildId).isEqualTo(new Base32LongID(42L).getId());
    }

    @Test
    public void shouldTriggerTemporaryGroupBuildContainingNotAllowedLightwellProject() throws Exception {
        BuildConfigurationSet group = mockGroup(
                mockBuildConfiguration(ALLOWED_PROJECT, BuildCategory.LIGHTWELL),
                mockBuildConfiguration(OTHER_PROJECT, BuildCategory.LIGHTWELL));
        mockGroupBuildResult();

        String groupBuildId = buildTriggerer.triggerGroupBuild(group.getId(), Optional.empty(), temporary());

        assertThat(groupBuildId).isEqualTo(new Base32LongID(42L).getId());
    }

    private void mockGroupBuildResult() throws Exception {
        BuildConfigSetRecord record = mock(BuildConfigSetRecord.class);
        when(record.getId()).thenReturn(new Base32LongID(42L));
        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        when(buildSetTask.getBuildConfigSetRecord()).thenReturn(Optional.of(record));
        when(buildCoordinator.buildSet(any(), any(), any(), any())).thenReturn(buildSetTask);
    }

    private BuildConfiguration mockBuildConfiguration(String projectName, BuildCategory category) {
        int id = nextId++;
        Project project = Project.Builder.newBuilder().id(id).name(projectName).build();
        BuildConfiguration bc = BuildConfiguration.Builder.newBuilder()
                .id(id)
                .name("bc-" + id)
                .project(project)
                .genericParameters(categoryParameter(category))
                .build();
        when(buildConfigurationRepository.queryById(id)).thenReturn(bc);
        return bc;
    }

    private BuildConfigurationAudited audited(
            BuildConfiguration bc,
            int rev,
            String projectName,
            BuildCategory category) {
        BuildConfigurationAudited audited = new BuildConfigurationAudited();
        audited.setIdRev(new IdRev(bc.getId(), rev));
        audited.setBuildConfiguration(bc);
        audited.setProject(Project.Builder.newBuilder().id(nextId++).name(projectName).build());
        audited.setGenericParameters(categoryParameter(category));
        return audited;
    }

    private BuildConfigurationSet mockGroup(BuildConfiguration... buildConfigurations) {
        BuildConfigurationSet.Builder builder = BuildConfigurationSet.Builder.newBuilder()
                .id(nextId++)
                .name("group");
        for (BuildConfiguration bc : buildConfigurations) {
            builder.buildConfiguration(bc);
        }
        BuildConfigurationSet group = builder.build();
        when(buildConfigurationSetRepository.queryById(group.getId())).thenReturn(group);
        return group;
    }

    private void mockSingleBuildResult(int buildConfigId) throws Exception {
        BuildSetTask buildSetTask = buildSetTaskFor(buildConfigId);
        when(buildCoordinator.buildConfig(any(), any(), any())).thenReturn(buildSetTask);
    }

    private static BuildSetTask buildSetTaskFor(int buildConfigId) {
        BuildTask buildTask = mock(BuildTask.class, RETURNS_DEEP_STUBS);
        when(buildTask.getId()).thenReturn("build-" + buildConfigId);
        when(buildTask.getBuildConfigurationAudited().getBuildConfiguration().getId()).thenReturn(buildConfigId);
        BuildSetTask buildSetTask = mock(BuildSetTask.class);
        when(buildSetTask.getBuildTasks()).thenReturn(Set.of(buildTask));
        return buildSetTask;
    }

    private static Map<String, String> categoryParameter(BuildCategory category) {
        return Map.of(BuildConfigurationParameterKeys.BUILD_CATEGORY.name(), category.name());
    }

    private static BuildOptions temporary() {
        BuildOptions options = new BuildOptions();
        options.setTemporaryBuild(true);
        return options;
    }

    private static BuildOptions persistent() {
        BuildOptions options = new BuildOptions();
        options.setTemporaryBuild(false);
        return options;
    }
}
