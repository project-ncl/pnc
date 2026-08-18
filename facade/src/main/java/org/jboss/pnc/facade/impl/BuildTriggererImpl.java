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
import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.BuildTriggerer;
import org.jboss.pnc.facade.providers.GenericSettingProvider;
import org.jboss.pnc.facade.util.HibernateLazyInitializer;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.remotecoordinator.maintenance.TemporaryBuildsCleaner;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    private BuildCoordinator buildCoordinator;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private TemporaryBuildsCleaner temporaryBuildsCleaner;

    @Inject
    private HibernateLazyInitializer hibernateLazyInitializer;

    @Inject
    private GenericSettingProvider genericSettingProvider;

    @Override
    public String triggerBuild(
            final int buildConfigId,
            OptionalInt buildConfigurationRevision,
            BuildOptions buildOptions) throws BuildConflictException, CoreException, BuildRequestException {

        throwCoreExceptionIfInMaintenanceModeAndNonSystemUser();

        BuildSetTask result = doTriggerBuild(buildConfigId, buildConfigurationRevision, buildOptions);
        return selectBuildRecordIdOf(result.getBuildTasks(), buildConfigId);
    }

    @Override
    public String triggerGroupBuild(int groupConfigId, Optional<GroupBuildRequest> revs, BuildOptions buildOptions)
            throws BuildConflictException, CoreException, BuildRequestException {

        throwCoreExceptionIfInMaintenanceModeAndNonSystemUser();

        BuildSetTask result = doTriggerGroupBuild(groupConfigId, revs, buildOptions);
        return result.getBuildConfigSetRecord().get().getId().getId();
    }

    @Override
    public boolean cancelBuild(String buildId) throws CoreException {
        return buildCoordinator.cancel(buildId);
    }

    private BuildSetTask doTriggerBuild(
            final int buildConfigId,
            OptionalInt buildConfigurationRevision,
            BuildOptions buildOptions) throws BuildConflictException, CoreException, BuildRequestException {

        BuildSetTask buildSetTask;
        if (buildConfigurationRevision.isPresent()) {
            final BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(new IdRev(buildConfigId, buildConfigurationRevision.getAsInt()));
            Preconditions.checkArgument(
                    buildConfigurationAudited != null,
                    "Can't find Build Configuration with id=" + buildConfigId + ", rev="
                            + buildConfigurationRevision.getAsInt());

            deletePreviousLightwellUpstreamTemporaryBuild(
                    buildConfigId,
                    buildConfigurationAudited.getGenericParameters(),
                    buildOptions);

            buildSetTask = buildCoordinator.buildConfigurationAudited(
                    hibernateLazyInitializer
                            .initializeBuildConfigurationAuditedBeforeTriggeringIt(buildConfigurationAudited),
                    user.currentUser(),
                    buildOptions);
        } else {
            final BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(buildConfigId);
            Preconditions.checkArgument(
                    buildConfiguration != null,
                    "Can't find Build Configuration with id=" + buildConfigId);

            deletePreviousLightwellUpstreamTemporaryBuild(
                    buildConfigId,
                    buildConfiguration.getGenericParameters(),
                    buildOptions);

            buildSetTask = buildCoordinator.buildConfig(
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

    /**
     * Before starting a temporary build of a Build Configuration whose build category is
     * {@link BuildCategory#LIGHTWELL_UPSTREAM}, delete the previous successful temporary build (if any) of the same
     * Build Configuration so that only a single successful temporary LIGHTWELL_UPSTREAM build is ever kept.
     *
     * <p>
     * The build category is not a first class field on the build; it is read from the Build Configuration's generic
     * parameter {@link BuildConfigurationParameterKeys#BUILD_CATEGORY}, which is how the rest of PNC determines a
     * build's category.
     *
     * <p>
     * The deletion is performed synchronously and, if it fails, a {@link CoreException} is thrown so the new build is
     * not started.
     *
     * @param buildConfigId the id of the Build Configuration being built
     * @param genericParameters the generic parameters of the Build Configuration (revision) being built
     * @param buildOptions the options of the build being triggered
     */
    private void deletePreviousLightwellUpstreamTemporaryBuild(
            int buildConfigId,
            Map<String, String> genericParameters,
            BuildOptions buildOptions) throws CoreException {

        if (!buildOptions.isTemporaryBuild()) {
            return;
        }

        boolean isLightwellUpstream = genericParameters != null && BuildCategory.LIGHTWELL_UPSTREAM.name()
                .equals(genericParameters.get(BuildConfigurationParameterKeys.BUILD_CATEGORY.name()));
        if (!isLightwellUpstream) {
            return;
        }

        BuildRecord previousBuild = buildRecordRepository.queryWithBuildConfigurationId(buildConfigId)
                .stream()
                .filter(br -> br.isTemporaryBuild() && br.getStatus() == BuildStatus.SUCCESS)
                .filter(this::isLightwellUpstreamBuild)
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (previousBuild == null) {
            logger.info(
                    "No previous successful temporary LIGHTWELL_UPSTREAM build found for Build Configuration {}.",
                    buildConfigId);
            return;
        }

        logger.info(
                "Deleting previous successful temporary LIGHTWELL_UPSTREAM build {} of Build Configuration {} before "
                        + "triggering a new temporary build.",
                previousBuild.getId(),
                buildConfigId);

        try {
            Result result = temporaryBuildsCleaner.deleteTemporaryBuild(previousBuild.getId());
            if (!result.isSuccess()) {
                throw new CoreException(
                        "Failed to delete previous successful temporary LIGHTWELL_UPSTREAM build "
                                + previousBuild.getId() + " of Build Configuration " + buildConfigId + ": "
                                + result.getMessage());
            }
        } catch (ValidationException e) {
            // A concurrent trigger of the same Build Configuration may have already deleted the build between our
            // query and this deletion. If it is already gone, the blocker is cleared and we can safely proceed.
            if (buildRecordRepository.queryById(previousBuild.getId()) == null) {
                logger.info(
                        "Previous temporary LIGHTWELL_UPSTREAM build {} of Build Configuration {} was already deleted "
                                + "(likely by a concurrent trigger); proceeding.",
                        previousBuild.getId(),
                        buildConfigId);
                return;
            }
            throw new CoreException(
                    "Failed to delete previous successful temporary LIGHTWELL_UPSTREAM build " + previousBuild.getId()
                            + " of Build Configuration " + buildConfigId,
                    e);
        }
    }

    /**
     * Determines whether the given build record's own build category is {@link BuildCategory#LIGHTWELL_UPSTREAM}. The
     * category is read from the {@link BuildConfigurationParameterKeys#BUILD_CATEGORY} generic parameter of the exact
     * Build Configuration revision the build was produced from, so builds from an older revision that had a different
     * category are not matched.
     *
     * @param buildRecord the build record to inspect
     * @return true if the build was produced as a LIGHTWELL_UPSTREAM build
     */
    private boolean isLightwellUpstreamBuild(BuildRecord buildRecord) {
        BuildConfigurationAudited buildConfigurationAudited = buildRecord.getBuildConfigurationAudited();
        if (buildConfigurationAudited == null) {
            buildConfigurationAudited = buildConfigurationAuditedRepository
                    .queryById(buildRecord.getBuildConfigurationAuditedIdRev());
        }
        if (buildConfigurationAudited == null) {
            return false;
        }

        Map<String, String> genericParameters = buildConfigurationAudited.getGenericParameters();
        return genericParameters != null && BuildCategory.LIGHTWELL_UPSTREAM.name()
                .equals(genericParameters.get(BuildConfigurationParameterKeys.BUILD_CATEGORY.name()));
    }

    private BuildSetTask doTriggerGroupBuild(
            final int groupConfigId,
            Optional<GroupBuildRequest> revs,
            BuildOptions buildOptions) throws CoreException, BuildRequestException, BuildConflictException {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(groupConfigId);
        Preconditions.checkArgument(
                buildConfigurationSet != null,
                "Can't find configuration with given id=" + groupConfigId);

        List<BuildConfigurationRevisionRef> revisions = revs.map(GroupBuildRequest::getBuildConfigurationRevisions)
                .orElse(Collections.emptyList());

        BuildSetTask buildSetTask = buildCoordinator.buildSet(
                hibernateLazyInitializer.initializeBuildConfigurationSetBeforeTriggeringIt(buildConfigurationSet),
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
                .map(BuildTask::getId)
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
