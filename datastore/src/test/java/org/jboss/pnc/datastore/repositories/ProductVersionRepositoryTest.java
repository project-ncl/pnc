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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductVersionRepositoryTest {

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    @Transactional
    public void shouldGetProductVersion() {
        // given
        Product pruduct = Product.Builder.newBuilder().abbreviation("mp").name("MyProduct").build();
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().version("1.0").product(pruduct).build();

        BuildConfigurationSet buildConfigurationSet = BuildConfigurationSet.Builder.newBuilder().name("set1").build();

        Set<BuildConfigurationSet> buildConfigurationSets = new HashSet<>();
        buildConfigurationSets.add(buildConfigurationSet);
        BuildConfigurationSet buildConfigurationSetSaved = buildConfigurationSetRepository.save(buildConfigurationSet);

        productVersion.setBuildConfigurationSets(buildConfigurationSets);

        // when
        productRepository.save(pruduct);
        ProductVersion saved = productVersionRepository.save(productVersion);

        // then
        ProductVersion productVersionFromDb = productVersionRepository.queryById(productVersion.getId());
        Assert.assertNotNull(productVersionFromDb);
        Assert.assertEquals(productVersionFromDb.getVersion(), productVersion.getVersion());

        assertThat(productVersionFromDb.getBuildConfigurationSets()).containsExactly(buildConfigurationSetSaved);
    }
}
