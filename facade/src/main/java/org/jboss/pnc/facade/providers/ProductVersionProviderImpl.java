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

import org.jboss.pnc.common.Maps;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.datastore.limits.rsql.EmptySortInfo;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneArtifactQualityStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionDeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionStatistics;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Tuple;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;

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
            String id) {
        Integer entityId = mapper.getIdMapper().toEntity(id);

        // List<ProductMilestone> productMilestones = versionRepository.getProductMilestonesTemp(entityId);
        List<ProductMilestone> productMilestones = milestoneRepository.queryWithPredicates(
                new DefaultPageInfo(pageIndex, pageSize),
                new EmptySortInfo<>(), // FIXME
                ProductMilestonePredicates.withProductVersionId(entityId));
        Set<Integer> ids = productMilestones.stream().map(ProductMilestone::getId).collect(Collectors.toSet());
        Map<Integer, EnumMap<ArtifactQuality, Long>> qualityStatistics = transformToMapByIds(
                versionRepository.getArtifactQualityStatistics(ids));
        List<ProductMilestoneArtifactQualityStatistics> artifactQualityStatistics = toProductMilestoneArtifactQualityStatistics(
                productMilestones,
                qualityStatistics);

        return new Page<>(pageIndex, pageSize, artifactQualityStatistics.size(), artifactQualityStatistics);
    }

    private static Map<Integer, EnumMap<ArtifactQuality, Long>> transformToMapByIds(List<Tuple> tuples) {
        var artifactQualities = new HashMap<Integer, EnumMap<ArtifactQuality, Long>>();

        // tuple = (product_milestone_id, product_milestone.artifact_quality, count(product_milestone.artifact_quality))
        for (var t : tuples) {
            var id = t.get(0, Integer.class);
            EnumMap<ArtifactQuality, Long> artifactQualitiesOfThisId = artifactQualities
                    .getOrDefault(id, Maps.initEnumMapWithDefaultValue(ArtifactQuality.class, 0L));
            artifactQualitiesOfThisId.put(t.get(1, ArtifactQuality.class), t.get(2, Long.class));
            artifactQualities.put(id, artifactQualitiesOfThisId);
        }

        return artifactQualities;
    }

    private List<ProductMilestoneArtifactQualityStatistics> toProductMilestoneArtifactQualityStatistics(
            List<ProductMilestone> productMilestones,
            Map<Integer, EnumMap<ArtifactQuality, Long>> qualityStatistics) {
        List<ProductMilestoneArtifactQualityStatistics> productMilestoneArtifactQualityStats = new ArrayList<>();

        for (var pm : productMilestones) {
            productMilestoneArtifactQualityStats.add(
                    ProductMilestoneArtifactQualityStatistics.builder()
                            .productMilestone(
                                    ProductMilestoneRef.refBuilder()
                                            .id(milestoneMapper.getIdMapper().toDto(pm.getId()))
                                            .version(pm.getVersion())
                                            .startingDate(dateToInstant(pm.getStartingDate()))
                                            .endDate(dateToInstant(pm.getEndDate()))
                                            .plannedEndDate(dateToInstant(pm.getPlannedEndDate()))
                                            .build())
                            .artifactQuality(
                                    qualityStatistics.getOrDefault(
                                            pm.getId(),
                                            Maps.initEnumMapWithDefaultValue(ArtifactQuality.class, 0L)))
                            .build());
        }

        return productMilestoneArtifactQualityStats;
    }

    private Instant dateToInstant(Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).orElse(null);
    }
}
