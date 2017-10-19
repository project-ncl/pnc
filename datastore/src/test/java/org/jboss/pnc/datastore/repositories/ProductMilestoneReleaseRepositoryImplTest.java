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
package org.jboss.pnc.datastore.repositories;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.MilestoneReleaseStatus;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductVersion;
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

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/30/16
 * Time: 2:03 PM
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

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    public void shouldReturnProperLatest() {

        ProductMilestone milestone1 = createMilestone();
        ProductMilestone milestone2 = createMilestone();

        milestoneRepository.save(milestone1);
        ProductMilestoneRelease r1 = new ProductMilestoneRelease();
        r1.setMilestone(milestone1);
        r1.setStatus(MilestoneReleaseStatus.FAILED);
        releaseRepository.save(r1);

        ProductMilestoneRelease r2 = new ProductMilestoneRelease();
        r2.setMilestone(milestone1);
        r2.setStatus(MilestoneReleaseStatus.SUCCEEDED);
        releaseRepository.save(r2);

        ProductMilestoneRelease r3 = new ProductMilestoneRelease();
        r3.setMilestone(milestone2);
        r3.setStatus(MilestoneReleaseStatus.IN_PROGRESS);
        releaseRepository.save(r3);

        ProductMilestoneRelease latestByMilestone = releaseRepository.findLatestByMilestone(milestone1);
        assertThat(latestByMilestone).isNotNull();
        assertThat(latestByMilestone.getStatus()).isEqualTo(MilestoneReleaseStatus.SUCCEEDED);
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
                .generateBrewTagPrefix(product.getAbbreviation(), version)
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