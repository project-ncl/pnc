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

import org.commonjava.atlas.maven.ident.ref.InvalidRefException;
import org.jboss.pnc.api.enums.Qualifier;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.common.sql.NativeQueryBuilder;
import org.jboss.pnc.coordinator.maintenance.BlacklistAsyncInvoker;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.ArtifactRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.ws.rs.NotFoundException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.mapping;
import static org.jboss.pnc.api.enums.Qualifier.*;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDeliveredInProductMilestone;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withTargetRepositoryId;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@PermitAll
@Stateless
@Slf4j
@SuppressWarnings("deprecation")
public class ArtifactProviderImpl
        extends AbstractUpdatableProvider<Integer, Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>
        implements ArtifactProvider {

    private static Logger logger = LoggerFactory.getLogger(ArtifactProviderImpl.class);

    private static final EnumSet<ArtifactQuality> USER_ALLOWED_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.NEW, ArtifactQuality.VERIFIED, ArtifactQuality.TESTED, ArtifactQuality.DEPRECATED);

    private static final EnumSet<ArtifactQuality> ADMIN_ALLOWED_ARTIFACT_QUALITIES = EnumSet.of(
            ArtifactQuality.NEW,
            ArtifactQuality.VERIFIED,
            ArtifactQuality.TESTED,
            ArtifactQuality.DEPRECATED,
            ArtifactQuality.BLACKLISTED,
            ArtifactQuality.DELETED);

    private static final EnumSet<ArtifactQuality> NOT_MODIFIABLE_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.DELETED, ArtifactQuality.BLACKLISTED);

    private static final EnumSet<ArtifactQuality> DA_SYNCRONIZED_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.DELETED, ArtifactQuality.BLACKLISTED);

    private static final String MVN_BLACKLIST_JSON_PAYLOAD = "{\"groupId\":\"%s\",\"artifactId\":\"%s\",\"version\":\"%s\"}";

    private static final String ID_SUFFIX = "_id";

    private ArtifactRevisionMapper artifactRevisionMapper;

    private ProductMilestoneMapper productMilestoneMapper;

    private ArtifactAuditedRepository artifactAuditedRepository;

    private BlacklistAsyncInvoker blacklistAsyncInvoker;

    private UserService userService;

    private UserMapper userMapper;

    @Inject
    private EntityManager em;

    @Inject
    public ArtifactProviderImpl(
            ArtifactRepository repository,
            ArtifactMapper mapper,
            ArtifactRevisionMapper artifactRevisionMapper,
            ProductMilestoneMapper productMilestoneMapper,
            ArtifactAuditedRepository artifactAuditedRepository,
            BlacklistAsyncInvoker blacklistAsyncInvoker,
            UserService userService,
            UserMapper userMapper) {
        super(repository, mapper, Artifact.class);
        this.artifactRevisionMapper = artifactRevisionMapper;
        this.productMilestoneMapper = productMilestoneMapper;
        this.artifactAuditedRepository = artifactAuditedRepository;
        this.blacklistAsyncInvoker = blacklistAsyncInvoker;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getAll(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Optional<String> sha256,
            Optional<String> md5,
            Optional<String> sha1) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withSha256(sha256),
                withMd5(md5),
                withSha1(sha1));
    }

    @Override
    public Page<ArtifactInfo> getAllFiltered(
            int pageIndex,
            int pageSize,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories,
            Set<QValue> requestedQualifiers) {

        Map<Integer, Object> queryParams = new HashMap<>();
        AtomicInteger paramCounter = new AtomicInteger(1);

        Map<Qualifier, List<String[]>> qualifierMap = requestedQualifiers.stream()
                .collect(groupingBy(QValue::getQualifier, mapping(QValue::getValue, toList())));

        String baseQuery = baseArtifactQuery(
                pageSize,
                pageIndex,
                identifierPattern,
                qualities,
                repoType,
                buildCategories,
                queryParams,
                paramCounter);
        String outerQuery = artifactInfoNativeQuery(baseQuery, repoType, qualifierMap, queryParams, paramCounter);

        // NATIVE SQL queries are required to be able to join Build and BuildConfiguration/BuildConfigurationAudited
        // ADDITIONALLY, these are STRICTLY required to be portable between DBMSs
        List<Tuple> list = doNativeQuery(outerQuery, queryParams);

        List<ArtifactInfo> artifacts = list.stream()
                .peek((object) -> log.debug(object.toString()))
                .collect(groupingBy(tuple -> tuple.get("identifier", String.class)))
                .values()
                .stream()
                .map(tuples -> mapTuplesToArtifactInfo(tuples, qualifierMap))
                .collect(toList());

        queryParams.clear();
        paramCounter.set(1);
        String countQuery = countArtifactQuery(
                identifierPattern,
                qualities,
                repoType,
                buildCategories,
                queryParams,
                paramCounter);

        int totalHits = doNativeQuery(countQuery, queryParams).get(0).get(0, BigInteger.class).intValue();
        int totalPages = (totalHits + pageSize - 1) / pageSize;

        return new Page<>(pageIndex, pageSize, totalPages, totalHits, artifacts);
    }

    private List<Tuple> doNativeQuery(String sql, Map<Integer, Object> queryParameters) {
        Query query = em.createNativeQuery(sql, Tuple.class);
        queryParameters.forEach(query::setParameter);

        return (List<Tuple>) query.getResultList();
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public org.jboss.pnc.dto.Artifact store(org.jboss.pnc.dto.Artifact restEntity) throws DTOValidationException {
        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        Instant now = Instant.now();
        return super.store(
                restEntity.toBuilder()
                        .creationUser(user)
                        .modificationUser(user)
                        .creationTime(now)
                        .modificationTime(now)
                        .build());
    }

    @Override
    protected void preUpdate(Artifact dbEntity, org.jboss.pnc.dto.Artifact restEntity) {
        if (!equalAuditedValues(dbEntity, restEntity)) {
            // Changes to audit, set the modificationUser and modificationTime to new values
            org.jboss.pnc.model.User currentUser = userService.currentUser();
            dbEntity.setModificationUser(currentUser);
            dbEntity.setModificationTime(new Date());
        }
    }

    private boolean equalAuditedValues(Artifact persisted, org.jboss.pnc.dto.Artifact toUpdate) {
        return Objects.equals(persisted.getArtifactQuality(), toUpdate.getArtifactQuality())
                && Objects.equals(persisted.getQualityLevelReason(), toUpdate.getQualityLevelReason());
    }

    @Override
    public ArtifactRevision createQualityLevelRevision(String id, String quality, String reason)
            throws DTOValidationException {

        boolean isLoggedInUserSystemUser = userService.hasLoggedInUserRole(SYSTEM_USER);

        ArtifactQuality newQuality = validateProvidedArtifactQuality(quality, isLoggedInUserSystemUser);

        org.jboss.pnc.dto.Artifact artifact = getSpecific(id);
        if (artifact == null) {
            throw new InvalidEntityException("Artifact with id: " + id + " does not exist.");
        }

        validateIfArtifactQualityIsModifiable(artifact, newQuality);

        update(id, artifact.toBuilder().artifactQuality(newQuality).qualityLevelReason(reason).build());

        ArtifactAudited latestRevision = artifactAuditedRepository.findLatestById(Integer.parseInt(id));
        if (latestRevision == null) {
            throw new RepositoryViolationException("Entity should exist in the DB");
        }

        if (DA_SYNCRONIZED_ARTIFACT_QUALITIES.contains(newQuality)) {
            String jsonPayload = createBlacklistJSONPayload(artifact);
            blacklistAsyncInvoker.notifyBlacklistToDA(jsonPayload);
        }

        return artifactRevisionMapper.toDTO(latestRevision);
    }

    private String createBlacklistJSONPayload(org.jboss.pnc.dto.Artifact artifact) {
        switch (artifact.getTargetRepository().getRepositoryType()) {
            case MAVEN: {
                try {
                    Gav gav = Gav.parse(artifact.getIdentifier());
                    return String.format(
                            MVN_BLACKLIST_JSON_PAYLOAD,
                            gav.getGroupId(),
                            gav.getArtifactId(),
                            gav.getVersion());
                } catch (InvalidRefException e) {
                    log.info(
                            "Gav coordinates could not be calculated for identifier '{}', the artifact will not be blocklisted in DA",
                            artifact.getIdentifier());
                    return null;
                }
            }
            default:
                return null;
        }
    }

    @Override
    @DenyAll
    public void delete(String id) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getBuiltArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getArtifactsForTargetRepository(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Integer targetRepositoryId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withTargetRepositoryId(targetRepositoryId));
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getDependantArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDependantBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getDeliveredArtifactsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String milestoneId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDeliveredInProductMilestone(productMilestoneMapper.getIdMapper().toEntity(milestoneId)));
    }

    @Override
    public Page<ArtifactRevision> getRevisions(int pageIndex, int pageSize, String id) {
        List<ArtifactAudited> auditedBuildConfigs = artifactAuditedRepository
                .findAllByIdOrderByRevDesc(Integer.valueOf(id));

        List<ArtifactRevision> toReturn = nullableStreamOf(auditedBuildConfigs).map(artifactRevisionMapper::toDTO)
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(toList());

        int totalHits = auditedBuildConfigs.size();
        int totalPages = (totalHits + pageSize - 1) / pageSize;

        return new Page<>(pageIndex, pageSize, totalPages, totalHits, toReturn);
    }

    @Override
    public ArtifactRevision getRevision(String id, Integer rev) {
        IdRev idRev = new IdRev(Integer.valueOf(id), rev);
        ArtifactAudited auditedArtifact = artifactAuditedRepository.queryById(idRev);

        return artifactRevisionMapper.toDTO(auditedArtifact);
    }

    @Override
    public org.jboss.pnc.dto.Artifact getSpecificFromPurl(String purl) {
        Artifact artifact = ((ArtifactRepository) repository).withPurl(purl);
        if (artifact == null) {
            throw new NotFoundException();
        }
        return mapper.toDTO(artifact);
    }

    /**
     * This method enhances base ArtifactInfo query with Artifact's Qualifier by using additional joins.
     *
     *
     * Amount of rows retrieved by this query can be larger than requested pageSize if any of Qualifiers is multi-valued
     * (f.e. DEPENDENCY is multi-valued because one Artifact can have multiple dependencies). For each value, there is
     * an additional row. The amount of additional rows should be kept to minimum.
     *
     * @param baseArtifactQuery base SQL to retrieve at most pageSize amount of unique Artifact
     * @param repoType
     * @param qualifierMap map of requested Qualifiers with values
     * @param queryParams map of positional parameters
     * @param paramCounter counter for next parameter position
     * @return Full ArtifactInfo SQL query with all necessary data for requested qualifiers
     */
    private String artifactInfoNativeQuery(
            String baseArtifactQuery,
            Optional<RepositoryType> repoType,
            Map<Qualifier, List<String[]>> qualifierMap,
            Map<Integer, Object> queryParams,
            AtomicInteger paramCounter) {
        NativeQueryBuilder builder = NativeQueryBuilder.builder();

        builder.select("art", Artifact_.ID, "id")
                .select("art", Artifact_.IDENTIFIER, "identifier")
                .select("art", Artifact_.ARTIFACT_QUALITY, "QUALITY")
                .select("art", Artifact_.BUILD_CATEGORY, "BUILD_CATEGORY");
        repoType.ifPresent(ign -> builder.select("art", TargetRepository_.REPOSITORY_TYPE, "repoType"));

        builder.from('(' + baseArtifactQuery + ')', "art");

        qualifierMap.forEach((qualifier, values) -> {
            switch (qualifier) {
                case PRODUCT:
                    // PRODUCT.abbr requires BUILD,MILESTONE,VERSION,PRODUCT
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(MILESTONE, builder);
                    requiresJoinOf(VERSION, builder);
                    requiresJoinOf(PRODUCT, builder);
                    requiresSelect(PRODUCT, builder);
                    break;
                case PRODUCT_ID:
                    // PRODUCT.id requires BUILD,MILESTONE,VERSION
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(MILESTONE, builder);
                    requiresJoinOf(VERSION, builder);
                    builder.select("pv", ProductVersion_.PRODUCT + ID_SUFFIX, qualifier.name());
                    break;
                case VERSION:
                    // 'PRODUCT.abbr PRODUCTVERSION.version' require BUILD,MILESTONE,VERSION,PRODUCT
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(MILESTONE, builder);
                    requiresJoinOf(VERSION, builder);
                    requiresJoinOf(PRODUCT, builder);

                    requiresSelect(PRODUCT, builder);
                    builder.select("pv", ProductVersion_.VERSION, qualifier.name());
                    break;
                case VERSION_ID:
                    // requires BUILD,MILESTONE
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(MILESTONE, builder);

                    builder.select("pm", ProductMilestone_.PRODUCT_VERSION + ID_SUFFIX, qualifier.name());
                    break;
                case MILESTONE:
                    // 'PRODUCT.abbr PRODUCTVERSION.version' require BUILD,MILESTONE,VERSION,PRODUCT
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(MILESTONE, builder);
                    requiresJoinOf(VERSION, builder);
                    requiresJoinOf(PRODUCT, builder);

                    requiresSelect(PRODUCT, builder);
                    builder.select("pm", ProductMilestone_.VERSION, qualifier.name());
                    break;
                case MILESTONE_ID:
                    // requires BUILD
                    requiresJoinOf(BUILD, builder);

                    builder.select("br", BuildRecord_.PRODUCT_MILESTONE + ID_SUFFIX, qualifier.name());
                    break;
                case GROUP_BUILD:
                    // requires BUILD
                    requiresJoinOf(BUILD, builder);

                    builder.select("br", BuildRecord_.BUILD_CONFIG_SET_RECORD + ID_SUFFIX, qualifier.name());
                    break;
                case BUILD:
                    // requires <<nothing>>
                    builder.select("art", Artifact_.BUILD_RECORD + ID_SUFFIX, qualifier.name());
                    break;
                case BUILD_CONFIG:
                    // requires BUILD, BUILD_CONFIG
                    // don't join with audited table to have latest data
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(BUILD_CONFIG, builder);

                    builder.select("bc", BuildConfiguration_.NAME, qualifier.name());
                    break;
                case BUILD_CONFIG_ID:
                    // requires BUILD
                    requiresJoinOf(BUILD, builder);

                    builder.select("br", "buildconfiguration_id", qualifier.name());
                    break;
                case GROUP_CONFIG:
                    // requires BUILD, GROUP_BUILD, GROUP_CONFIG
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(GROUP_BUILD, builder);
                    requiresJoinOf(GROUP_CONFIG, builder);

                    builder.select("bcs", BuildConfigurationSet_.NAME, qualifier.name());
                    break;
                case GROUP_CONFIG_ID:
                    // requires BUILD, GROUP_BUILD
                    requiresJoinOf(BUILD, builder);
                    requiresJoinOf(GROUP_BUILD, builder);

                    builder.select("bcsr", BuildConfigSetRecord_.BUILD_CONFIGURATION_SET + ID_SUFFIX, qualifier.name());
                    break;
                case DEPENDENCY: // MULTI-VALUE
                    // requires DEPENDENCY_MAP
                    List<String> parameters = values.stream()
                            .peek(
                                    qValue -> queryParams.put(
                                            paramCounter.getAndIncrement(),
                                            LongBase32IdConverter.toLong(qValue[0])))
                            .map(ign -> " ? ")
                            .collect(toList());
                    builder.join(
                            "LEFT",
                            "build_record_artifact_dependencies_map",
                            "depmap",
                            "art.id = depmap.dependency_artifact_id AND depmap.build_record_id IN ("
                                    + String.join(",", parameters) + ")"); // query only dependencies in request
                    builder.select("depmap", "build_record_id", qualifier.name());
                    break;
                case QUALITY: // MULTI-VALUE in the future
                    // requires <<nothing>>
                    // do nothing, handled by select QUALITY at the start of method
                    break;
            }
        });

        return builder.build();
    }

    /**
     * Used for Joins that can repeat between Qualifiers. For example a lot of Qualifiers require join with BuildRecord.
     */
    private void requiresJoinOf(Qualifier toJoin, NativeQueryBuilder builder) {
        switch (toJoin) {
            case PRODUCT:
                builder.requiresJoin("LEFT", "Product", "prod", "pv.product_id = prod.id");
                break;
            case VERSION:
                builder.requiresJoin("LEFT", "ProductVersion", "pv", "pm.productversion_id = pv.id");
                break;
            case MILESTONE:
                builder.requiresJoin("LEFT", "ProductMilestone", "pm", "br.productmilestone_id = pm.id");
                break;
            case GROUP_BUILD:
                builder.requiresJoin("LEFT", "BuildConfigSetRecord", "bcsr", "br.buildconfigsetrecord_id = bcsr.id");
                break;
            case BUILD:
                builder.requiresJoin("LEFT", "BuildRecord", "br", "art.buildrecord_id = br.id");
                break;
            case BUILD_CONFIG:
                // Join with Audited configuration or Regular one?
                builder.requiresJoin("LEFT", "BuildConfiguration", "bc", "br.buildconfiguration_id = bc.id");
                break;
            case GROUP_CONFIG:
                builder.requiresJoin("LEFT", "BuildConfigurationSet", "bcs", "bcsr.buildconfigurationset_id = bcs.id");
                break;
        }
    }

    /**
     * Used for Joins that can repeat between Qualifiers. For example multiple Qualifiers require product abbreviation.
     */
    private void requiresSelect(Qualifier toSelect, NativeQueryBuilder builder) {
        switch (toSelect) {
            case PRODUCT:
                builder.requiresSelect("prod", Product_.ABBREVIATION, PRODUCT.name());
                break;
        }
    }

    /**
     * Base ArtifactInfo query. It has to be paginated and sorted.
     *
     * @return SQL query for pageSize unique artifacts
     */
    private String baseArtifactQuery(
            int pageSize,
            int pageIndex,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories,
            Map<Integer, Object> queryParameters,
            AtomicInteger parameterCounter) {
        NativeQueryBuilder builder = NativeQueryBuilder.builder();

        builder.select("a", Artifact_.ID)
                .select("a", Artifact_.IDENTIFIER)
                .select("a", Artifact_.ARTIFACT_QUALITY)
                .select("a", Artifact_.BUILD_CATEGORY)
                // used often for joins in outer query
                .select("a", Artifact_.BUILD_RECORD + ID_SUFFIX);

        if (repoType.isPresent()) {
            builder.select("tr", TargetRepository_.REPOSITORY_TYPE);
        }

        baseArtifactQueryBody(
                builder,
                identifierPattern,
                qualities,
                repoType,
                buildCategories,
                queryParameters,
                parameterCounter);

        builder.orderBy("a." + Artifact_.ID + " ASC");
        builder.limit(pageSize);
        builder.offset(pageIndex * pageSize);
        return builder.build();
    }

    private String countArtifactQuery(
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories,
            Map<Integer, Object> queryParameters,
            AtomicInteger parameterCounter) {
        NativeQueryBuilder builder = NativeQueryBuilder.builder();
        builder.select("count (*)");

        baseArtifactQueryBody(
                builder,
                identifierPattern,
                qualities,
                repoType,
                buildCategories,
                queryParameters,
                parameterCounter);

        return builder.build();
    }

    private static void baseArtifactQueryBody(
            NativeQueryBuilder builder,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories,
            Map<Integer, Object> queryParameters,
            AtomicInteger parameterCounter) {
        builder.from("artifact", "a");

        if (identifierPattern.isPresent()) {
            queryParameters.put(parameterCounter.getAndIncrement(), identifierPattern.get().replace("*", "%"));
            builder.where(Artifact_.IDENTIFIER + " LIKE ?");
        }

        if (!qualities.isEmpty()) {
            Set<String> strings = qualities.stream().map(e -> '\'' + e.toString() + '\'').collect(toSet());
            // NO need to parametrize for enums
            builder.where(Artifact_.ARTIFACT_QUALITY + " in (" + String.join(",", strings) + ")");
        }

        if (!buildCategories.isEmpty()) {
            Set<String> strings = buildCategories.stream().map(e -> '\'' + e.toString() + '\'').collect(toSet());
            // NO need to parametrize for enums
            builder.where(Artifact_.BUILD_CATEGORY + " in (" + String.join(",", strings) + ")");
        }

        if (repoType.isPresent()) {
            builder.join(
                    "LEFT",
                    "TargetRepository",
                    "tr",
                    format("a.{0} = tr.{1}", Artifact_.TARGET_REPOSITORY + ID_SUFFIX, TargetRepository_.ID));
            String trColumn = "tr." + TargetRepository_.REPOSITORY_TYPE;
            String repoTypeString = repoType.get().toString();
            // NO need to parametrize for enums
            builder.where(trColumn + " = '" + repoTypeString + "'");
        }
    }

    private Predicate<Artifact>[] getPredicates(
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories) {

        List<Predicate<Artifact>> predicates = new ArrayList<>(4);

        if (identifierPattern.isPresent()) {
            Predicate<Artifact> withIdentifierLike = ArtifactPredicates
                    .withIdentifierLike(identifierPattern.get().replace("*", "%"));
            predicates.add(withIdentifierLike);
        }

        if (!qualities.isEmpty()) {
            Predicate<Artifact> withQualityIn = ArtifactPredicates.withArtifactQualityIn(qualities);
            predicates.add(withQualityIn);
        }

        if (!buildCategories.isEmpty()) {
            Predicate<Artifact> withBuildCategoryIn = ArtifactPredicates.withBuildCategoryIn(buildCategories);
            predicates.add(withBuildCategoryIn);
        }

        if (repoType.isPresent()) {
            Predicate<Artifact> withRepoType = ArtifactPredicates.withTargetRepositoryRepositoryType(repoType.get());
            predicates.add(withRepoType);
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private ArtifactInfo mapTuplesToArtifactInfo(List<Tuple> tuples, Map<Qualifier, List<String[]>> qualifierMap) {
        ArtifactInfo baseInfo = mapNamedTupleToArtifactInfo(tuples.get(0));
        for (Tuple tuple : tuples) {
            for (Map.Entry<Qualifier, List<String[]>> entry : qualifierMap.entrySet()) {

                Qualifier qualifier = entry.getKey();
                List<String[]> allowedValues = entry.getValue();

                String[] parsedValue = parseValue(tuple, qualifier);

                if (allowedValues.stream().anyMatch(value -> Arrays.equals(value, parsedValue))) {
                    String joinedValue = String.join(" ", parsedValue);

                    if (baseInfo.getQualifiers().containsKey(qualifier)) {
                        baseInfo.getQualifiers().get(qualifier).add(joinedValue);
                    } else {
                        baseInfo.getQualifiers().put(qualifier, new HashSet<>());
                        baseInfo.getQualifiers().get(qualifier).add(joinedValue);
                    }
                }
            }
        }

        return baseInfo;
    }

    private static String[] parseValue(Tuple tuple, Qualifier qualifier) {
        String[] parsedValue;
        switch (qualifier) {
            case VERSION:
            case MILESTONE:
                String productAbbr = tuple.get(PRODUCT.name(), String.class);
                String versionPart = tuple.get(qualifier.name(), String.class);
                parsedValue = new String[] { productAbbr, versionPart };
                break;
            case BUILD:
            case DEPENDENCY:
                BigInteger toConvertId = tuple.get(qualifier.name(), BigInteger.class);
                parsedValue = new String[] {
                        LongBase32IdConverter.toString(toConvertId == null ? null : toConvertId.longValue()) };
                break;
            case GROUP_BUILD:
                BigInteger bigIntId = tuple.get(qualifier.name(), BigInteger.class);
                parsedValue = new String[] { bigIntId == null ? null : String.valueOf(bigIntId.longValue()) };
                break;
            case PRODUCT_ID:
            case VERSION_ID:
            case MILESTONE_ID:
            case BUILD_CONFIG_ID:
            case GROUP_CONFIG_ID:
                Integer intId = tuple.get(qualifier.name(), Integer.class);
                parsedValue = new String[] { intId == null ? null : intId.toString() };
                break;
            case PRODUCT:
            case BUILD_CONFIG:
            case GROUP_CONFIG:
            case QUALITY:
                parsedValue = new String[] { tuple.get(qualifier.name(), String.class) };
                break;
            default:
                throw new IllegalArgumentException("Unknown qualifier " + qualifier.name());
        }
        return parsedValue;
    }

    private ArtifactInfo mapTupleToArtifactInfo(Tuple tuple) {

        return ArtifactInfo.builder()
                .id(tuple.get(0).toString())
                .identifier(tuple.get(1).toString())
                .artifactQuality((ArtifactQuality) tuple.get(2))
                .repositoryType((RepositoryType) tuple.get(3))
                .buildCategory((BuildCategory) tuple.get(4))
                .build();
    }

    private ArtifactInfo mapNamedTupleToArtifactInfo(Tuple tuple) {

        ArtifactInfo.Builder builder = ArtifactInfo.builder()
                .id(tuple.get("id", Integer.class).toString())
                .identifier(tuple.get("identifier", String.class))
                .artifactQuality(ArtifactQuality.valueOf(tuple.get("QUALITY", String.class)))
                .buildCategory(BuildCategory.valueOf(tuple.get("BUILD_CATEGORY", String.class)))
                .qualifiers(new HashMap<>());

        try {
            builder.repositoryType(RepositoryType.valueOf(tuple.get("repoType", String.class)));
        } catch (IllegalArgumentException e) {
            // not found
        }

        return builder.build();
    }

    private ArtifactQuality validateProvidedArtifactQuality(String quality, boolean isLoggedInUserSystemUser) {

        ArtifactQuality newQuality;
        try {
            newQuality = ArtifactQuality.valueOf(quality.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEntityException("Artifact quality: " + quality + " does not exist.");
        }

        // User can specify NEW, TESTED, VERIFIED, DEPRECATED quality levels; admins can also specify DELETED and
        // BLACKLISTED
        EnumSet<ArtifactQuality> allowedQualities = isLoggedInUserSystemUser ? ADMIN_ALLOWED_ARTIFACT_QUALITIES
                : USER_ALLOWED_ARTIFACT_QUALITIES;

        if (!allowedQualities.contains(newQuality)) {
            throw new InvalidEntityException("Artifact quality level can be changed only to " + allowedQualities);
        }

        return newQuality;
    }

    private void validateIfArtifactQualityIsModifiable(
            org.jboss.pnc.dto.Artifact artifact,
            ArtifactQuality newQuality) {

        // If the quality level is not changing (e.g. in a bulk update after a single update), ignore the checks below
        if (artifact.getArtifactQuality().equals(newQuality)) {
            return;
        }

        // Artifacts with DELETED, BLACKLISTED quality levels cannot be changed
        if (NOT_MODIFIABLE_ARTIFACT_QUALITIES.contains(artifact.getArtifactQuality())) {
            throw new ConflictedEntryException(
                    "Artifact " + artifact.getId() + " with quality " + artifact.getArtifactQuality()
                            + " cannot be changed to another quality level.",
                    Artifact.class,
                    artifact.getId());
        }

        // If the artifact is TEMPORARY, quality level can only change to DELETED
        if (ArtifactQuality.TEMPORARY.equals(artifact.getArtifactQuality())
                && !newQuality.equals(ArtifactQuality.DELETED)) {

            throw new ConflictedEntryException(
                    "Temporary artifact " + artifact.getId() + " can only be changed to DELETED quality level.",
                    Artifact.class,
                    artifact.getId());
        }
    }

}
