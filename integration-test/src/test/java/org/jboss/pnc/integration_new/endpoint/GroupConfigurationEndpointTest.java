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
package org.jboss.pnc.integration_new.endpoint;

import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.patch.GroupConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

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

    @Ignore // TODO ENABLE ME
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

        assertThat(updated.getProductVersion().getVersion()).isEqualTo(newProductVersion.getVersion());

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
        return pvClient.createNew(pv);
    }

    @Test
    public void testCreateNewGroupConfig() throws RemoteResourceException, ClientException {
        GroupConfigurationClient client = new GroupConfigurationClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));

        final String name = "Testing 101";

        GroupConfiguration gc = GroupConfiguration.builder()
                .productVersion(ProductVersionRef.refBuilder().id("101").build())
                .name(name)
                .build();

        GroupConfiguration created = client.createNew(gc);
        assertEquals(name, created.getName());
        GroupConfiguration specific = client.getSpecific(created.getId());
        assertEquals(created, specific);
    }

    @Test
    public void testUpdateGroupConfig() throws RemoteResourceException, ClientException {
        GroupConfigurationClient client = new GroupConfigurationClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));

        final String name = "Testing 100 Updated";

        GroupConfiguration specific = client.getSpecific("100");
        GroupConfiguration updating = specific.toBuilder().name(name).build();
        client.update("100", updating);
        GroupConfiguration updated = client.getSpecific("100");
        assertEquals(name, updated.getName());
    }

    @Test
    public void testGetSpecificGroupConfig() throws ClientException {
        String gcid = "100";
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());

        assertThat(client.getSpecific(gcid)).isNotNull();
    }

    @Test
    public void testGetGroupConfigs() throws RemoteResourceException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());

        assertThat(client.getAll()).isNotEmpty();
    }

    @Test
    public void testAddBuildConfig() throws ClientException {
        // with
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        BuildConfigurationClient bcClient = new BuildConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "100";
        BuildConfiguration buildConfiguration = bcClient.getAll().iterator().next();
        String bcToAddId = buildConfiguration.getId();
        GroupConfiguration groupConfiguration = client.getSpecific(gcId);
        assertThat(groupConfiguration.getBuildConfigs()).doesNotContainKey(bcToAddId);

        // when
        client.addConfiguration(gcId, buildConfiguration);

        // then
        assertThat(client.getSpecific(gcId).getBuildConfigs()).containsKey(bcToAddId);
    }

    /**
     * reproducer for NCL-3552
     */
    @Test
    public void testConcurrentGet() throws RemoteResourceException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        Map<Integer, RemoteCollection<BuildConfiguration>> responseMap = new HashMap<>();
        String gcId = "100";

        ExecutorService executorService = MDCExecutors.newFixedThreadPool(2);
        executorService.execute(() -> {
            logger.info("Making 1st request ...");
            RemoteCollection<BuildConfiguration> configurations = null;
            try {
                configurations = client.getConfigurations(gcId);
            } catch (RemoteResourceException e) {
                // detected with null in responseMap
            }
            logger.info("1st done.");
            responseMap.put(1, configurations);
        });

        executorService.execute(() -> {
            logger.info("Making 2nd request ...");
            RemoteCollection<BuildConfiguration> configurations = null;
            try {
                configurations = client.getConfigurations(gcId);
            } catch (RemoteResourceException e) {
                // detected with null in responseMap
            }
            logger.info("2nd done.");
            responseMap.put(2, configurations);
        });

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new AssertionError("Requests were not completed in given timeout.", e);
        }

        assertThat(responseMap).containsKeys(1, 2).doesNotContainValue(null);
    }

    @Test
    public void testRemoveBuildConfigFromGroupConfig() throws ClientException {
        // with
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "100";
        BuildConfiguration configuration = client.getConfigurations(gcId).iterator().next();

        // when
        client.removeConfiguration(gcId, configuration.getId());

        // then
        GroupConfiguration refreshed = client.getSpecific(gcId);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getBuildConfigs()).doesNotContainKey(configuration.getId());
    }

    @Test
    public void getBuildConfigs() throws ClientException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "100";
        Set<String> bcIds = client.getSpecific(gcId).getBuildConfigs().keySet();

        assertThat(client.getConfigurations(gcId)).hasSameSizeAs(bcIds).allSatisfy(bc -> bcIds.contains(bc.getId()));
    }

    @Test
    public void testCreatingExistingConflicts() throws ClientException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "100";
        GroupConfiguration existing = client.getSpecific(gcId).toBuilder().id(null).build();

        assertThatThrownBy(() -> client.createNew(existing)).hasCauseInstanceOf(ClientErrorException.class)
                .has(
                        new Condition<Throwable>(
                                (e -> ((ClientErrorException) e.getCause()).getResponse().getStatus() == 409),
                                "Has Cause with conflicted status code 409"));
    }

}
