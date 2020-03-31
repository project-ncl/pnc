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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.EnvironmentClient;
import org.jboss.pnc.client.ProductClient;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.client.patch.BuildConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.AlignmentParameters;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildConfigurationEndpointTest.class);

    private static final String PARAMETER_KEY = "KEY1";

    private static final String PME_PARAMS_LONG = "dependencyManagement=org.jboss.eap:jboss-eap-parent:${EAPBOM:version},"
            + "dependencyRelocations.org.wildfly:@org.jboss.eap:=${EAPBOM:version},"
            + "dependencyExclusion.org.freemarker:freemarker@*=${freemarker-2.3.23:version},"
            + "dependencyExclusion.org.liquibase:liquibase-core@*=${liquibase-3.4.1:version},"
            + "dependencyExclusion.org.twitter4j:twitter4j-core@*=${twitter4j-4.0.4:version},"
            + "dependencyExclusion.com.google.zxing:core@*=${zxing-3.2.1:version},"
            + "dependencyExclusion.org.infinispan:infinispan-core@*=8.1.4.Final-redhat-1,"
            + "dependencyExclusion.io.undertow:undertow-core@*=1.3.24.Final-redhat-1,"
            + "dependencyExclusion.org.wildfly.core:wildfly-version@*=${WFCORE:version},"
            + "dependencyExclusion.org.jboss.as:jboss-as-server@*=7.5.11.Final-redhat-1,"
            + "dependencyExclusion.org.hibernate:hibernate-entitymanager@*=5.0.9.Final-redhat-1,"
            + "dependencyExclusion.org.jboss.logging:jboss-logging-annotations@*=2.0.1.Final-redhat-1,"
            + "dependencyExclusion.org.jboss.resteasy:resteasy-jaxrs@*=3.0.18.Final-redhat-1,"
            + "dependencyExclusion.org.osgi:org.osgi.core@*=5.0.0,"
            + "dependencyExclusion.org.jboss.spec.javax.servlet:jboss-servlet-api_3.0_spec@*=1.0.2.Final-redhat-2,"
            + "dependencyExclusion.org.drools:drools-bom@*=6.4.0.Final-redhat-10,"
            + "dependencyExclusion.org.jboss.integration-platform:jboss-integration-platform-bom@*=6.0.6.Final-redhat-3";

    private static String configurationId;
    private static String configuration3Id;
    private static String configuration4Id;
    private static String productId;
    private static String projectId;
    private static String environmentId;
    private static String repositoryConfigurationId;
    private static String repositoryConfiguration2Id;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void prepareData() throws Exception {
        BuildConfigurationClient bcc = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());
        Iterator<BuildConfiguration> it = bcc.getAll().iterator();
        configurationId = it.next().getId();
        it.next();
        configuration3Id = it.next().getId();
        configuration4Id = it.next().getId();

        ProductClient pdc = new ProductClient(RestClientConfiguration.asAnonymous());
        productId = pdc.getAll().iterator().next().getId();

        EnvironmentClient ec = new EnvironmentClient(RestClientConfiguration.asAnonymous());
        environmentId = ec.getAll().iterator().next().getId();

        ProjectClient pjc = new ProjectClient(RestClientConfiguration.asAnonymous());
        projectId = pjc.getAll().iterator().next().getId();

        SCMRepositoryClient scmrc = new SCMRepositoryClient(RestClientConfiguration.asAnonymous());
        final Iterator<SCMRepository> scmrIt = scmrc.getAll(null, null).iterator();
        repositoryConfigurationId = scmrIt.next().getId();
        repositoryConfiguration2Id = scmrIt.next().getId();

    }

    @Test
    @InSequence(10)
    public void testGetAll() throws RemoteResourceException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<BuildConfiguration> all = client.getAll();

        assertThat(all).hasSize(5); // from DatabaseDataInitializer
    }

    @Test
    @InSequence(20)
    public void shouldCreateNewBuildConfiguration() throws ClientException {
        BuildConfiguration bc = createBuildConfigurationAndValidateResults(
                projectId,
                environmentId,
                repositoryConfigurationId,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());

        assertThat(bc.getCreationTime()).isNotNull();
        assertThat(bc.getModificationTime()).isNotNull();
    }

    @Test
    @InSequence(20)
    public void shouldCreateBuildConfigurationWithLongGenericParameter() throws Exception {
        createBuildConfigurationAndValidateResults(
                projectId,
                environmentId,
                repositoryConfigurationId,
                UUID.randomUUID().toString(),
                PME_PARAMS_LONG);
    }

    private BuildConfiguration createBuildConfigurationAndValidateResults(
            String projectId,
            String environmentId,
            String repositoryConfigurationId,
            String name,
            String genericParameterValue1) throws ClientException {
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .project(ProjectRef.refBuilder().id(projectId).build())
                .environment(Environment.builder().id(environmentId).build())
                .scmRepository(SCMRepository.builder().id(repositoryConfigurationId).build())
                .name(name)
                .parameters(Collections.singletonMap(PARAMETER_KEY, genericParameterValue1))
                .buildType(BuildType.MVN)
                .build();

        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());

        BuildConfiguration newBC = client.createNew(buildConfiguration);

        return newBC;
    }

    @Test
    @InSequence(10)
    public void testGetSpecific() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        BuildConfiguration dto = client.getSpecific(configurationId);

        assertThat(dto.getScmRevision()).isEqualTo("*/v0.2"); // from DatabaseDataInitializer
        assertThat(dto.getDescription()).isEqualTo("Test build config for project newcastle"); // from
                                                                                               // DatabaseDataInitializer
    }

    /**
     * Reproducer NCL-2615 - big generic parameters cannot be ubdated in the BuildConfiguration
     *
     * @throws ClientException
     */
    @Test
    @InSequence(30)
    public void shouldUpdateBuildConfiguration() throws ClientException {
        // given
        final String updatedBuildScript = "mvn clean deploy -Dmaven.test.skip=true";
        final String updatedName = UUID.randomUUID().toString();
        final String updatedProjectId = String.valueOf(projectId);
        final String updatedGenParamValue = PME_PARAMS_LONG;

        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(configurationId)
                .name(updatedName)
                .buildScript(updatedBuildScript)
                .creationTime(Instant.ofEpochMilli(1518382545038L))
                .modificationTime(Instant.ofEpochMilli(155382545038L))
                .project(ProjectRef.refBuilder().id(updatedProjectId).build())
                .environment(Environment.builder().id(environmentId).build())
                .parameters(Collections.singletonMap(PARAMETER_KEY, updatedGenParamValue))
                .scmRepository(SCMRepository.builder().id(repositoryConfigurationId).build())
                .buildType(BuildType.MVN)
                .build();

        // when
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        client.update(configurationId, buildConfiguration);
        BuildConfiguration updatedBC = client.getSpecific(configurationId);

        // then
        assertThat(updatedBC.getId()).isEqualTo(configurationId);
        assertThat(updatedBC.getName()).isEqualTo(updatedName);
        assertThat(updatedBC.getBuildScript()).isEqualTo(updatedBuildScript);
        assertThat(updatedBC.getScmRepository().getId()).isEqualTo(repositoryConfigurationId);
        assertThat(updatedBC.getProject().getId()).isEqualTo(updatedProjectId);
        assertThat(updatedBC.getParameters().get(PARAMETER_KEY)).isEqualTo(updatedGenParamValue);
        assertThat(updatedBC.getEnvironment().getId()).isEqualTo(environmentId);
    }

    @Test
    @InSequence(20)
    public void shouldPatchBuildConfiguration() throws ClientException, PatchBuilderException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());

        BuildConfiguration buildConfiguration = client.getAll().iterator().next();
        String newDescription = "Testing patch support.";
        Instant modTime = buildConfiguration.getModificationTime();

        String id = buildConfiguration.getId();

        Map<String, String> addElements = Collections.singletonMap("newKey", "newValue");
        BuildConfigurationPatchBuilder builder = new BuildConfigurationPatchBuilder().replaceDescription(newDescription)
                .addParameters(addElements);
        BuildConfiguration updated = client.patch(id, builder);

        Assert.assertEquals(newDescription, updated.getDescription());
        Assert.assertEquals(modTime, updated.getModificationTime());
        Assertions.assertThat(updated.getParameters()).contains(addElements.entrySet().toArray(new Map.Entry[1]));

        String newDescription2 = "Testing patch support 2.";
        BuildConfigurationPatchBuilder builder2 = new BuildConfigurationPatchBuilder()
                .replaceDescription(newDescription2);
        BuildConfiguration updated2 = client.patch(id, builder2.getJsonPatch(), BuildConfiguration.class);
        Assert.assertEquals(newDescription2, updated2.getDescription());
    }

    @Test
    public void testGetBuilds() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Build> all = client.getBuilds(configurationId, null);

        assertThat(all).hasSize(2).allMatch(b -> configurationId.equals(b.getBuildConfigRevision().getId()));
    }

    @Test
    @InSequence(20)
    public void shouldCloneBuildConfiguration() throws ClientException {
        // given
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        BuildConfiguration original = client.getSpecific(configurationId);
        assertThat(original.getProductVersion()).isNotNull();
        assertThat(original.getGroupConfigs()).isNotEmpty();
        BuildConfiguration parent = client.getSpecific(configuration4Id);
        assertThat(parent.getDependencies()).containsKey(configurationId);

        // when
        BuildConfiguration clone = client.clone(original.getId());

        // then
        assertThat(clone.getId()).isNotEmpty();
        assertThat(clone.getProductVersion()).isNull();
        assertThat(clone.getGroupConfigs()).isEmpty();
        BuildConfiguration retrieved = client.getSpecific(clone.getId());
        BuildConfiguration retrivedParent = client.getSpecific(configuration4Id);
        assertThat(retrivedParent.getDependencies()).containsKey(configurationId);
        assertThat(retrivedParent.getDependencies()).doesNotContainKey(clone.getId());
        assertThat(clone).isEqualToIgnoringGivenFields(
                original,
                "id",
                "name",
                "groupConfigs",
                "creationTime",
                "modificationTime",
                "modificationTime",
                "productVersion");
        assertThat(retrieved).isEqualToIgnoringGivenFields(clone, "modificationTime"); // close of transaction changes
                                                                                       // the modification time -
                                                                                       // WONTFIX
    }

    @Test
    public void testGetGroupConfig() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<GroupConfiguration> all = client.getGroupConfigs(configurationId);

        assertThat(all).hasSize(1).allMatch(gc -> gc.getBuildConfigs().containsKey(configurationId));
    }

    @Test
    @InSequence(30)
    public void testGetDependencies() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<BuildConfiguration> all = client.getDependencies(configuration3Id);

        assertThat(all).hasSize(2);
    }

    @Test
    @InSequence(20)
    public void testAddDependency() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        // given
        BuildConfiguration newDependency = createBuildConfigurationAndValidateResults(
                projectId,
                environmentId,
                repositoryConfigurationId,
                "dep-" + UUID.randomUUID(),
                PARAMETER_KEY);
        BuildConfiguration parent = client.getSpecific(configuration3Id);
        Map<String, BuildConfigurationRef> oldDependencies = parent.getDependencies();
        assertThat(oldDependencies).doesNotContainKey(newDependency.getId());

        // when
        client.addDependency(parent.getId(), newDependency);

        // then
        RemoteCollection<BuildConfiguration> all = client.getDependencies(parent.getId());
        assertThat(all).extracting(DTOEntity::getId)
                .containsAll(oldDependencies.keySet().stream().collect(Collectors.toList()))
                .contains(newDependency.getId());
    }

    @Test
    @InSequence(40)
    public void testRemoveDependency() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        // given
        BuildConfiguration parent = client.getSpecific(configuration3Id);
        Map<String, BuildConfigurationRef> oldDependencies = parent.getDependencies();
        assertThat(oldDependencies).isNotEmpty();
        BuildConfigurationRef toDelete = oldDependencies.values().iterator().next();

        // when
        client.removeDependency(parent.getId(), toDelete.getId());

        // then
        RemoteCollection<BuildConfiguration> all = client.getDependencies(parent.getId());
        oldDependencies.remove(toDelete.getId());
        assertThat(all).extracting(DTOEntity::getId)
                .doesNotContain(toDelete.getId())
                .containsAll(oldDependencies.keySet().stream().collect(Collectors.toList()));
    }

    @Test
    public void shouldGetBuildConfigurationRevisions() throws Exception {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<BuildConfigurationRevision> revisions = client.getRevisions(configurationId);

        assertThat(revisions).anySatisfy(config -> assertThat(config.getId()).isEqualTo(configurationId));
    }

    @Test
    public void shouldCreateBuildConfigRevision() throws ClientException {
        final String description = "Updated description.";
        final String name = "Updated name.";
        final String buildScript = "mvn deploy # Updated script";

        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        BuildConfiguration bc = client.getSpecific(configurationId);

        Instant modTime = bc.getModificationTime();
        BuildConfiguration newBC1 = bc.toBuilder().name(name).description(description).build();
        BuildConfiguration newBC2 = bc.toBuilder().buildScript(buildScript).build();

        BuildConfigurationRevision newRevision1 = client.createRevision(configurationId, newBC1);
        BuildConfigurationRevision newRevision2 = client.createRevision(configurationId, newBC2);

        assertNotEquals(modTime, newRevision1.getModificationTime());
        assertNotEquals(modTime, newRevision2.getModificationTime());
        assertEquals(name, newRevision1.getName());
        assertEquals(bc.getBuildScript(), newRevision1.getBuildScript());
        assertEquals(bc.getEnvironment(), newRevision2.getEnvironment());
        assertEquals(buildScript, newRevision2.getBuildScript());
        assertThat(newRevision1.getRev()).isLessThan(newRevision2.getRev());
    }

    @Test
    public void shouldGetBuildConfigurationRevision() throws Exception {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        BuildConfigurationRevision revision = client.getRevision(configurationId, 1);

        assertThat(revision.getId()).isEqualTo(configurationId);
    }

    @Test
    public void shouldRestoreBuildConfigurationRevision() throws Exception {
        // given
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        BuildConfiguration original = client.getSpecific(configurationId);

        Iterator<BuildConfigurationRevision> it = client.getRevisions(configurationId).iterator();

        // given latest revision
        BuildConfigurationRevision originalRev = it.next();
        while (it.hasNext()) {
            BuildConfigurationRevision candidate = it.next();
            if (candidate.getRev() > originalRev.getRev()) {
                originalRev = candidate;
            }
        }

        // when
        BuildConfiguration toUpdate = original.toBuilder()
                .description("shouldRestoreBuildConfigurationRevision Updated")
                .build();
        client.update(configurationId, toUpdate);
        BuildConfiguration updated = client.getSpecific(configurationId);
        assertThat(updated.getDescription()).isNotEqualTo(original.getDescription());

        // and when
        BuildConfiguration restored = client.restoreRevision(configurationId, originalRev.getRev());
        BuildConfiguration retrieved = client.getSpecific(configurationId);

        // then
        assertThat(restored.getDescription()).isNotEqualTo(updated.getDescription());
        assertThat(restored).isEqualToIgnoringGivenFields(original, "modificationTime");
        assertThat(retrieved).isEqualToIgnoringGivenFields(restored, "modificationTime");
    }

    @Test
    public void testGetSupportedParameters() throws ClientException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());
        Set<Parameter> all = client.getSupportedParameters();

        assertThat(all).haveExactly(
                1,
                new Condition<>(
                        p -> p.getName().equals("ALIGNMENT_PARAMETERS")
                                && p.getDescription().startsWith("Additional parameters, which will be "),
                        "has PME parameter"))
                .size()
                .isGreaterThanOrEqualTo(4);
    }

    @Test
    public void testGetBuildTypeDefaultAlignmentParameters() throws RemoteResourceException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());

        for (BuildType buildType : BuildType.values()) {
            AlignmentParameters params = client.getBuildTypeDefaultAlignmentParameters(buildType.name());
            assertThat(params.getParameters()).isNotEmpty();
            assertThat(params.getBuildType()).isNotEmpty();
        }
    }
}
