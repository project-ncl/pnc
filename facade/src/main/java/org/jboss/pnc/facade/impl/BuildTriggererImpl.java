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
package org.jboss.pnc.facade.impl;

import com.google.common.base.Preconditions;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.facade.BuildCoordinatorProvider;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.facade.util.HibernateLazyInitializer;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

/**
 *
 * @author jbrazdil
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Stateless
public class BuildTriggererImpl implements BuildTriggerer {

    private static final Logger logger = LoggerFactory.getLogger(BuildTriggererImpl.class);

    @Inject
    private UserService user;

    @Inject
    private BuildStatusNotifications buildStatusNotifications;

    @Inject
    private BuildCoordinatorProvider buildCoordinatorProvider;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private HibernateLazyInitializer hibernateLazyInitializer;

    @Inject
    GenericSettingProvider genericSettingProvider;

    @Override
    public String triggerBuild(
            final int buildConfigId,
            OptionalInt buildConfigurationRevision,
            BuildOptions buildOptions) throws BuildConflictException, CoreException {

        throwCoreExceptionIfInMaintenanceModeAndNonSystemUser();

        BuildSetTask result = doTriggerBuild(buildConfigId, buildConfigurationRevision, buildOptions);
        return selectBuildRecordIdOf(result.getBuildTasks(), buildConfigId);
    }

    @Override
    public int triggerGroupBuild(int groupConfigId, Optional<GroupBuildRequest> revs, BuildOptions buildOptions)
            throws BuildConflictException, CoreException {

        throwCoreExceptionIfInMaintenanceModeAndNonSystemUser();

        BuildSetTask result = doTriggerGroupBuild(groupConfigId, revs, buildOptions);
        return result.getBuildConfigSetRecord().get().getId();
    }

    @Override
    public boolean cancelBuild(String buildId) throws CoreException {
        return buildCoordinatorProvider.getCoordinator().cancel(buildId);
    }

    @Override
    public Optional<BuildTaskContext> getMdcMeta(String buildId) {
        return buildCoordinatorProvider.getCoordinator().getMDCMeta(buildId);
    }

    private BuildSetTask doTriggerBuild(
            final int buildConfigId,
            OptionalInt buildConfigurationRevision,
            BuildOptions buildOptions) throws BuildConflictException, CoreException {

        BuildSetTask buildSetTask;
        if (buildConfigurationRevision.isPresent()) {
            final BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(new IdRev(buildConfigId, buildConfigurationRevision.getAsInt()));
            Preconditions.checkArgument(
                    buildConfigurationAudited != null,
                    "Can't find Build Configuration with id=" + buildConfigId + ", rev="
                            + buildConfigurationRevision.getAsInt());

            buildSetTask = buildCoordinatorProvider.getCoordinator()
                    .buildConfigurationAudited(
                            hibernateLazyInitializer
                                    .initializeBuildConfigurationAuditedBeforeTriggeringIt(buildConfigurationAudited),
                            user.currentUser(),
                            buildOptions);
        } else {
            final BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(buildConfigId);
            Preconditions.checkArgument(
                    buildConfiguration != null,
                    "Can't find Build Configuration with id=" + buildConfigId);

            buildSetTask = buildCoordinatorProvider.getCoordinator()
                    .buildConfig(
                            hibernateLazyInitializer.initializeBuildConfigurationBeforeTriggeringIt(buildConfiguration),
                            user.currentUser(),
                            buildOptions);
        }

        logger.info(
                "Started build of Build Configuration {}. Build Tasks: {}",
                buildConfigId,
                buildSetTask.getBuildTasks().stream().map(BuildTask::getId).collect(Collectors.joining()));
        return buildSetTask;
    }

    private BuildSetTask doTriggerGroupBuild(
            final int groupConfigId,
            Optional<GroupBuildRequest> revs,
            BuildOptions buildOptions) throws CoreException {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(groupConfigId);
        Preconditions.checkArgument(
                buildConfigurationSet != null,
                "Can't find configuration with given id=" + groupConfigId);

        List<BuildConfigurationRevisionRef> revisions = revs.map(GroupBuildRequest::getBuildConfigurationRevisions)
                .orElse(Collections.emptyList());

        BuildSetTask buildSetTask = buildCoordinatorProvider.getCoordinator()
                .buildSet(
                        hibernateLazyInitializer
                                .initializeBuildConfigurationSetBeforeTriggeringIt(buildConfigurationSet),
                        loadAuditedsFromDB(buildConfigurationSet, revisions),
                        user.currentUser(),
                        buildOptions);

        logger.info(
                "Started build of Group Configuration {}. Build Tasks: {}",
                groupConfigId,
                buildSetTask.getBuildTasks().stream().map(BuildTask::getId).collect(Collectors.joining()));
        return buildSetTask;
    }

    private Map<Integer, BuildConfigurationAudited> loadAuditedsFromDB(
            BuildConfigurationSet buildConfigurationSet,
            List<BuildConfigurationRevisionRef> buildConfigurationAuditedRests) throws InvalidEntityException {
        Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap = new HashMap<>();

        Set<IdRev> buildConfigurationAuditedRevs = nullableStreamOf(buildConfigurationAuditedRests)
                .map(bcrRef -> new IdRev(Integer.valueOf(bcrRef.getId()), bcrRef.getRev()))
                .collect(Collectors.toSet());
        if (!buildConfigurationAuditedRevs.isEmpty()) {
            Map<IdRev, BuildConfigurationAudited> buildConfigurationsAuditedMap = buildConfigurationAuditedRepository
                    .queryById(buildConfigurationAuditedRevs);

            for (BuildConfigurationRevisionRef bc : buildConfigurationAuditedRests) {
                BuildConfigurationAudited buildConfigurationAudited = buildConfigurationsAuditedMap
                        .get(new IdRev(Integer.valueOf(bc.getId()), bc.getRev()));
                Preconditions.checkArgument(
                        buildConfigurationAudited != null,
                        "Can't find Build Configuration with id=" + bc.getId() + ", rev=" + bc.getRev());
                buildConfigurationAudited = hibernateLazyInitializer
                        .initializeBuildConfigurationAuditedBeforeTriggeringIt(buildConfigurationAudited);

                if (!buildConfigurationSet.getBuildConfigurations()
                        .contains(buildConfigurationAudited.getBuildConfiguration())) {
                    throw new InvalidEntityException(
                            "BuildConfigurationSet " + buildConfigurationSet
                                    + " doesn't contain this BuildConfigurationAudited entity "
                                    + buildConfigurationAudited);
                }

                buildConfigurationAuditedsMap.put(buildConfigurationAudited.getId(), buildConfigurationAudited);
            }
        }

        return buildConfigurationAuditedsMap;
    }

    private String selectBuildRecordIdOf(Collection<BuildTask> buildTasks, int buildConfigId) throws CoreException {
        return buildTasks.stream()
                .filter(t -> t.getBuildConfigurationAudited().getBuildConfiguration().getId().equals(buildConfigId))
                .map(buildTask -> buildTask.getId())
                .findAny()
                .orElseThrow(() -> new CoreException("No build id for the triggered configuration"));
    }

    private void throwCoreExceptionIfInMaintenanceModeAndNonSystemUser() throws BuildConflictException {

        if (!genericSettingProvider.isCurrentUserAllowedToTriggerBuilds()) {
            String reason = genericSettingProvider.getAnnouncementBanner();

            if (reason == null) {
                reason = "";
            }

            throw new BuildConflictException("PNC is in maintenance mode: " + reason);
        }
    }

}
