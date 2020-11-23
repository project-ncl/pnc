/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.model.BuildConfiguration;

import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;

@PermitAll
@Stateless
@Slf4j
public class ProductVersionProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.ProductVersion, ProductVersion, ProductVersionRef>
        implements ProductVersionProvider {

    private ProductRepository productRepository;
    private ProductMilestoneRepository milestoneRepository;
    private BuildConfigurationSetRepository groupConfigRepository;
    private SystemConfig systemConfig;
    private BuildConfigurationRepository buildConfigurationRepository;
    private ProductMilestoneMapper milestoneMapper;

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
    protected void validateBeforeUpdating(String id, ProductVersion restEntity) {
        super.validateBeforeUpdating(id, restEntity);

        validateVersionChange(id, restEntity);
        validateGroupConfigsBeforeUpdating(id, restEntity);
        validateMilestone(id, restEntity);
    }

    private void validateVersionChange(String id, ProductVersion restEntity) throws InvalidEntityException {
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

    private void validateGroupConfigsBeforeUpdating(String id, ProductVersion restEntity)
            throws InvalidEntityException, NumberFormatException, ConflictedEntryException {
        if (restEntity.getGroupConfigs() != null) {
            for (String groupConfigId : restEntity.getGroupConfigs().keySet()) {
                BuildConfigurationSet set = groupConfigRepository.queryById(Integer.valueOf(groupConfigId));
                if (set == null) {
                    throw new InvalidEntityException("Group config with id: " + groupConfigId + " does not exist.");
                }
                if (set.getProductVersion() != null && !set.getProductVersion().getId().toString().equals(id)) {
                    throw new ConflictedEntryException(
                            "Group config with id: " + groupConfigId + " already belongs to different product version.",
                            org.jboss.pnc.model.ProductVersion.class,
                            set.getProductVersion().getId().toString());
                }
            }
        }
    }

    private void validateMilestone(String id, ProductVersion entity) {
        if (entity.getCurrentProductMilestone() != null) {
            Integer newMilestoneId = milestoneMapper.getIdMapper()
                    .toEntity(entity.getCurrentProductMilestone().getId());
            org.jboss.pnc.model.ProductVersion productVersion = repository.queryById(mapper.getIdMapper().toEntity(id));
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

}
