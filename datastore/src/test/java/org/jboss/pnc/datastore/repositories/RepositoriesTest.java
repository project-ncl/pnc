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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RepositoriesTest {

    @Inject
    ProductRepository productRepository;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    public void testInsertProduct() throws Exception {

        // given
        Assert.assertNotNull(productRepository);
        Product product = Product.Builder.newBuilder()
                .name("Test Product")
                .description("Test")
                .abbreviation("TP")
                .build();

        // when
        product = productRepository.save(product);

        // then
        Assert.assertNotNull(product.getId());
    }

}
