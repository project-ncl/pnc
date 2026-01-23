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
package org.jboss.pnc.rest.endpoints.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.api.dto.ErrorResponse;
import org.jboss.pnc.api.orch.dto.BuildImport;
import org.jboss.pnc.api.orch.dto.BuildResultRest;
import org.jboss.pnc.api.orch.dto.ImportBuildsRequest;
import org.jboss.pnc.api.orch.dto.RepositoryManagerResultRest;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildMetaMapper;
import org.jboss.pnc.mapper.api.BuildResultMapper;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildTaskMappers;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordAttribute;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.remotecoordinator.builder.SetRecordTasks;
import org.jboss.pnc.rest.endpoints.internal.api.BuildTaskEndpoint;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.common.enums.StopFlag;
import org.jboss.pnc.rex.model.ServerResponse;
import org.jboss.pnc.rex.model.requests.MinimizedTask;
import org.jboss.pnc.rex.model.requests.NotificationRequest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildMeta;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.KV;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.text.MessageFormat.format;
import static org.jboss.pnc.mapper.api.BuildTaskMappers.toBuildStatus;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.*;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.isAttachmentTo;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdRev;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withEndTime;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withIds;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withOneOfCombinationOfAttributes;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withStartTime;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withSubmitTime;

@Dependent
public class BuildTaskEndpointImpl implements BuildTaskEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BuildTaskEndpointImpl.class);

    private final ObjectMapper jsonMapper = new JacksonProvider().getMapper();

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private BuildResultMapper mapper;

    @Inject
    private BuildTaskMappers taskMapper;

    @Inject
    private BuildMetaMapper metaMapper;

    @Inject
    private BuildMapper buildMapper;

    @Inject
    private ArtifactMapper artifactMapper;

    @Inject
    private SystemConfig systemConfig;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private BuildConfigurationAuditedRepository bcaRepository;

    @Inject
    private GroupBuildProvider groupBuildProvider;

    @Inject
    private SetRecordTasks setRecordTasks;

    @Override
    public Response buildTaskCompleted(String buildId, BuildResultRest buildResult) throws InvalidEntityException {

        // TODO set MDC from request headers instead of business data
        // logger.debug("Received task completed notification for coordinating task id [{}].", buildId);
        // BuildExecutionConfigurationRest buildExecutionConfiguration = buildResult.getBuildExecutionConfiguration();
        // buildResult.getRepositoryManagerResult().getBuildContentId();
        // if (buildExecutionConfiguration == null) {
        // logger.error("Missing buildExecutionConfiguration in buildResult for buildTaskId [{}].", buildId);
        // throw new CoreException("Missing buildExecutionConfiguration in buildResult for buildTaskId " + buildId);
        // }
        // MDCUtils.addContext(buildExecutionConfiguration.getBuildContentId(),
        // buildExecutionConfiguration.isTempBuild(), systemConfig.getTemporaryBuildExpireDate());
        logger.info("Received build task completed notification for id {}.", buildId);

        ValidationBuilder.validateObject(buildResult, WhenCreatingNew.class).validateAnnotations();

        // check if task is already completed
        // required workaround as we don't remove the BpmTasks immediately after the completion
        Optional<BuildTask> maybeBuildTask;
        try {
            maybeBuildTask = buildCoordinator.getSubmittedBuildTask(buildId);
        } catch (RemoteRequestException | MissingDataException e) {
            // these are never thrown by In-memory BuildCoordinator
            throw new RuntimeException(e);
        }

        if (maybeBuildTask.isPresent()) {
            BuildTask buildTask = maybeBuildTask.get();
            boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
            // user MDC must be set by the request filter
            MDCUtils.addBuildContext(
                    buildTask.getContentId(),
                    temporaryBuild,
                    ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild),
                    null);
            try {
                if (buildTask.getStatus().isCompleted()) {
                    logger.warn(
                            "BuildTask with id: {} is already completed with status: {}",
                            buildTask.getId(),
                            buildTask.getStatus());
                    return Response.status(Response.Status.GONE)
                            .entity(
                                    "BuildTask with id: " + buildTask.getId() + " is already completed with status: "
                                            + buildTask.getStatus() + ".")
                            .build();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Received build result with full log: {}.",
                            JsonOutputConverterMapper.apply(buildResult));
                }
                logger.debug("Completing buildTask [{}] ...", buildId);

                buildCoordinator.completeBuild(buildTask, mapper.toEntity(buildResult));

                logger.debug("Completed buildTask [{}].", buildId);
                return Response.ok().build();
            } finally {
                MDCUtils.removeBuildContext();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No active build with id: " + buildId).build();
        }
    }

    @Override
    @Transactional
    // TODO create a role for importer
    // @RolesAllowed({ USERS_ADMIN })
    public List<Build> buildImportEndpoint(ImportBuildsRequest request, Set<String> uniqueAttributes) {
        logger.debug("Received request to import multiple builds. This is a import by an external build system.");
        ValidationBuilder.validateObject(request, WhenImporting.class).validateAnnotations();
        validateTargetRepositories(request);

        validateAttributesArePresent(request, uniqueAttributes);

        var matchedRecords = handleIdempotency(request, uniqueAttributes);

        List<BuildRecord> oldImportedRecords = new ArrayList<>();
        List<BuildImport> newImports = new ArrayList<>();
        for (var entry : matchedRecords.entrySet()) {
            BuildImport buildImport = entry.getKey();
            BuildRecord buildRecord = entry.getValue();

            if (buildRecord == null) {
                newImports.add(buildImport);
                continue;
            }

            oldImportedRecords.add(buildRecord);
            logger.info(
                    "There's an attempt to re-import BuildRecord '{}' from Config '{}' (id: {}; rev: {})",
                    buildRecord.getId(),
                    buildRecord.getBuildConfigurationAudited().getName(),
                    buildRecord.getBuildConfigurationAudited().getId(),
                    buildRecord.getBuildConfigurationAudited().getRev());
        }

        // save new imports into DB
        Set<Base32LongID> newIds = new HashSet<>();
        for (BuildImport anImport : newImports) {
            var metaDto = anImport.getMetadata();
            var resultDto = anImport.getResult();

            // fill out the build ID
            String importBuildId = Sequence.nextBase32Id();
            newIds.add(new Base32LongID(importBuildId));
            metaDto = metaDto.toBuilder().id(importBuildId).build();

            var meta = metaMapper.toEntity(metaDto);
            var result = mapper.toEntity(resultDto);
            var taskRef = taskMapper.toBuildTaskRef(
                    meta,
                    anImport.getStartTime(),
                    anImport.getEndTime(),
                    fromCompletionStatus(result.getCompletionStatus()));

            buildCoordinator.completeBuild(taskRef, Optional.of(result), BuildCoordinationStatus.NEW);
        }

        List<BuildRecord> records = new ArrayList<>();

        // return newly created records
        if (!newIds.isEmpty()) {
            records = buildRecordRepository.queryWithPredicates(withIds(newIds));
            validateImportResult(newIds, records);
        }

        // return also the records which were found to be an equivalent
        records.addAll(oldImportedRecords);

        return records.stream().map(buildMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Unfortunately, cascading validation for TargetRepository DTO would cause issues with WhenCreatingNew group.
     * Therefore, we're doing validation for TR manually.
     */
    private void validateTargetRepositories(ImportBuildsRequest request) {
        for (int iidx = 0; iidx < request.getImports().size(); iidx++) {
            var anImport = request.getImports().get(iidx);
            var repoResult = anImport.getResult().getRepositoryManagerResult();
            String importPath = "imports[" + iidx + "].result";
            if (repoResult != null) {
                validateTargetRepositories(
                        importPath + ".repositoryManagerResult.builtArtifacts",
                        repoResult.getBuiltArtifacts());
                validateTargetRepositories(
                        importPath + ".repositoryManagerResult.dependencies",
                        repoResult.getDependencies());
            }
            validateTargetRepositories(importPath + ".attachments", anImport.getResult().getAttachments());

        }
    }

    private void validateTargetRepositories(String artifactPath, List<Artifact> artifacts) {
        if (artifacts != null && !artifacts.isEmpty()) {
            for (int aidx = 0; aidx < artifacts.size(); aidx++) {
                Artifact artifact = artifacts.get(aidx);
                try {
                    ValidationBuilder.validateObject(artifact.getTargetRepository(), WhenImporting.class)
                            .validateAnnotations();
                } catch (InvalidEntityException e) {
                    throw new InvalidEntityException(
                            e.getMessage(),
                            artifactPath + "[" + aidx + "].targetRepository" + e.getField());
                }
            }
        }
    }

    /**
     * 1. Try to find a matching record in Database. 2. If found, ensure that it is the SAME, otherwise return 400 3.
     * Imports without a pair have their unique attributes searched in DB to
     */
    private Map<BuildImport, BuildRecord> handleIdempotency(
            ImportBuildsRequest importRequest,
            Set<String> uniqueAttributes) {

        Map<BuildImport, BuildRecord> matchedPairs = new HashMap<>();
        List<BuildImport> unmatched = new ArrayList<>();
        for (BuildImport anImport : importRequest.getImports()) {
            var idRev = new IdRev(
                    anImport.getMetadata().getIdRev().getId(),
                    anImport.getMetadata().getIdRev().getRev());
            var submitTime = anImport.getMetadata().getSubmitTime();
            var startTime = anImport.getStartTime();
            var endTime = anImport.getEndTime();
            BuildRecord match = buildRecordRepository.queryByPredicates(
                    withBuildConfigurationIdRev(idRev),
                    withSubmitTime(submitTime),
                    withStartTime(startTime),
                    withEndTime(endTime));

            if (match != null) {
                match.setBuildConfigurationAudited(bcaRepository.queryById(match.getBuildConfigurationAuditedIdRev()));
                matchedPairs.put(anImport, match);
            } else {
                unmatched.add(anImport);
            }
        }

        // if match in Database found, has to be equal with import
        for (var entry : matchedPairs.entrySet()) {
            var imp = entry.getKey();
            var record = entry.getValue();

            logger.info("Validating '{}' is equal to its matched Import.", record.getId());
            validateMatchEquals(imp, record);
        }

        // if not found in Database, validate that an existing record with same unique attributes doesn't exist
        if (uniqueAttributes != null && !uniqueAttributes.isEmpty()) {
            validateAttributesWithUnmatched(unmatched, uniqueAttributes);
        }

        unmatched.forEach(imp -> matchedPairs.put(imp, null));

        return matchedPairs;
    }

    private void validateAttributesWithUnmatched(List<BuildImport> unmatched, Set<String> uniqueAttributes) {
        if (unmatched.isEmpty()) {
            return;
        }

        List<BuildRecord> records = queryRecordsWithSameUniqueAttributes(unmatched, uniqueAttributes);
        if (!records.isEmpty()) {
            logger.error(
                    "Attempt to import a different Build when there is a Build with the same unique attribute. \n Issues: {}",
                    records.stream()
                            .map(br -> makeDetailedSummary(br, uniqueAttributes))
                            .reduce("", (s1, s2) -> s1 + "\n" + s2));
            throw new BadRequestException(
                    "Unique constraint validation failed. Found records with same unique attributes.");
        }
    }

    private String makeDetailedSummary(BuildRecord record, Set<String> uniqueAttributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("\tBuild: ").append(record.getId().getId());
        sb.append(" Unique Attributes: ")
                .append(
                        record.getAttributes()
                                .stream()
                                .filter(attribute -> uniqueAttributes.contains(attribute.getKey()))
                                .map(att -> "K: " + att.getKey() + ", V: " + att.getValue())
                                .reduce((sb1, sb2) -> sb1 + "; " + sb2)
                                .get());

        return sb.toString();
    }

    private void validateMatchEquals(BuildImport anImport, BuildRecord record) {
        Date startTime = anImport.getStartTime();
        Date endTime = anImport.getEndTime();
        compareFields(record.getStartTime(), startTime, "startTime", record);
        compareFields(record.getEndTime(), endTime, "endTime", record);

        validateMetaEquals(anImport.getMetadata(), record);

        validateResult(anImport.getResult(), record);
    }

    private void validateResult(BuildResultRest brr, BuildRecord record) {

        if (brr.getCompletionStatus().isFailed()
                && (!record.getStatus().isFinal() || record.getStatus().completedSuccessfully())) {
            logger.warn(
                    "Import is marked as FAILED but the matched BuildRecord '{}' is '{}'.",
                    record.getId(),
                    record.getStatus());
            throw new BadRequestException("Import is marked as FAILED but the matched record '{}' is '{}'.");
        }

        validateRepositoryManagerResult(brr.getRepositoryManagerResult(), record);

        validateContainsAttachments(brr.getAttachments(), record);

        validateContainsAttributes(record.getAttributes(), brr.getExtraAttributes(), "extraAttributes", record);
    }

    private void validateRepositoryManagerResult(
            RepositoryManagerResultRest repositoryManagerResult,
            BuildRecord record) {
        var built = queryArtifactsMapAndSort(withBuildRecordId(record.getId()));
        var deps = queryArtifactsMapAndSort(withDependantBuildRecordId(record.getId()));

        if (repositoryManagerResult == null && !built.isEmpty()) {
            logAndThrowBadRequest("builtArtifacts", record);
        }

        if (repositoryManagerResult == null && !deps.isEmpty()) {
            logAndThrowBadRequest("dependencyArtifacts", record);
        }

        if (repositoryManagerResult != null) {
            var resultDeps = sanitizeAndSort(repositoryManagerResult.getDependencies());
            if (!resultDeps.equals(deps)) {
                logAndThrowBadRequest("dependencyArtifacts", record);
            }

            var resultBuilt = sanitizeAndSort(repositoryManagerResult.getBuiltArtifacts());
            if (!resultBuilt.equals(built)) {
                logAndThrowBadRequest("builtArtifacts", record);
            }
        }

    }

    /**
     * The Record's attachments can be added, therefore we just check that attachment are present, not full equivalence.
     */
    private void validateContainsAttachments(List<Artifact> resultAttachments, BuildRecord record) {
        var attachments = new TreeSet<>(Comparator.comparing(Artifact::getIdentifier));
        attachments.addAll(queryArtifactsMapAndSort(isAttachmentTo(record.getId())));

        if (resultAttachments != null) {
            resultAttachments = sanitizeAndSort(resultAttachments);
            if (!attachments.containsAll(resultAttachments)) {
                logAndThrowBadRequest("attachments", record);
            }
        }
    }

    private static List<Artifact> sanitizeAndSort(List<Artifact> artifacts) {
        if (artifacts == null) {
            return Collections.emptyList();
        }

        return artifacts.stream()
                // map to null because it may be generated by DB and cause comparison errors
                .map(art -> art.toBuilder().buildCategory(null).build())
                .sorted(Comparator.comparing(Artifact::getIdentifier))
                .collect(Collectors.toList());
    }

    private List<Artifact> queryArtifactsMapAndSort(Predicate<org.jboss.pnc.model.Artifact> record) {
        return artifactRepository.queryWithPredicates(record)
                .stream()
                .map(art -> artifactMapper.asImportedDTO(art))
                .sorted(Comparator.comparing(Artifact::getIdentifier))
                .collect(Collectors.toList());
    }

    private void validateMetaEquals(org.jboss.pnc.api.orch.dto.BuildMeta meta, BuildRecord record) {
        comparePrimitiveFields(record.isTemporaryBuild(), meta.isTemporaryBuild(), "temporaryBuild", record);
        compareFields(record.getSubmitTime(), meta.getSubmitTime(), "submitTime", record);
        compareFields(record.getBuildContentId(), meta.getContentId(), "buildContentId", record);
        compareFields(record.getUser().getUsername(), meta.getUsername(), "username", record);
        compareEntityId(record.getProductMilestone(), meta.getProductMilestoneId(), "productMilestoneId", record);
        comparePrimitiveFields(
                record.getAlignmentPreference(),
                meta.getAlignmentPreference(),
                "alignmentPreference",
                record);
        compareFields(
                record.getBuildConfigurationAuditedIdRev(),
                meta.getIdRev() == null ? null : new IdRev(meta.getIdRev().getId(), meta.getIdRev().getRev()),
                "BCA ID-REV",
                record);
        compareEntityId(
                record.getNoRebuildCause(),
                meta.getNoRebuildCauseId() == null ? null : new Base32LongID(meta.getNoRebuildCauseId()),
                "noRebuildCauseId",
                record);
    }

    /**
     * The Record can contain generated attributes, therefore we check just attributes are present not full equivalence.
     */
    private void validateContainsAttributes(
            Set<BuildRecordAttribute> recordAttributes,
            Map<String, String> importAttributes,
            String fieldName,
            BuildRecord record) {
        if (importAttributes != null && !importAttributes.isEmpty()) {

            if (recordAttributes == null || recordAttributes.isEmpty()) {
                logAndThrowBadRequest(fieldName, record);
                return;
            }

            Map<String, String> map = recordAttributes.stream()
                    .collect(Collectors.toMap(BuildRecordAttribute::getKey, BuildRecordAttribute::getValue));
            importAttributes.forEach((key, value) -> {
                if (!(map.containsKey(key) && value.equals(map.get(key)))) {
                    logAndThrowBadRequest(fieldName, record);
                }
            });
        }
    }

    private void compareFields(Object field, Object field2, String fieldName, BuildRecord record) {
        if (!Objects.equals(field2, field)) {
            logAndThrowBadRequest(fieldName, record);
        }
    }

    private void comparePrimitiveFields(Object field, Object field2, String fieldName, BuildRecord record) {
        if (!(field == field2)) {
            logAndThrowBadRequest(fieldName, record);
        }
    }

    private <ID extends Serializable> void compareEntityId(
            GenericEntity<ID> entity,
            ID comparedId,
            String fieldName,
            BuildRecord record) {
        if (entity == null && comparedId != null) {
            logAndThrowBadRequest(fieldName, record);
        }

        if (entity != null && !entity.getId().equals(comparedId)) {
            logAndThrowBadRequest(fieldName, record);
        }
    }

    private static void logAndThrowBadRequest(String fieldName, BuildRecord record) {
        logger.warn(
                "Field '{}' is not the same for an matched Import and BR: {}. Returning 404.",
                fieldName,
                record.getId());
        throw new BadRequestException(
                "Matched Import and BuildRecord " + record.getId() + " differs in field '" + fieldName + "'");
    }

    private List<BuildRecord> queryRecordsWithSameUniqueAttributes(
            List<BuildImport> imports,
            Set<String> uniqueAttributes) {
        Set<Set<KV<String, String>>> searchFor = new HashSet<>();
        for (var uniqueAttribute : uniqueAttributes) {
            imports.stream()
                    .map(BuildImport::getResult)
                    .map(BuildResultRest::getExtraAttributes)
                    .forEach(
                            requestAttributes -> searchFor
                                    .add(Set.of(new KV<>(uniqueAttribute, requestAttributes.get(uniqueAttribute)))));
        }
        if (searchFor.isEmpty()) {
            return Collections.emptyList();
        }
        return buildRecordRepository.queryWithPredicates(withOneOfCombinationOfAttributes(searchFor));
    }

    private void validateImportResult(Set<Base32LongID> newIds, List<BuildRecord> records) {
        if (newIds.size() != records.size()) {
            throw new InternalServerErrorException("Some records were not imported.");
        }
    }

    BuildCoordinationStatus fromCompletionStatus(CompletionStatus completionStatus) {
        switch (completionStatus) {
            case SUCCESS:
                return BuildCoordinationStatus.DONE;
            case NO_REBUILD_REQUIRED:
                return BuildCoordinationStatus.REJECTED_ALREADY_BUILT;
            case FAILED:
                return BuildCoordinationStatus.DONE_WITH_ERRORS;
            case CANCELLED:
                return BuildCoordinationStatus.CANCELLED;
            case TIMED_OUT:
            case SYSTEM_ERROR:
                return BuildCoordinationStatus.SYSTEM_ERROR;
            default:
                throw new IllegalStateException("Unexpected completion status: " + completionStatus);
        }
    }

    private void validateAttributesArePresent(ImportBuildsRequest request, Set<String> uniqueAttributes) {
        if (uniqueAttributes == null || uniqueAttributes.isEmpty()) {
            return;
        }

        for (var buildImport : request.getImports()) {
            var attributes = buildImport.getResult().getExtraAttributes();

            if (!attributes.keySet().containsAll(uniqueAttributes)) {
                throw new BadRequestException("Missing attribute in build result.");
            }
        }
    }

    @Override
    @Transactional
    public Response buildTaskNotification(@NotBlank String buildId, @NotNull NotificationRequest notification)
            throws InvalidEntityException {
        logger.debug("Received transition notification for Build '{}'", buildId);

        // BuildID of endpoint must match the rex task ID
        if (!buildId.equals(notification.getTask().getName())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Build ID does not correspond to associated Rex Task")
                    .build();
        }

        BuildMeta buildMeta = parseBuildMeta(buildId, notification);

        // Task has to be present in Rex, otherwise it is unknown
        validateBuildIsActive(buildId, notification);

        // Avoid notifications if build is already persisted
        validateBuildHasNotBeenSaved(buildId, notification);

        State newState = notification.getAfter();
        State previousState = notification.getBefore();
        StopFlag stopFlag = notification.getTask().getStopFlag();
        logger.debug("Handling transition for build '{}' from {} to {}", buildId, previousState, newState);

        if (newState.isFinal()) {
            // handle edge case when GroupBuild didn't manage to be saved yet but Build finished faster
            validateSetRecordIsPresent(buildId, notification);

            handleFinalTransition(notification.getTask(), buildMeta, previousState);
        } else if (!shouldSkip(previousState, newState, stopFlag)) {
            handleRegularTransition(notification.getTask(), buildMeta, newState, previousState, stopFlag);
        }

        logger.debug("Completed notification for build '{}'.", buildId);
        return Response.ok().build();
    }

    private void validateSetRecordIsPresent(String buildId, NotificationRequest notification)
            throws WebApplicationException {
        String groupBuildId = notification.getTask().getCorrelationID();
        if (groupBuildId != null && groupBuildProvider.getSpecific(groupBuildId) == null) {
            throw new WebApplicationException(
                    Response.status(parseInt(TOO_EARLY_CODE))
                            .entity(format("GroupBuild is not yet persisted."))
                            .build());
        }
    }

    private void validateBuildHasNotBeenSaved(String buildId, NotificationRequest request)
            throws WebApplicationException {
        // fetch directly from DB
        BuildRecord build = buildRecordRepository.queryById(new Base32LongID(buildId));
        if (build != null && build.getStatus().isFinal()) {
            logger.error("Notification arrived while Build '{}' is already saved. Request: {}", buildId, request);
            throw new WebApplicationException(
                    Response.status(Response.Status.GONE)
                            .entity(format("Build {0} has already been persisted.", buildId))
                            .build());
        }
    }

    private void validateBuildIsActive(String buildId, NotificationRequest request)
            throws InternalServerErrorException, NotFoundException {
        Optional<BuildTask> task;
        try {
            task = buildCoordinator.getSubmittedBuildTask(buildId);
        } catch (RemoteRequestException | MissingDataException e) {
            logger.error("Failed to retrieve task '" + buildId + "' from Rex. Request: " + request.toString(), e);
            throw new InternalServerErrorException("Failed to retrieve task '" + buildId + "' from Rex.", e);
        }

        if (task.isEmpty()) {
            logger.warn("Missing or inactive Build with id '{}' received a notification.", buildId);
            throw new NotFoundException(
                    Response.status(Response.Status.NOT_FOUND).entity("No active build with id: " + buildId).build());
        }
    }

    private BuildMeta parseBuildMeta(String buildId, NotificationRequest notification) throws BadRequestException {
        try {
            return jsonMapper.convertValue(notification.getAttachment(), BuildMeta.class);
        } catch (IllegalArgumentException e) {
            logger.error("Notification for Build {} is missing metadata. Request: {}", buildId, notification);
            throw new BadRequestException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("The attachment doesn't contain Build metadata")
                            .build());
        }
    }

    private boolean shouldSkip(State previousState, State newState, StopFlag stopFlag) {
        // For ongoing cancellations, we don't have BuildStatuses
        EnumSet<State> ignoredStates = EnumSet.of(State.STOP_REQUESTED, State.STOPPING);
        if (ignoredStates.contains(newState)) {
            logger.debug("Skipping transition for state {}.", newState);
            return true;
        }

        BuildCoordinationStatus before = toBuildStatus(previousState, null);
        BuildCoordinationStatus after = toBuildStatus(newState, stopFlag);

        // no change between internal statuses
        if (before == after) {
            logger.debug(
                    "Skipping transition from {} to {}, no change in current BuildStatus {}.",
                    previousState,
                    newState,
                    after);
            return true;
        }

        return false;
    }

    private void handleRegularTransition(
            MinimizedTask rexTask,
            BuildMeta buildMeta,
            State newState,
            State previousState,
            StopFlag stopFlag) {
        buildCoordinator.updateBuildTaskStatus(
                taskMapper.toBuildTaskRef(rexTask, buildMeta),
                toBuildStatus(previousState, stopFlag),
                toBuildStatus(newState, stopFlag));
    }

    private void handleFinalTransition(MinimizedTask rexTask, BuildMeta meta, State previousState) {
        Optional<BuildResult> buildResultRest = getBuildResult(rexTask, previousState);

        // store BuildRecord
        buildCoordinator.completeBuild(
                taskMapper.toBuildTaskRef(rexTask, meta),
                buildResultRest,
                toBuildStatus(previousState, rexTask.getStopFlag()));

        // poke BCSR update job
        if (rexTask.getCorrelationID() != null) {
            try {
                setRecordTasks.updateConfigSetRecordsStatuses();
            } catch (CoreException e) {
                throw new InternalServerErrorException(
                        format("Error while trying to update record jobs for finished task {0}", rexTask.getName()),
                        e);
            }
        }
    }

    private Optional<BuildResult> getBuildResult(MinimizedTask rexTask, State previousState)
            throws InvalidEntityException {
        var responseObject = rexTask.getServerResponses()
                .stream()
                .filter(sr -> sr.getState() == previousState) // get bpm response out of Rex's dto
                .findFirst();

        var bpmResponse = responseObject.map(ServerResponse::getBody);

        if (bpmResponse.isEmpty()) {
            // BPM returned null message
            return handleEmptyResponse(rexTask);
        }

        return handleResponse(rexTask, responseObject.get(), bpmResponse);
    }

    private Optional<BuildResult> handleEmptyResponse(MinimizedTask rexTask) {
        switch (rexTask.getStopFlag()) {
            case NONE:
            case UNSUCCESSFUL: {
                // SYSTEM_ERRORS
                if (rexTask.getState() == State.START_FAILED) {
                    return Optional.of(
                            createEmptyExceptionalResult(
                                    new ProcessException(
                                            "Failed to start BPM process for build " + rexTask.getName())));
                } else if (rexTask.getState() == State.FAILED) {
                    return Optional.of(
                            createEmptyExceptionalResult(
                                    new ProcessException("BPM response is missing for build " + rexTask.getName())));
                }

                return Optional.empty();
            }
            // dependency was cancelled, failed or the build had timeout on cancel
            case CANCELLED:
            case DEPENDENCY_FAILED:
            case DEPENDENCY_NOTIFY_FAILED:
                return Optional.empty();
        }

        return Optional.empty();
    }

    private Optional<BuildResult> handleResponse(
            MinimizedTask rexTask,
            ServerResponse responseObject,
            Optional<Object> bpmResponse) {
        switch (responseObject.getOrigin()) {
            case REMOTE_ENTITY:
                return parseBPMResult(rexTask, bpmResponse);
            case REX_INTERNAL_ERROR:
                return parseInternalError(rexTask, bpmResponse);
            default:
                throw new InternalServerErrorException("Unknown origin.");
        }
    }

    private Optional<BuildResult> parseInternalError(MinimizedTask rexTask, Optional<Object> bpmResponse) {
        Optional<ErrorResponse> errorOpt = Optional
                .ofNullable(jsonMapper.convertValue(bpmResponse, ErrorResponse.class));
        if (errorOpt.isPresent()) {
            ErrorResponse error = errorOpt.get();
            return Optional.of(
                    createEmptyExceptionalResult(
                            new ProcessException(
                                    "REX Internal Error for build " + rexTask.getName() + ". Exception: "
                                            + error.getErrorType() + "\nException Message: " + error.getErrorMessage()
                                            + "\nREX Details: " + error.getDetails().toString())));
        }
        return Optional.of(
                createEmptyExceptionalResult(
                        new ProcessException(
                                "Rex Internal Error for build " + rexTask.getName() + ". Can't parse the error.")));
    }

    private Optional<BuildResult> parseBPMResult(MinimizedTask rexTask, Optional<Object> bpmResponse) {
        Optional<BuildResult> buildResult;
        try {
            // valid response from BPM
            var buildResultRest = jsonMapper.convertValue(bpmResponse, BuildResultRest.class);
            if (buildResultRest != null) {
                ValidationBuilder.validateObject(buildResultRest, WhenCreatingNew.class).validateAnnotations();
            }

            buildResult = Optional.ofNullable(mapper.toEntity(buildResultRest));
        } catch (IllegalArgumentException e) {
            // Can't parse BPM message or Rex finished Task with Server error
            buildResult = Optional.of(
                    createEmptyExceptionalResult(
                            new ProcessException(
                                    "Can't parse result for build " + rexTask.getName() + ". " + e.getMessage())));
        }
        return buildResult;
    }

    private BuildResult createEmptyExceptionalResult(ProcessException exception) {
        return new BuildResult(
                CompletionStatus.SYSTEM_ERROR,
                Optional.of(exception),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Map.of());
    }
}
