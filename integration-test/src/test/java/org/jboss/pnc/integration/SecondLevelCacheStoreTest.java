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
package org.jboss.pnc.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.EnvironmentClient;
import org.jboss.pnc.client.ProductVersionClient;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class SecondLevelCacheStoreTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static String environmentId;
    private static String dependencyBCId;
    private static String buildConfigurationId;
    private static String projectBCId;
    private static String projectDepBCId;
    private static String repositoryConfigurationBCId;
    private static String repositoryConfigurationDepBCId;

    private ProjectClient projectClient = new ProjectClient(RestClientConfiguration.asUser());

    private ProductVersionClient productVersionClient = new ProductVersionClient(RestClientConfiguration.asUser());

    private BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(
            RestClientConfiguration.asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    private Project createProjectAndValidateResults(
            String projectUrl,
            String issueTrackerUrl,
            String name,
            String description) throws ClientException {

        Project project = Project.builder()
                .projectUrl(projectUrl)
                .issueTrackerUrl(issueTrackerUrl)
                .name(name)
                .description(description)
                .build();

        // when
        Project created = projectClient.createNew(project);

        // then
        assertThat(created.getId()).isNotNull().isNotEmpty();
        assertThat(created.getName()).isEqualTo(project.getName());
        assertThat(created.getProjectUrl()).isEqualTo(project.getProjectUrl());
        assertThat(created.getIssueTrackerUrl()).isEqualTo(project.getIssueTrackerUrl());

        return created;
    }

    public ProductVersion createProductVersionAndValidateResults(Product product, String version)
            throws ClientException {
        // given
        ProductVersion productVersion = ProductVersion.builder().product(product).version(version).build();

        // when
        ProductVersion created = productVersionClient.createNew(productVersion);

        // then
        assertThat(created.getId()).isNotEmpty();
        ProductVersion retrieved = productVersionClient.getSpecific(created.getId());

        ProductVersion toCompare = productVersion.toBuilder()
                .productMilestones(Collections.emptyMap()) // query had null, but server responds with empty map
                .productReleases(Collections.emptyMap()) // query had null, but server responds with empty map
                .groupConfigs(Collections.emptyMap()) // query had null, but server responds with empty map
                .buildConfigs(Collections.emptyMap()) // query had null, but server responds with empty map
                .build();

        assertThat(created.getProduct().getId()).isEqualTo(toCompare.getProduct().getId());
        assertThat(created).isEqualToIgnoringGivenFields(toCompare, "id", "product", "attributes");
        assertThat(retrieved).isEqualTo(created);

        return retrieved;
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
                .parameters(Collections.singletonMap("KEY1", genericParameterValue1))
                .buildType(BuildType.MVN)
                .build();

        BuildConfiguration newBC = buildConfigurationClient.createNew(buildConfiguration);
        return newBC;
    }

    /**
     * At first we need to create testing data and commit it.
     */
    @Test
    @InSequence(-2)
    public void prepareData() throws Exception {

        EnvironmentClient client = new EnvironmentClient(RestClientConfiguration.asAnonymous());
        environmentId = client.getAll().iterator().next().getId();

        SCMRepositoryClient scmrc = new SCMRepositoryClient(RestClientConfiguration.asAnonymous());
        final Iterator<SCMRepository> scmrIt = scmrc.getAll(null, null).iterator();
        repositoryConfigurationBCId = scmrIt.next().getId();
        repositoryConfigurationDepBCId = scmrIt.next().getId();

        Project projectBC = createProjectAndValidateResults(
                "https://github.com/project-ncl/dependency-analysis",
                null,
                "Dependency Analysis New",
                "Dependency Analysis - Analise project dependencies.");
        Project projectDepBC = createProjectAndValidateResults(
                "https://github.com/project-ncl/pnc",
                null,
                "Project Newcastle Demo Project 1 New",
                "Example Project for Newcastle Demo");

        projectBCId = projectBC.getId();
        projectDepBCId = projectDepBC.getId();

        BuildConfiguration dependencyBC = createBuildConfigurationAndValidateResults(
                projectDepBCId,
                environmentId,
                repositoryConfigurationDepBCId,
                "pnc-1.0.0.DR1-new",
                UUID.randomUUID().toString());

        BuildConfiguration buildConfiguration = createBuildConfigurationAndValidateResults(
                projectBCId,
                environmentId,
                repositoryConfigurationBCId,
                "dependency-analysis-master-new",
                UUID.randomUUID().toString());

        dependencyBCId = dependencyBC.getId();
        buildConfigurationId = buildConfiguration.getId();
    }

    /**
     * Secondly we need to verify it.
     */
    @Test
    @InSequence(-1)
    public void verifyPresenceOfRequiredData() throws Exception {
        BuildConfiguration savedBuildConfiguration = buildConfigurationClient.getSpecific(buildConfigurationId);
        assertThat(savedBuildConfiguration).isNotNull();

        BuildConfiguration savedDependencyBuildConfiguration = buildConfigurationClient.getSpecific(dependencyBCId);
        assertThat(savedDependencyBuildConfiguration).isNotNull();

        assertThat(savedBuildConfiguration.getDependencies().containsKey(dependencyBCId));
    }

    /**
     * Finally, we update it.
     */
    @Test
    @InSequence(1)
    public void bogusUpdateBC() throws Exception {

        BuildConfiguration originalBuildConfiguration = buildConfigurationClient.getSpecific(buildConfigurationId);
        BuildConfiguration.Builder bcBuilder = originalBuildConfiguration.toBuilder();
        bcBuilder.buildScript("mvn clean install");
        BuildConfiguration updatedBC = bcBuilder.build();

        buildConfigurationClient.update(updatedBC.getId(), updatedBC);
    }
}
