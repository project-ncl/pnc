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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.mapper.Base32LongIdMapper;
import org.jboss.pnc.mapper.IDToReferenceMapper;
import org.jboss.pnc.mapper.UserFetcher;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.rex.common.enums.StopFlag;
import org.jboss.pnc.rex.common.enums.Transition;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.dto.TransitionTimeDTO;
import org.jboss.pnc.rex.model.TransitionTime;
import org.jboss.pnc.rex.model.requests.MinimizedTask;
import org.jboss.pnc.spi.coordinator.BuildMeta;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.DefaultBuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mapper(
        config = MapperCentralConfig.class,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        uses = { UserFetcher.class, IDToReferenceMapper.class, Base32LongIdMapper.class },
        imports = { ContentIdentityManager.class, IdRev.class })
public interface BuildTaskMappers {

    @Mapping(target = "id", source = "task.name")
    @Mapping(target = "idRev", source = "task.constraint")
    @Mapping(target = "buildConfigSetRecordId", source = "task.correlationID")
    @Mapping(target = "productMilestone", source = "meta.productMilestoneId")
    @Mapping(target = "submitTime", source = "meta.submitTime")
    @Mapping(target = "startTime", ignore = true) // generated in fillStartAndEndTime
    @Mapping(target = "endTime", ignore = true) // generated in fillStartAndEndTime
    @Mapping(target = "user", source = "meta.username", qualifiedBy = ByUsername.class)
    @Mapping(target = "noRebuildCause", source = "meta.noRebuildCauseId")
    @Mapping(
            target = "status",
            expression = "java(BuildTaskMappers.toBuildStatus(task.getState(), task.getStopFlag()))")
    @Mapping(target = "dependants", source = "meta.dependants")
    @Mapping(target = "dependencies", source = "meta.dependencies")
    @Mapping(target = "taskDependants", source = "task.dependants")
    @Mapping(target = "taskDependencies", source = "task.dependencies")
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "remoteStart", "remoteCancel", "callerNotifications", "state",
                    "stopFlag", "serverResponses", "id", "idRev", "configuration", "timestamps" })
    DefaultBuildTaskRef toBuildTaskRef(TaskDTO task, BuildMeta meta);

    @Mapping(target = "id", source = "task.name")
    @Mapping(target = "idRev", source = "task.constraint")
    @Mapping(target = "buildConfigSetRecordId", source = "task.correlationID")
    @Mapping(target = "productMilestone", source = "meta.productMilestoneId")
    @Mapping(target = "submitTime", source = "meta.submitTime")
    @Mapping(target = "startTime", ignore = true) // generated in fillStartAndEndTime
    @Mapping(target = "endTime", ignore = true) // generated in fillStartAndEndTime
    @Mapping(
            target = "status",
            expression = "java(BuildTaskMappers.toBuildStatus(task.getState(), task.getStopFlag()))")
    @Mapping(target = "user", source = "meta.username", qualifiedBy = ByUsername.class)
    @Mapping(target = "noRebuildCause", source = "meta.noRebuildCauseId")
    @Mapping(target = "dependants", source = "meta.dependants")
    @Mapping(target = "dependencies", source = "meta.dependencies")
    @Mapping(target = "taskDependants", source = "task.dependants")
    @Mapping(target = "taskDependencies", source = "task.dependencies")
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "remoteStart", "remoteCancel", "callerNotifications", "state",
                    "stopFlag", "serverResponses", "id", "idRev", "configuration", "timestamps" })
    DefaultBuildTaskRef toBuildTaskRef(MinimizedTask task, BuildMeta meta);

    @BeforeMapping
    default void fillStartAndEndTime(TaskDTO source, @MappingTarget DefaultBuildTaskRef.Builder builder) {
        if (source.getTimestamps() == null) {
            return;
        }

        Optional<Instant> startTime = getStartTimeDTO(source.getTimestamps());
        Optional<Instant> endTime = getEndTimeDTO(source.getTimestamps());

        startTime.ifPresent(builder::startTime);
        endTime.ifPresent(builder::endTime);
    }

    @BeforeMapping
    default void fillStartAndEndTime(MinimizedTask source, @MappingTarget DefaultBuildTaskRef.Builder builder) {
        if (source.getTimestamps() == null) {
            return;
        }

        Optional<Instant> startTime = getStartTime(source.getTimestamps());
        Optional<Instant> endTime = getEndTime(source.getTimestamps());

        startTime.ifPresent(builder::startTime);
        endTime.ifPresent(builder::endTime);
    }

    static Optional<Instant> getStartTime(List<TransitionTime> transitionTimes) {
        return getTimestamp(
                transitionTimes,
                TransitionTime::getTransition,
                TransitionTime::getTime,
                BuildTaskMappers::identifyStart,
                true);
    }

    static Optional<Instant> getStartTimeDTO(List<TransitionTimeDTO> transitionTimes) {
        return getTimestamp(
                transitionTimes,
                TransitionTimeDTO::getTransition,
                TransitionTimeDTO::getTime,
                BuildTaskMappers::identifyStart,
                true);
    }

    static Optional<Instant> getEndTime(List<TransitionTime> transitionTimes) {
        return getTimestamp(
                transitionTimes,
                TransitionTime::getTransition,
                TransitionTime::getTime,
                BuildTaskMappers::identifyEnd,
                false);
    }

    static Optional<Instant> getEndTimeDTO(List<TransitionTimeDTO> transitionTimes) {
        return getTimestamp(
                transitionTimes,
                TransitionTimeDTO::getTransition,
                TransitionTimeDTO::getTime,
                BuildTaskMappers::identifyEnd,
                false);
    }

    /**
     * Transition FROM ENQUEUED state is a start of building phase
     */
    private static Boolean identifyStart(Transition transition) {
        return transition.getBefore() == State.ENQUEUED;
    }

    /**
     * Transition INTO final state is the final transition with the end time
     */
    private static Boolean identifyEnd(Transition transition) {
        return transition.getAfter().isFinal();
    }

    static <T> Optional<Instant> getTimestamp(
            List<T> collection,
            Function<T, Transition> extractTransition,
            Function<T, Instant> extractTime,
            Predicate<Transition> transitionPredicate,
            boolean earliest) {
        return collection.stream()
                .filter((transitionTime) -> transitionPredicate.test(extractTransition.apply(transitionTime)))
                .map(extractTime)
                // for edge-cases where predicate matches more than one transition (retries?)
                .min(earliest ? Comparator.naturalOrder() : Comparator.reverseOrder());
    }

    @Mapping(target = "buildConfigurationAudited", source = "idRev")
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "buildSetTask", ignore = true)
    @Mapping(target = "buildConfigSetRecordId", source = "buildConfigSetRecordId")
    @Mapping(target = "buildOptions.temporaryBuild", source = "temporaryBuild")
    @Mapping(target = "buildOptions.alignmentPreference", source = "alignmentPreference")
    @Mapping(target = "requestContext", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "taskDependants", "taskDependencies" })
    BuildTask toBuildTask(BuildTaskRef remoteTaskRef);

    @Mapping(target = "id", source = "request.id")
    @Mapping(
            target = "idRev",
            expression = "java( new IdRev( Integer.valueOf(request.getBuildConfigurationAudited().getId()), request.getBuildConfigurationAudited().getRev() ) )")
    @Mapping(target = "temporaryBuild", source = "request.buildOptions.temporaryBuild")
    @Mapping(target = "status", constant = "REJECTED_ALREADY_BUILT")
    @Mapping(target = "alignmentPreference", source = "request.buildOptions.alignmentPreference")
    @Mapping(target = "productMilestone", source = "request.currentProductMilestone.id")
    @Mapping(target = "buildConfigSetRecordId", source = "setRecordId")
    @Mapping(target = "user", source = "request.username", qualifiedBy = ByUsername.class)
    @Mapping(target = "startTime", ignore = true) // NRR don't have startTime
    @Mapping(target = "endTime", ignore = true) // NRR don't have endTime
    @Mapping(target = "contentId", expression = "java(ContentIdentityManager.getBuildContentId(request.getId()))")
    @Mapping(target = "dependants", source = "request.dependants")
    @Mapping(target = "dependencies", source = "request.dependencies")
    @Mapping(target = "taskDependencies", ignore = true)
    @Mapping(target = "taskDependants", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "alreadyRunning", "buildConfigurationAudited" })
    DefaultBuildTaskRef toNRRBuildTaskRef(RemoteBuildTask request, Long setRecordId);

    @Mapping(
            target = "idRev",
            expression = "java( new IdRev( Integer.valueOf(request.getBuildConfigurationAudited().getId()), request.getBuildConfigurationAudited().getRev() ) )")
    @Mapping(target = "contentId", expression = "java(ContentIdentityManager.getBuildContentId(request.getId()))")
    @Mapping(target = "temporaryBuild", source = "buildOptions.temporaryBuild")
    @Mapping(target = "alignmentPreference", source = "buildOptions.alignmentPreference")
    @Mapping(target = "productMilestoneId", source = "currentProductMilestone.id")
    @Mapping(
            target = "noRebuildCauseId",
            expression = "java( request.getNoRebuildCause().map(r -> r.getId().getId()).orElse(null) )")
    @BeanMapping(ignoreUnmappedSourceProperties = { "buildConfigurationAudited", "alreadyRunning", "noRebuildCause" })
    BuildMeta toBuildMeta(RemoteBuildTask request);

    // used for mapping Collection<BuildTask> dependencies and dependants into Collection<String> of ids
    static BuildTask fromStringId(String depId) {
        return new BuildTask(null, null, null, null, null, depId, null, null, null, null);
    }

    static BuildCoordinationStatus toBuildStatus(State state, StopFlag flag) {
        switch (state) {
            case NEW:
                return BuildCoordinationStatus.NEW;
            case WAITING:
                return BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES;
            case ENQUEUED:
                return BuildCoordinationStatus.ENQUEUED;
            case STOP_REQUESTED:
            case STARTING:
            case STOPPING:
            case UP:
                return BuildCoordinationStatus.BUILDING;
            case STOP_FAILED:
            case START_FAILED:
                return BuildCoordinationStatus.SYSTEM_ERROR;
            case FAILED:
                return BuildCoordinationStatus.DONE_WITH_ERRORS;
            case SUCCESSFUL:
                return BuildCoordinationStatus.DONE;
            case STOPPED:
                switch (flag) {
                    case NONE:
                    case UNSUCCESSFUL:
                        return BuildCoordinationStatus.SYSTEM_ERROR;
                    case CANCELLED:
                        return BuildCoordinationStatus.CANCELLED;
                    case DEPENDENCY_FAILED:
                        return BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES;
                    default:
                        throw new IllegalArgumentException("Unknown stopFlag " + flag);
                }
            default:
                throw new IllegalArgumentException("Unknown Rex state " + state);
        }
    }

    static String toConstraint(IdRev idRev) {
        return idRev.getId() + "-" + idRev.getRev();
    }

    static IdRev fromConstraint(String constraint) {
        String[] split = constraint.split("-");
        return new IdRev(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    static <T> T unwrap(Optional<T> optional) {
        return (optional != null && optional.isPresent()) ? optional.get() : null;
    }
}
