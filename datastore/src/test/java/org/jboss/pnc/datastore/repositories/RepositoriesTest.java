/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
