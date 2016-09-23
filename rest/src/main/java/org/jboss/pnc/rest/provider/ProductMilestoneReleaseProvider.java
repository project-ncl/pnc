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

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.rest.restmodel.ProductMilestoneReleaseRest;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

@Stateless
public class ProductMilestoneReleaseProvider extends AbstractProvider<ProductMilestoneRelease, ProductMilestoneReleaseRest> {

    private ProductMilestoneRepository milestoneRepository;
    private ProductMilestoneReleaseRepository releaseRepository;

    @Inject
    public ProductMilestoneReleaseProvider(ProductMilestoneReleaseRepository releaseRepository,
                                           ProductMilestoneRepository milestoneRepository,
                                           RSQLPredicateProducer rsqlPredicateProducer,
                                           SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(releaseRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.releaseRepository = releaseRepository;
        this.milestoneRepository = milestoneRepository;
    }

    // needed for EJB/CDI
    @Deprecated
    public ProductMilestoneReleaseProvider() {
    }

    @Override
    protected Function<? super ProductMilestoneRelease, ? extends ProductMilestoneReleaseRest> toRESTModel() {
        return ProductMilestoneReleaseRest::new;
    }

    @Override
    protected Function<? super ProductMilestoneReleaseRest, ? extends ProductMilestoneRelease> toDBModel() {
        throw new IllegalStateException("ProductMilestoneRelease entity is not to be created via REST");
    }

    public ProductMilestoneReleaseRest latestForMilestone(Integer milestoneId) {
        ProductMilestone milestone = milestoneRepository.queryById(milestoneId);

        ProductMilestoneRelease release = milestone == null ? null : releaseRepository.findLatestByMilestone(milestone);

        return release == null ? null : toRESTModel().apply(release);
    }
}
