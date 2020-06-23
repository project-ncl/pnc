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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.client.patch.ProductVersionPatchBuilder;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductVersionEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductVersionEndpointTest.class);

    private static Product product;
    private static String productVersionsId;
    private static String productVersionsId2;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        ProductClient productClient = new ProductClient(RestClientConfiguration.asAnonymous());
        product = productClient.getAll().iterator().next();
        Iterator<ProductVersion> it = productClient.getProductVersions(product.getId()).iterator();
        productVersionsId = it.next().getId();
        productVersionsId2 = it.next().getId();
    }

    @Test
    public void testCreateNew() throws ClientException {
        // given
        ProductVersion productVersion = ProductVersion.builder().product(product).version("42.0").build();

        // when
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion created = client.createNew(productVersion);

        // then
        assertThat(created.getId()).isNotEmpty();
        ProductVersion retrieved = client.getSpecific(created.getId());

        ProductVersion toCompare = productVersion.toBuilder()
                .productMilestones(Collections.emptyMap()) // query had null, but server responds with empty map
                .productReleases(Collections.emptyMap()) // query had null, but server responds with empty map
                .groupConfigs(Collections.emptyMap()) // query had null, but server responds with empty map
                .buildConfigs(Collections.emptyMap()) // query had null, but server responds with empty map
                .build();

        assertThat(created.getProduct().getId()).isEqualTo(toCompare.getProduct().getId());
        assertThat(created).isEqualToIgnoringGivenFields(toCompare, "id", "product", "attributes");
        assertThat(retrieved).isEqualTo(created);
    }

    @Test
    public void testGetSpecific() throws ClientException {
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asAnonymous());

        ProductVersion dto = client.getSpecific(productVersionsId);

        assertThat(dto.getVersion()).isEqualTo("1.0"); // from DatabaseDataInitializer
        assertThat(dto.getProduct().getId()).isEqualTo(product.getId()); // from DatabaseDataInitializer
    }

    @Test
    public void testUpdate() throws ClientException {
        // given
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        final String version = "2.1";
        ProductVersion dto = client.getSpecific(productVersionsId2);
        ProductVersion toUpdate = dto.toBuilder().version(version).build();

        // when
        client.update(productVersionsId2, toUpdate);

        // then
        ProductVersion retrieved = client.getSpecific(dto.getId());

        assertThat(retrieved).isEqualTo(toUpdate);
        assertThat(retrieved).isEqualToIgnoringGivenFields(dto, "version");
        assertThat(retrieved.getVersion()).isEqualTo(version);
    }

    @Test
    public void testGetBuildConfigurations() throws ClientException {
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<BuildConfiguration> all = client.getBuildConfigs(productVersionsId);

        assertThat(all).hasSize(2).allMatch(v -> v.getProductVersion().getId().equals(productVersionsId));
    }

    @Test
    public void testGetGroupConfigurations() throws ClientException {
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<GroupConfiguration> all = client.getGroupConfigs(productVersionsId);

        assertThat(all).hasSize(2).allMatch(v -> v.getProductVersion().getId().equals(productVersionsId));
    }

    @Test
    public void testGetMilestones() throws ClientException {
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<ProductMilestone> all = client.getMilestones(productVersionsId);

        assertThat(all).hasSize(3).allMatch(v -> v.getProductVersion().getId().equals(productVersionsId));
    }

    @Test
    public void testGetReleases() throws ClientException {
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<ProductRelease> all = client.getReleases(productVersionsId);

        assertThat(all).hasSize(1).allMatch(v -> v.getProductVersion().getId().equals(productVersionsId));
    }

    @Test
    public void shouldFailGracefullyOnNonExistentProduct() {
        // given
        String nonExistentProductId = "384583";
        ProductVersion productVersion = ProductVersion.builder()
                .product(ProductRef.refBuilder().id(nonExistentProductId).build())
                .version("42.2")
                .build();

        // whenthen
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());

        assertThatThrownBy(() -> client.createNew(productVersion)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldGenerateBrewTagWhenCreatingProductVersion() throws Exception {
        // given
        final String version = "42.3";
        ProductVersion productVersion = ProductVersion.builder().product(product).version(version).build();

        // when
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion created = client.createNew(productVersion);

        // then
        assertThat(created.getAttributes()).containsKey(Attributes.BREW_TAG_PREFIX);
        assertThat(created.getAttributes().get(Attributes.BREW_TAG_PREFIX))
                .isEqualTo(product.getAbbreviation().toLowerCase() + "-" + version + "-pnc");
    }

    @Test
    public void shouldUpdateGroupConfigs() throws ClientException {
        // given
        GroupConfiguration gc = GroupConfiguration.builder().name("New GC").build();
        GroupConfigurationClient gcc = new GroupConfigurationClient(RestClientConfiguration.asUser());
        GroupConfiguration gcToAdd = gcc.createNew(gc);
        Map<String, GroupConfigurationRef> groupConfis = new HashMap<>();

        // when
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion productVersion = client.getSpecific(productVersionsId2);

        groupConfis.putAll(productVersion.getGroupConfigs());
        groupConfis.put(gcToAdd.getId(), gcToAdd);

        ProductVersion toUpdate = productVersion.toBuilder().groupConfigs(groupConfis).build();
        client.update(productVersion.getId(), toUpdate);
        ProductVersion retrieved = client.getSpecific(productVersion.getId());

        // then
        assertThat(retrieved.getGroupConfigs()).hasSameSizeAs(groupConfis).containsKey(gcToAdd.getId());
    }

    @Test
    public void shouldUpdateGroupConfigsUsingPatch() throws PatchBuilderException, RemoteResourceException {
        // given
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        GroupConfiguration gc = GroupConfiguration.builder().name("GC patch test").build();
        GroupConfigurationClient gcc = new GroupConfigurationClient(RestClientConfiguration.asUser());
        GroupConfiguration gcToAdd = gcc.createNew(gc);
        ProductVersion productVersion = client.getSpecific(productVersionsId2);
        Map<String, GroupConfigurationRef> groupConfigs = productVersion.getGroupConfigs();

        // when
        ProductVersionPatchBuilder patchBuilder = new ProductVersionPatchBuilder();
        ProductVersionPatchBuilder patch = patchBuilder
                .addGroupConfigs(Collections.singletonMap(gcToAdd.getId(), gcToAdd));
        client.patch(productVersionsId2, patch);

        // then
        groupConfigs.put(gcToAdd.getId(), gcToAdd);
        ProductVersion productVersionUpdated = client.getSpecific(productVersionsId2);
        assertThat(productVersionUpdated.getGroupConfigs())
                .containsKeys(groupConfigs.keySet().toArray(new String[groupConfigs.keySet().size()]));
    }

    @Test
    public void shouldNotUpdateGroupConfigsWhenOneIsAlreadyAsssociatedWithAnotherProductVersion()
            throws ClientException {
        // given
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        GroupConfigurationRef alreadyAssignedGC = client.getSpecific(productVersionsId)
                .getGroupConfigs()
                .values()
                .iterator()
                .next();
        Map<String, GroupConfigurationRef> groupConfis = new HashMap<>();
        assertThat(alreadyAssignedGC).isNotNull();

        // when
        ProductVersion productVersion = client.getSpecific(productVersionsId2);

        groupConfis.putAll(productVersion.getGroupConfigs());
        groupConfis.put(alreadyAssignedGC.getId(), alreadyAssignedGC);

        ProductVersion toUpdate = productVersion.toBuilder().groupConfigs(groupConfis).build();

        // then
        assertThatThrownBy(() -> client.update(productVersion.getId(), toUpdate)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldNotUpdateGroupConfigsWhenOneIsAlreadyAsssociatedWithAnotherProductVersionUsingPatch()
            throws ClientException, PatchBuilderException {
        // given
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        GroupConfigurationRef alreadyAssignedGC = client.getSpecific(productVersionsId)
                .getGroupConfigs()
                .values()
                .iterator()
                .next();
        Map<String, GroupConfigurationRef> groupConfis = new HashMap<>();
        assertThat(alreadyAssignedGC).isNotNull();

        // when
        ProductVersionPatchBuilder patchBuilder = new ProductVersionPatchBuilder();
        ProductVersionPatchBuilder patch = patchBuilder
                .addGroupConfigs(Collections.singletonMap(alreadyAssignedGC.getId(), alreadyAssignedGC));

        // then
        assertThatThrownBy(() -> client.patch(productVersionsId2, patch)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldNotUpdateGroupConfigsWithNonExistantGroupConfig() throws ClientException {
        // given
        GroupConfigurationRef notExistingGC = GroupConfigurationRef.refBuilder()
                .id("9999")
                .name("i-dont-exist")
                .build();
        Map<String, GroupConfigurationRef> groupConfis = new HashMap<>();

        // when
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion productVersion = client.getSpecific(productVersionsId2);

        groupConfis.putAll(productVersion.getGroupConfigs());
        groupConfis.put(notExistingGC.getId(), notExistingGC);

        ProductVersion toUpdate = productVersion.toBuilder().groupConfigs(groupConfis).build();

        // then
        assertThatThrownBy(() -> client.update(productVersion.getId(), toUpdate)).isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldNotUpdateWithClosedMilestone() throws ClientException {
        // given
        ProductVersionClient client = new ProductVersionClient(RestClientConfiguration.asUser());
        ProductVersion productVersion = client.getSpecific(productVersionsId); // has closed milestone, from
                                                                               // DatabaseDataInitializer

        // when
        ProductVersion toUpdate = productVersion.toBuilder().version("2.0").build();

        // then
        assertThatThrownBy(() -> client.update(productVersion.getId(), toUpdate)).isInstanceOf(ClientException.class);
    }
}
