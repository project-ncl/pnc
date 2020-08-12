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
package org.jboss.pnc.integration.endpoints;

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
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void shouldPatchGroupConfiguration() throws ClientException, PatchBuilderException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());

        GroupConfiguration groupConfiguration = client.getAll().iterator().next();
        String id = groupConfiguration.getId();

        ProductVersion newProductVersion = createProductVersion();
        ProductVersion pv = ProductVersion.builder().id(newProductVersion.getId()).build();

        GroupConfigurationPatchBuilder builder = new GroupConfigurationPatchBuilder().replaceProductVersion(pv);
        GroupConfiguration updated = client.patch(id, builder);

        assertThat(updated.getProductVersion().getVersion()).isEqualTo(newProductVersion.getVersion());

    }

    private ProductVersion createProductVersion() throws ClientException {
        ProductClient pClient = new ProductClient(RestClientConfiguration.asUser());
        Product product = pClient.getAll().iterator().next();

        ProductVersionClient pvClient = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion pv = ProductVersion.builder()
                .version("3245.6742")
                .product(ProductRef.refBuilder().id(product.getId()).build())
                .build();
        return pvClient.createNew(pv);
    }

    @Test
    public void testCreateNewGroupConfig() throws RemoteResourceException, ClientException {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());

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
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());

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
    public void shouldUpdateAllBuildConfigurations() throws Exception {
        // with
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        BuildConfigurationClient bcClient = new BuildConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "101";
        BuildConfiguration bc = bcClient.getAll().iterator().next();
        GroupConfiguration gc = client.getSpecific(gcId);
        String bcId = bc.getId();
        Map<String, BuildConfigurationRef> buildConfigurationMap = new HashMap<>();
        buildConfigurationMap.put(bcId, bc);
        GroupConfiguration updated = gc.toBuilder().buildConfigs(buildConfigurationMap).build();
        assertThat(gc.getBuildConfigs()).doesNotContainKey(bcId);

        // when
        client.update(gc.getId(), updated);

        // then
        assertThat(client.getSpecific(gcId).getBuildConfigs()).containsOnlyKeys(bcId);
    }

    @Test
    public void shouldUpdateAllBuildConfigurationsWithEmptyList() throws Exception {
        GroupConfigurationClient client = new GroupConfigurationClient(RestClientConfiguration.asUser());
        String gcId = "101";
        GroupConfiguration gc = client.getSpecific(gcId);
        GroupConfiguration updated = gc.toBuilder().buildConfigs(new HashMap<>()).build();
        assertThat(gc.getBuildConfigs()).isNotEmpty();

        // when
        client.update(gc.getId(), updated);

        // then
        assertThat(client.getSpecific(gcId).getBuildConfigs()).isEmpty();
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
        client.addBuildConfig(gcId, buildConfiguration);

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
                configurations = client.getBuildConfigs(gcId);
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
                configurations = client.getBuildConfigs(gcId);
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
        BuildConfiguration configuration = client.getBuildConfigs(gcId).iterator().next();

        // when
        client.removeBuildConfig(gcId, configuration.getId());

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

        assertThat(client.getBuildConfigs(gcId)).hasSameSizeAs(bcIds).allSatisfy(bc -> bcIds.contains(bc.getId()));
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

    @Test
    public void shouldDeleteBuildConfigWithPatch() throws Exception {
        // given
        GroupConfigurationClient groupConfigurationClient = new GroupConfigurationClient(
                RestClientConfiguration.asUser());
        BuildConfigurationClient bcClient = new BuildConfigurationClient(RestClientConfiguration.asUser());
        GroupConfiguration gc = groupConfigurationClient.getAll().iterator().next();

        assertThat(gc.getBuildConfigs()).isNotEmpty();

        BuildConfiguration toRemove = bcClient.getSpecific(gc.getBuildConfigs().keySet().iterator().next());

        GroupConfigurationPatchBuilder builder = new GroupConfigurationPatchBuilder();
        builder.removeBuildConfigs(Collections.singletonList(toRemove.getId()));

        // when
        groupConfigurationClient.patch(gc.getId(), builder);

        // then
        GroupConfiguration refresh = groupConfigurationClient.getSpecific(gc.getId());

        assertThat(refresh.getBuildConfigs().keySet()).doesNotContain(toRemove.getId());
    }

}
