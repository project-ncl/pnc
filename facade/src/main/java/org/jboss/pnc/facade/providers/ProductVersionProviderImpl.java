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

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.common.Maps;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneArtifactQualityStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneRepositoryTypeStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionDeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionStatistics;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;

@PermitAll
@Stateless
@Slf4j
public class ProductVersionProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.ProductVersion, ProductVersion, ProductVersionRef>
        implements ProductVersionProvider {

    private final ProductVersionRepository versionRepository;
    private final ProductRepository productRepository;
    private final ProductMilestoneRepository milestoneRepository;
    private final BuildConfigurationSetRepository groupConfigRepository;
    private final SystemConfig systemConfig;
    private final BuildConfigurationRepository buildConfigurationRepository;
    private final ProductMilestoneMapper milestoneMapper;

    @Inject
    public ProductVersionProviderImpl(
            ProductVersionRepository repository,
            ProductVersionMapper mapper,
            ProductMilestoneMapper milestoneMapper,
            ProductRepository productRepository,
            ProductMilestoneRepository milestoneRepository,
            BuildConfigurationSetRepository groupConfigRepository,
            BuildConfigurationRepository buildConfigurationRepository,
            SystemConfig systemConfig) {

        super(repository, mapper, org.jboss.pnc.model.ProductVersion.class);

        this.versionRepository = repository;
        this.milestoneMapper = milestoneMapper;
        this.productRepository = productRepository;
        this.groupConfigRepository = groupConfigRepository;
        this.systemConfig = systemConfig;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.milestoneRepository = milestoneRepository;
    }

    @Override
    public ProductVersion store(ProductVersion restEntity) {
        validateBeforeSaving(restEntity);
        org.jboss.pnc.model.ProductVersion productVersionRestDb = mapper.toEntity(restEntity);

        Product product = productRepository.queryById(Integer.valueOf(restEntity.getProduct().getId()));

        productVersionRestDb.generateBrewTagPrefix(
                product.getAbbreviation(),
                restEntity.getVersion(),
                systemConfig.getBrewTagPattern());

        org.jboss.pnc.model.ProductVersion productVersion = repository.save(productVersionRestDb);
        for (BuildConfiguration bc : productVersionRestDb.getBuildConfigurations()) {
            bc.setProductVersion(productVersion);
        }
        repository.flushAndRefresh(productVersion);
        return mapper.toDTO(productVersion);
    }

    @Override
    protected void validateBeforeSaving(ProductVersion restEntity) {

        super.validateBeforeSaving(restEntity);

        Product product = productRepository.queryById(Integer.valueOf(restEntity.getProduct().getId()));

        if (product == null) {
            throw new InvalidEntityException(
                    "Product with id: " + restEntity.getProduct().getId() + " does not exist.");
        }

        Set<org.jboss.pnc.model.ProductVersion> productVersionList = product.getProductVersions();

        if (productVersionList == null) {
            return;
        }
        productVersionList.stream()
                .filter(pv -> pv.getVersion().equals(restEntity.getVersion()))
                .findFirst()
                .ifPresent(pv -> {
                    throw new ConflictedEntryException(
                            "Product version with version " + restEntity.getVersion() + " already exists",
                            org.jboss.pnc.model.ProductVersion.class,
                            pv.getId().toString());
                });
    }

    @Override
    protected void validateBeforeUpdating(Integer id, ProductVersion restEntity) {
        super.validateBeforeUpdating(id, restEntity);

        validateVersionChange(id, restEntity);
        validateGroupConfigsBeforeUpdating(id, restEntity);
        validateMilestone(id, restEntity);
    }

    private void validateVersionChange(Integer id, ProductVersion restEntity) throws InvalidEntityException {
        org.jboss.pnc.model.ProductVersion entityInDb = findInDB(id);
        boolean changingVersion = !entityInDb.getVersion().equals(restEntity.getVersion());
        if (changingVersion) {
            boolean hasClosedMilestone = entityInDb.getProductMilestones()
                    .stream()
                    .anyMatch(milestone -> milestone.getEndDate() != null);
            if (hasClosedMilestone) {
                throw new InvalidEntityException(
                        "Cannot change version due to having closed milestone. Product version id: " + id);
            }
        }
    }

    private void validateGroupConfigsBeforeUpdating(Integer id, ProductVersion restEntity)
            throws InvalidEntityException, NumberFormatException, ConflictedEntryException {
        if (restEntity.getGroupConfigs() != null) {
            for (String groupConfigId : restEntity.getGroupConfigs().keySet()) {
                BuildConfigurationSet set = groupConfigRepository.queryById(Integer.valueOf(groupConfigId));
                if (set == null) {
                    throw new InvalidEntityException("Group config with id: " + groupConfigId + " does not exist.");
                }
                if (set.getProductVersion() != null && !set.getProductVersion().getId().equals(id)) {
                    throw new ConflictedEntryException(
                            "Group config with id: " + groupConfigId + " already belongs to different product version.",
                            org.jboss.pnc.model.ProductVersion.class,
                            set.getProductVersion().getId().toString());
                }
            }
        }
    }

    private void validateMilestone(Integer id, ProductVersion entity) {
        if (entity.getCurrentProductMilestone() != null) {
            Integer newMilestoneId = milestoneMapper.getIdMapper()
                    .toEntity(entity.getCurrentProductMilestone().getId());
            org.jboss.pnc.model.ProductVersion productVersion = repository.queryById(id);
            ProductMilestone currentMilestone = productVersion.getCurrentProductMilestone();
            if (currentMilestone == null || currentMilestone.getId() != newMilestoneId) {
                ProductMilestone newMilestone = milestoneRepository.queryById(newMilestoneId);
                if (newMilestone == null) {
                    throw new InvalidEntityException("Milestone with id: " + newMilestoneId + " does not exist.");
                } else if (newMilestone.getEndDate() != null) {
                    throw new InvalidEntityException(
                            "Milestone with id: " + newMilestoneId + " is closed, so cannot be set as current.");
                }
            }
        }
    }

    @Override
    public Page<ProductVersion> getAllForProduct(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(Integer.valueOf(productId)));
    }

    @Override
    public ProductVersionStatistics getStatistics(String id) {
        Integer entityId = mapper.getIdMapper().toEntity(id);

        return ProductVersionStatistics.builder()
                .milestones(versionRepository.countMilestonesInVersion(entityId))
                .productDependencies(versionRepository.countProductDependenciesInVersion(entityId))
                .milestoneDependencies(versionRepository.countMilestoneDependenciesInVersion(entityId))
                .artifactsInVersion(versionRepository.countBuiltArtifactsInVersion(entityId))
                .deliveredArtifactsSource(
                        ProductVersionDeliveredArtifactsStatistics.builder()
                                .thisVersion(versionRepository.countDeliveredArtifactsBuiltInThisVersion(entityId))
                                .otherVersions(versionRepository.countDeliveredArtifactsBuiltInOtherVersions(entityId))
                                .otherProducts(versionRepository.countDeliveredArtifactsBuiltByOtherProducts(entityId))
                                .noMilestone(versionRepository.countDeliveredArtifactsBuiltInNoMilestone(entityId))
                                .noBuild(versionRepository.countDeliveredArtifactsNotBuilt(entityId))
                                .build())
                .build();
    }

    @Override
    public Page<ProductMilestoneArtifactQualityStatistics> getArtifactQualitiesStatistics(
            int pageIndex,
            int pageSize,
            String sort,
            String query,
            String id) {
        Integer entityId = mapper.getIdMapper().toEntity(id);
        List<ProductMilestone> productMilestones = getProductMilestones(pageIndex, pageSize, sort, query, entityId);
        List<Tuple> artifactQualities = versionRepository.getArtifactQualityStatistics(
                productMilestones.stream().map(ProductMilestone::getId).collect(Collectors.toSet()));
        Map<Integer, EnumMap<ArtifactQuality, Long>> artifactQualityStatsById = transformToMapByIds(
                artifactQualities,
                ArtifactQuality.class);
        List<ProductMilestoneArtifactQualityStatistics> artifactQualityStatistics = toProductMilestoneArtifactQualityStatistics(
                productMilestones,
                artifactQualityStatsById);

        return new Page<>(
                pageIndex,
                pageSize,
                countAllProductMilestonesOfTheVersion(query, entityId),
                artifactQualityStatistics);
    }

    @Override
    public Page<ProductMilestoneRepositoryTypeStatistics> getRepositoryTypesStatistics(
            int pageIndex,
            int pageSize,
            String sort,
            String query,
            String id) {
        Integer entityId = mapper.getIdMapper().toEntity(id);
        List<ProductMilestone> productMilestones = getProductMilestones(pageIndex, pageSize, sort, query, entityId);
        List<Tuple> repositoryTypes = versionRepository.getRepositoryTypesStatistics(
                productMilestones.stream().map(ProductMilestone::getId).collect(Collectors.toSet()));
        Map<Integer, EnumMap<RepositoryType, Long>> repositoryTypesStatsById = transformToMapByIds(
                repositoryTypes,
                RepositoryType.class);
        List<ProductMilestoneRepositoryTypeStatistics> repositoryTypesStats = toProductMilestoneRepositoryTypeStatistics(
                productMilestones,
                repositoryTypesStatsById);

        return new Page<>(
                pageIndex,
                pageSize,
                countAllProductMilestonesOfTheVersion(query, entityId),
                repositoryTypesStats);
    }

    private List<ProductMilestone> getProductMilestones(
            int pageIndex,
            int pageSize,
            String sort,
            String query,
            Integer entityId) {
        Predicate<ProductMilestone> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(ProductMilestone.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo<ProductMilestone> sortInfo = rsqlPredicateProducer.getSortInfo(ProductMilestone.class, sort);

        return milestoneRepository.queryWithPredicates(
                pageInfo,
                sortInfo,
                rsqlPredicate,
                ProductMilestonePredicates.withProductVersionId(entityId));
    }

    private static <E extends Enum<E>> Map<Integer, EnumMap<E, Long>> transformToMapByIds(
            List<Tuple> tuples,
            Class<E> entityClass) {
        HashMap<Integer, EnumMap<E, Long>> statisticsById = new HashMap<Integer, EnumMap<E, Long>>();

        for (Tuple tuple : tuples) {
            Integer id = tuple.get(0, Integer.class);
            EnumMap<E, Long> statsOfThisId = statisticsById
                    .getOrDefault(id, Maps.initEnumMapWithDefaultValue(entityClass, 0L));
            statsOfThisId.put(tuple.get(1, entityClass), tuple.get(2, Long.class));
            statisticsById.put(id, statsOfThisId);
        }

        return statisticsById;
    }

    private List<ProductMilestoneArtifactQualityStatistics> toProductMilestoneArtifactQualityStatistics(
            List<ProductMilestone> productMilestones,
            Map<Integer, EnumMap<ArtifactQuality, Long>> artifactQualityStatsById) {
        List<ProductMilestoneArtifactQualityStatistics> productMilestoneArtifactQualityStats = new ArrayList<>();

        for (ProductMilestone pm : productMilestones) {
            productMilestoneArtifactQualityStats.add(
                    ProductMilestoneArtifactQualityStatistics.builder()
                            .productMilestone(milestoneMapper.toRef(pm))
                            .artifactQuality(
                                    artifactQualityStatsById.getOrDefault(
                                            pm.getId(),
                                            Maps.initEnumMapWithDefaultValue(ArtifactQuality.class, 0L)))
                            .build());
        }

        return productMilestoneArtifactQualityStats;
    }

    private List<ProductMilestoneRepositoryTypeStatistics> toProductMilestoneRepositoryTypeStatistics(
            List<ProductMilestone> productMilestones,
            Map<Integer, EnumMap<RepositoryType, Long>> repositoryTypesStatsById) {
        List<ProductMilestoneRepositoryTypeStatistics> productMilestoneRepositoryTypeStats = new ArrayList<>();

        for (ProductMilestone pm : productMilestones) {
            productMilestoneRepositoryTypeStats.add(
                    ProductMilestoneRepositoryTypeStatistics.builder()
                            .productMilestone(milestoneMapper.toRef(pm))
                            .repositoryType(
                                    repositoryTypesStatsById.getOrDefault(
                                            pm.getId(),
                                            Maps.initEnumMapWithDefaultValue(RepositoryType.class, 0L)))
                            .build());
        }

        return productMilestoneRepositoryTypeStats;
    }

    private int countAllProductMilestonesOfTheVersion(String query, Integer id) {
        Predicate<ProductMilestone> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(ProductMilestone.class, query);
        return milestoneRepository.count(rsqlPredicate, ProductMilestonePredicates.withProductVersionId(id));
    }
}
