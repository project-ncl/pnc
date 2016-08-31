/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.managers.ProductMilestoneReleaseManager;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.withProductVersionId;

@Stateless
public class ProductMilestoneProvider extends AbstractProvider<ProductMilestone, ProductMilestoneRest> {

    private static final Logger log = LoggerFactory.getLogger(ProductMilestoneProvider.class);

    private ArtifactRepository artifactRepository;
    private ProductMilestoneReleaseManager releaseManager;

    @Inject
    public ProductMilestoneProvider(
            ProductMilestoneRepository productMilestoneRepository,
            ArtifactRepository artifactRepository,
            ProductMilestoneReleaseManager releaseManager,
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(productMilestoneRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.artifactRepository = artifactRepository;
        this.releaseManager = releaseManager;
    }

    // needed for EJB/CDI
    public ProductMilestoneProvider() {
    }

    public CollectionInfo<ProductMilestoneRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql,
                                                                        String query, Integer versionId) {
        return super.queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(versionId));
    }

    @Override
    protected Function<? super ProductMilestone, ? extends ProductMilestoneRest> toRESTModel() {
        return ProductMilestoneRest::new;
    }

    @Override
    protected Function<? super ProductMilestoneRest, ? extends ProductMilestone> toDBModel() {
        return productMilestoneRest -> productMilestoneRest.toDBEntityBuilder().build();
    }

    public void addDistributedArtifact(Integer milestoneId, Integer artifactId) throws ValidationException {
        ProductMilestone milestone = repository.queryById(milestoneId);
        Artifact artifact = artifactRepository.queryById(artifactId);
        ValidationBuilder.validateObject(milestone, WhenUpdating.class)
                .validateCondition(milestone != null, "No product milestone exists with id: " + milestoneId)
                .validateCondition(artifact != null, "No artiffact exists with id: " + artifactId);

        milestone.addDistributedArtifact(artifact);
        repository.save(milestone);
    }

    public void removeDistributedArtifact(Integer milestoneId, Integer artifactId) throws ValidationException {
        ProductMilestone milestone = repository.queryById(milestoneId);
        Artifact artifact = artifactRepository.queryById(artifactId);
        ValidationBuilder.validateObject(milestone, WhenUpdating.class)
                .validateCondition(milestone != null, "No product milestone exists with id: " + milestoneId)
                .validateCondition(artifact != null, "No artifact exists with id: " + artifactId);
        milestone.removeDistributedArtifact(artifact);
        repository.save(milestone);
    }

    @Override
    public void update(Integer id, ProductMilestoneRest restEntity) throws ValidationException {
        log.debug("Updating milestone for id: {}", id);
        restEntity.setId(id);
        validateBeforeUpdating(id, restEntity);
        ProductMilestone milestone = toDBModel().apply(restEntity);

        if (restEntity.getEndDate() != null && releaseManager.noReleaseInProgress(milestone)) {
            log.debug("Milestone end date set and no release in progress, will start release");
            releaseManager.startRelease(milestone);
        }
        repository.save(milestone);
    }
}
