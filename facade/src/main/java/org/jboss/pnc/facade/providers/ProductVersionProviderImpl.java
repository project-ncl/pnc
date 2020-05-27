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
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;

@PermitAll
@Stateless
public class ProductVersionProviderImpl
        extends AbstractProvider<Integer, org.jboss.pnc.model.ProductVersion, ProductVersion, ProductVersionRef>
        implements ProductVersionProvider {

    private ProductRepository productRepository;
    private BuildConfigurationSetRepository groupConfigRepository;
    private SystemConfig systemConfig;

    @Inject
    public ProductVersionProviderImpl(
            ProductVersionRepository repository,
            ProductVersionMapper mapper,
            ProductRepository productRepository,
            BuildConfigurationSetRepository groupConfigRepository,
            SystemConfig systemConfig) {

        super(repository, mapper, org.jboss.pnc.model.ProductVersion.class);

        this.productRepository = productRepository;
        this.groupConfigRepository = groupConfigRepository;
        this.systemConfig = systemConfig;
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
        repository.flushAndRefresh(productVersion);
        return mapper.toDTO(productVersion);
    }

    @Override
    public ProductVersion update(String id, ProductVersion restEntity) {
        validateBeforeUpdating(id, restEntity);
        ProductVersion current = super.getSpecific(id);

        boolean hasClosedMilestone = current.getProductMilestones()
                .values()
                .stream()
                .anyMatch(milestone -> milestone.getEndDate() != null);
        boolean changingVersion = !current.getVersion().equals(restEntity.getVersion());

        if (changingVersion && hasClosedMilestone) {
            throw new InvalidEntityException(
                    "Cannot change version id due to having closed milestone. Product version id: " + id);
        }

        updateGroupConfigs(current, restEntity.getGroupConfigs());

        return super.update(id, restEntity);
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

    @Override
    public Page<ProductVersion> getAllForProduct(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(Integer.valueOf(productId)));
    }

    private void updateGroupConfigs(ProductVersion current, Map<String, GroupConfigurationRef> buildConfigs) {
        Set<String> newIds;
        if (buildConfigs == null) {
            newIds = Collections.emptySet();
        } else {
            newIds = new HashSet<>(buildConfigs.keySet());
        }
        for (String id : current.getGroupConfigs().keySet()) {
            if (!newIds.contains(id)) {
                BuildConfigurationSet set = groupConfigRepository.queryById(Integer.valueOf(id));
                set.setProductVersion(null);
                groupConfigRepository.save(set);
            }
            newIds.remove(id);
        }
        if (!newIds.isEmpty()) {
            org.jboss.pnc.model.ProductVersion productVersion = repository.queryById(Integer.valueOf(current.getId()));
            for (String id : newIds) {
                BuildConfigurationSet set = groupConfigRepository.queryById(Integer.valueOf(id));
                set.setProductVersion(productVersion);
                groupConfigRepository.save(set);
            }
        }
    }
}
