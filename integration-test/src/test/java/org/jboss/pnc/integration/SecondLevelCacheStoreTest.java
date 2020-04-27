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
package org.jboss.pnc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.ConflictedEntryValidator;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.COMMIT)
@Category(ContainerTest.class)
public class SecondLevelCacheStoreTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    BuildEnvironmentRepository environmentRepository;

    @Inject
    RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    ProductMilestoneRepository productMilestoneRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    private static int dependencyBCId;
    private static int buildConfigurationId;
    private static int buildEnvironmentBCId;
    private static int projectBCId;
    private static int repositoryConfigurationBCId;
    private static int productVersionBCId;
    private static Date creationModificationTime = new Date();

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        restWar.addClass(SecondLevelCacheStoreTest.class);

        JavaArchive pncModel = enterpriseArchive.getAsType(JavaArchive.class, "/model.jar");
        pncModel.addPackage(BuildConfiguration.class.getPackage());

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    /**
     * At first we need to create testing data and commit it.
     */
    @Test
    @InSequence(-2)
    public void prepareData() throws Exception {

        BuildEnvironment buildEnvironmentBC = BuildEnvironment.Builder.newBuilder()
                .name("OpenJDK 1.8.0; Mvn 3.5.2 New")
                .description("OpenJDK 1.8.0; Mvn 3.5.2")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .systemImageRepositoryUrl("docker-registry-default.cloud.registry.upshift.redhat.com")
                .systemImageId("newcastle/builder-rhel-7-j8-mvn3.5.2:latest")
                .attribute("MAVEN", "3.5.2")
                .attribute("JDK", "1.8.0")
                .attribute("OS", "Linux")
                .deprecated(false)
                .build();

        buildEnvironmentBC = environmentRepository.save(buildEnvironmentBC);

        RepositoryConfiguration repositoryConfigurationBC = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl("git+ssh://code.stage.engineering.redhat.com/project-ncl/dependency-analysis-new.git")
                .externalUrl("https://github.com/project-ncl/dependency-analysis-new.git")
                .preBuildSyncEnabled(false)
                .build();
        RepositoryConfiguration repositoryConfigurationDepBC = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl("git+ssh://code.stage.engineering.redhat.com/project-ncl/pnc-new.git")
                .externalUrl(null)
                .preBuildSyncEnabled(true)
                .build();

        repositoryConfigurationBC = repositoryConfigurationRepository.save(repositoryConfigurationBC);
        repositoryConfigurationDepBC = repositoryConfigurationRepository.save(repositoryConfigurationDepBC);

        Project projectBC = Project.Builder.newBuilder()
                .name("Dependency Analysis New")
                .description("Dependency Analysis - Analise project dependencies.")
                .issueTrackerUrl(null)
                .projectUrl("https://github.com/project-ncl/dependency-analysis")
                .build();
        Project projectDepBC = Project.Builder.newBuilder()
                .name("Project Newcastle Demo Project 1 New")
                .description("Example Project for Newcastle Demo")
                .issueTrackerUrl(null)
                .projectUrl("https://github.com/project-ncl/pnc")
                .build();

        projectBC = projectRepository.save(projectBC);
        projectDepBC = projectRepository.save(projectDepBC);

        Product product = Product.Builder.newBuilder()
                .name("Project Newcastle Demo Product New")
                .description("Example Product for Project Newcastle Demo New")
                .abbreviation("PNCNew")
                .build();

        product = productRepository.save(product);

        ProductVersion productVersionBC = ProductVersion.Builder.newBuilder()
                .version("13.0")
                .product(product)
                .generateBrewTagPrefix(product.getAbbreviation(), "1.0", "${product_short_name}-${product_version}-pnc")
                .build();
        productVersionBC = productVersionRepository.save(productVersionBC);

        ProductMilestone currentProductMilestone = ProductMilestone.Builder.newBuilder()
                .version("13.0.0.Build1New")
                .startingDate(new Date())
                .endDate(new Date())
                .plannedEndDate(new Date())
                .productVersion(productVersionBC)
                .build();
        ProductMilestone futureProductMilestone = ProductMilestone.Builder.newBuilder()
                .version("13.0.0.Build2New")
                .startingDate(new Date())
                .endDate(new Date())
                .plannedEndDate(new Date())
                .productVersion(productVersionBC)
                .build();

        currentProductMilestone = productMilestoneRepository.save(currentProductMilestone);
        futureProductMilestone = productMilestoneRepository.save(futureProductMilestone);

        productVersionBC.setCurrentProductMilestone(currentProductMilestone);
        productVersionBC = productVersionRepository.save(productVersionBC);

        BuildConfiguration dependencyBC = BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(buildEnvironmentBC)
                .project(projectDepBC)
                .repositoryConfiguration(repositoryConfigurationDepBC)
                .name("pnc-1.0.0.DR1-new")
                .description("Test build config for project newcastle")
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRevision("*/v0.2")
                .buildType(BuildType.MVN)
                .productVersion(productVersionBC)
                .build();

        dependencyBC = buildConfigurationRepository.save(dependencyBC);

        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(buildEnvironmentBC)
                .project(projectBC)
                .repositoryConfiguration(repositoryConfigurationBC)
                .name("dependency-analysis-master-new")
                .description("Test config for Dependency Analysis.")
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRevision("master")
                .buildType(BuildType.MVN)
                .dependency(dependencyBC)
                .creationTime(creationModificationTime)
                .lastModificationTime(creationModificationTime)
                .build();

        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);

        dependencyBCId = dependencyBC.getId();
        buildConfigurationId = buildConfiguration.getId();
        buildEnvironmentBCId = buildEnvironmentBC.getId();
        projectBCId = projectBC.getId();
        repositoryConfigurationBCId = repositoryConfigurationBC.getId();
        productVersionBCId = productVersionBC.getId();
    }

    /**
     * Secondly we need to verify it.
     */
    @Test
    @InSequence(-1)
    public void verifyPresenceOfRequiredData() throws Exception {
        BuildConfiguration savedBuildConfiguration = buildConfigurationRepository.queryById(buildConfigurationId);
        assertThat(savedBuildConfiguration).isNotNull();

        BuildConfiguration savedDependencyBuildConfiguration = buildConfigurationRepository.queryById(dependencyBCId);
        assertThat(savedDependencyBuildConfiguration).isNotNull();

        BuildConfigurationRest buildConfigurationRest = new BuildConfigurationRest(savedBuildConfiguration);
        assertThat(buildConfigurationRest.getDependencyIds()).contains(dependencyBCId);
    }

    /**
     * Finally, we update it.
     */
    @Test
    @InSequence(1)
    public void bogusUpdateBC() throws Exception {

        // Recreate all the REST objects as they were coming from the endpoint

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("MAVEN", "3.5.2");
        attributes.put("JDK", "1.8.0");
        attributes.put("OS", "Linux");
        BuildEnvironmentRest buildEnvironmentRest = new BuildEnvironmentRest();
        buildEnvironmentRest.setId(buildEnvironmentBCId);
        buildEnvironmentRest.setName("OpenJDK 1.8.0; Mvn 3.5.2 New");
        buildEnvironmentRest.setDescription("OpenJDK 1.8.0; Mvn 3.5.2");
        buildEnvironmentRest.setSystemImageType(SystemImageType.DOCKER_IMAGE);
        buildEnvironmentRest.setSystemImageRepositoryUrl("docker-registry-default.cloud.registry.upshift.redhat.com");
        buildEnvironmentRest.setSystemImageId("newcastle/builder-rhel-7-j8-mvn3.5.2:latest");
        buildEnvironmentRest.setDeprecated(false);
        buildEnvironmentRest.setAttributes(attributes);

        ProjectRest projectRest = new ProjectRest();
        projectRest.setId(projectBCId);
        projectRest.setName("Dependency Analysis New");
        projectRest.setDescription("Dependency Analysis - Analise project dependencies.");
        projectRest.setIssueTrackerUrl(null);
        projectRest.setProjectUrl("https://github.com/project-ncl/dependency-analysis");
        projectRest.setLicenseId(null);

        RepositoryConfigurationRest repositoryConfigurationRest = new RepositoryConfigurationRest();
        repositoryConfigurationRest.setId(repositoryConfigurationBCId);
        repositoryConfigurationRest
                .setInternalUrl("git+ssh://code.stage.engineering.redhat.com/project-ncl/dependency-analysis-new.git");
        repositoryConfigurationRest.setExternalUrl("https://github.com/project-ncl/dependency-analysis-new.git");
        repositoryConfigurationRest.setPreBuildSyncEnabled(false);

        Set<Integer> dependencyIds = new HashSet<Integer>();
        dependencyIds.add(dependencyBCId);
        BuildConfigurationRest buildConfigurationRest = new BuildConfigurationRest();
        buildConfigurationRest.setId(buildConfigurationId);
        buildConfigurationRest.setName("dependency-analysis-master-new");
        buildConfigurationRest.setDescription("Test config for Dependency Analysis.");
        buildConfigurationRest.setBuildScript("mvn clean deploy -DskipTests=true");
        buildConfigurationRest.setScmRevision("master");
        buildConfigurationRest.setBuildType(BuildType.MVN);
        buildConfigurationRest.setCreationTime(creationModificationTime);
        buildConfigurationRest.setLastModificationTime(creationModificationTime);
        buildConfigurationRest.setArchived(false);
        buildConfigurationRest.setEnvironment(buildEnvironmentRest);
        buildConfigurationRest.setProject(projectRest);
        buildConfigurationRest.setRepositoryConfiguration(repositoryConfigurationRest);
        buildConfigurationRest.setProductVersionId(productVersionBCId);
        buildConfigurationRest.setDependencyIds(dependencyIds);

        // Start of update (validateBeforeUpdating in BuildConfigurationProvider)
        validateBeforeUpdating(buildConfigurationId, buildConfigurationRest);
        validateIfItsNotConflicted(buildConfigurationRest);
        validateDependencies(buildConfigurationRest.getId(), buildConfigurationRest.getDependencyIds());

        BuildConfiguration.Builder builder = buildConfigurationRest.toDBEntityBuilder();
        BuildConfiguration buildConfigDB = buildConfigurationRepository.queryById(buildConfigurationRest.getId());
        // If updating an existing record, need to replace several fields from the rest entity with values from DB
        if (buildConfigDB != null) {
            builder.lastModificationTime(buildConfigDB.getLastModificationTime()); // Handled by JPA @Version
            builder.creationTime(buildConfigDB.getCreationTime()); // Immutable after creation
            if (buildConfigurationRest.getDependencyIds() == null) {
                // If the client request does not include a list of dependencies, just keep the current set
                builder.dependencies(buildConfigDB.getDependencies());
            }
        }
        buildConfigurationRepository.save(builder.build());
    }

    @SuppressWarnings("unchecked")
    private void validateBeforeUpdating(Integer id, BuildConfigurationRest restEntity) throws RestValidationException {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(buildConfigurationRepository, id, true);
    }

    @SuppressWarnings("unchecked")
    private void validateIfItsNotConflicted(BuildConfigurationRest buildConfigurationRest)
            throws ConflictedEntryException, InvalidEntityException {
        ValidationBuilder.validateObject(buildConfigurationRest, WhenUpdating.class).validateConflict(() -> {
            BuildConfiguration buildConfigurationFromDB = buildConfigurationRepository
                    .queryByPredicates(withName(buildConfigurationRest.getName()), isNotArchived());

            // don't validate against myself
            if (buildConfigurationFromDB != null
                    && !buildConfigurationFromDB.getId().equals(buildConfigurationRest.getId())) {
                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        buildConfigurationFromDB.getId(),
                        BuildConfiguration.class,
                        "Build configuration with the same name already exists");
            }
            return null;
        });
    }

    private void validateDependencies(Integer buildConfigId, Set<Integer> dependenciesIds)
            throws InvalidEntityException {
        if (dependenciesIds == null || dependenciesIds.isEmpty()) {
            return;
        }

        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(buildConfigId);
        for (Integer dependencyId : dependenciesIds) {

            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                    .validateCondition(
                            !buildConfig.getId().equals(dependencyId),
                            "A build configuration cannot depend on itself");

            BuildConfiguration dependency = buildConfigurationRepository.queryById(dependencyId);
            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                    .validateCondition(
                            !dependency.getAllDependencies().contains(buildConfig),
                            "Cannot add dependency from : " + buildConfig.getId() + " to: " + dependencyId
                                    + " because it would introduce a cyclic dependency");
        }
    }
}
