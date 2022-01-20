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
package org.jboss.pnc.datastore.repositories;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestoneReleasePredicates;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/30/16 Time: 2:03 PM
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductMilestoneReleaseRepositoryImplTest {
    @Inject
    private ProductMilestoneRepository milestoneRepository;
    @Inject
    private ProductMilestoneReleaseRepository releaseRepository;
    @Inject
    private ProductRepository productRepository;
    @Inject
    private ProductVersionRepository productVersionRepository;

    private static ProductMilestone milestone1;
    private static ProductMilestoneRelease release1;
    private static ProductMilestoneRelease release2;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    @InSequence(1)
    public void shouldReturnProperLatest() {

        milestone1 = createMilestone();
        ProductMilestone milestone2 = createMilestone();

        release1 = new ProductMilestoneRelease();
        release1.setId(Sequence.nextId());
        release1.setMilestone(milestone1);
        release1.setStatus(MilestoneCloseStatus.FAILED);
        releaseRepository.save(release1);

        release2 = new ProductMilestoneRelease();
        release2.setId(Sequence.nextId());
        release2.setMilestone(milestone1);
        release2.setStatus(MilestoneCloseStatus.SUCCEEDED);
        releaseRepository.save(release2);

        ProductMilestoneRelease r3 = new ProductMilestoneRelease();
        r3.setId(Sequence.nextId());
        r3.setMilestone(milestone2);
        r3.setStatus(MilestoneCloseStatus.IN_PROGRESS);
        releaseRepository.save(r3);

        ProductMilestoneRelease latestByMilestone = releaseRepository.findLatestByMilestone(milestone1);
        assertThat(latestByMilestone).isNotNull();
        assertThat(latestByMilestone.getStatus()).isEqualTo(MilestoneCloseStatus.FAILED);
    }

    @Test
    @InSequence(2)
    public void shouldReturnForMilestone() {
        Integer milestoneId = milestone1.getId();
        List<ProductMilestoneRelease> result = releaseRepository
                .queryWithPredicates(ProductMilestoneReleasePredicates.withMilestoneId(milestoneId));

        assertThat(result.size()).isEqualTo(2);
        List<Long> ids = result.stream().map(ProductMilestoneRelease::getId).collect(Collectors.toList());
        assertThat(ids).containsExactlyInAnyOrder(release1.getId(), release2.getId());
    }

    @Test
    @InSequence(3)
    public void shouldReturnForMilestoneWithStatus() {
        Integer milestoneId = milestone1.getId();
        List<ProductMilestoneRelease> result = releaseRepository.queryWithPredicates(
                ProductMilestoneReleasePredicates.withMilestoneId(milestoneId),
                ProductMilestoneReleasePredicates.withStatus(MilestoneCloseStatus.SUCCEEDED));

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(release2.getId());
    }

    private ProductMilestone createMilestone() {
        ProductMilestone milestone = new ProductMilestone();
        milestone.setVersion(randomNumeric(3) + "." + randomNumeric(3) + "." + randomNumeric(3) + ".ER1");
        milestone.setProductVersion(createProductVersion());
        milestoneRepository.save(milestone);
        return milestone;
    }

    private ProductVersion createProductVersion() {
        final String version = randomNumeric(2) + "." + randomNumeric(2);
        final Product product = createProduct();

        ProductVersion productVersion = ProductVersion.Builder.newBuilder()
                .version(version)
                .product(product)
                .generateBrewTagPrefix(
                        product.getAbbreviation(),
                        version,
                        "${product_short_name}-${product_version}-pnc")
                .build();

        productVersionRepository.save(productVersion);
        return productVersion;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setName(RandomStringUtils.randomAlphabetic(10));
        product.setAbbreviation(RandomStringUtils.randomAlphabetic(3));
        productRepository.save(product);
        return product;
    }
}