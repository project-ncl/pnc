/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new.endpoint;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.patch.GroupConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class GroupConfigurationEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(GroupConfigurationEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Ignore //TODO ENABLE ME
    @Test
    public void shouldPatchGroupConfiguration() throws ClientException, PatchBuilderException {
        GroupConfigurationClient client = new GroupConfigurationClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));

        GroupConfiguration groupConfiguration = client.getAll().iterator().next();
        String id = groupConfiguration.getId();

        ProductVersion newProductVersion = createProductVersion();

        GroupConfigurationPatchBuilder builder = new GroupConfigurationPatchBuilder()
                .replaceProductVersion(newProductVersion);
        GroupConfiguration updated = client.patch(id, builder);

        Assert.assertEquals(newProductVersion.getVersion(), updated.getProductVersion().getVersion());

    }

    private ProductVersion createProductVersion() throws ClientException {
        ProductClient pClient = new ProductClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));
        Product product = pClient.getAll().iterator().next();

        ProductVersionClient pvClient = new ProductVersionClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));
        ProductVersion pv = ProductVersion.builder()
                .version("3245.6742")
                .product(ProductRef.refBuilder().id(product.getId()).build())
                .build();
        return pvClient.createNewProductVersion(pv);
    }
}
