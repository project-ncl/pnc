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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.datastore.audit.AuditRepository;
import org.jboss.pnc.spi.datastore.audit.Revision;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End to end scenario test for auditing entities.
 * <i>Note that Hibernate Envers works on persisted entities so we need to put each test step in a
 * separate method and commit the transaction using Arquillian JTA integration</i>
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.COMMIT)
@Category(ContainerTest.class)
public class DatastoreTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String ORIGINAL_NAME = "original name";

    public static final String CHANGED_NAME = "changedName";

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductVersionRepository productVersionRepository;

    @Inject
    BuildEnvironmentRepository environmentRepository;

    @Inject
    RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

    @Inject
    AuditRepository<BuildConfiguration, Integer> auditedBuildConfigurationRepository;

    private static int testedConfigurationId;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        restWar.addClass(DatastoreTest.class);

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
    public void prepareDataForAuditTest() throws Exception {
        Product product = Product.Builder.newBuilder().name("test").abbreviation("tt").build();
        product = productRepository.save(product);
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().version("1.0").product(product)
                .generateBrewTagPrefix(product.getAbbreviation(), "1.0", "${product_short_name}-${product_version}-pnc")
                .build();
        BuildEnvironment environment = BuildEnvironment.Builder.newBuilder().name("DatastoreTest Test Environment")
                .systemImageType(SystemImageType.DOCKER_IMAGE).systemImageId("92387492739").build();
        Project project = Project.Builder.newBuilder().name("test").build();
        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(BuildConfigurationRestTest.VALID_INTERNAL_REPO).build();

        environment = environmentRepository.save(environment);
        productVersion = productVersionRepository.save(productVersion);
        project = projectRepository.save(project);
        repositoryConfigurationRepository.save(repositoryConfiguration);

        BuildConfiguration testedConfiguration = BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .buildEnvironment(environment)
                .name(ORIGINAL_NAME).project(project)
                .repositoryConfiguration(repositoryConfiguration)
                .build();

        testedConfigurationId = buildConfigurationRepository.save(testedConfiguration).getId();
    }

    /**
     * Secondly we need to modify it.
     */
    @Test
    @InSequence(-1)
    public void modifyDataForAuditTest() throws Exception {
        BuildConfiguration testedConfiguration = buildConfigurationRepository.queryById(testedConfigurationId);

        testedConfiguration = buildConfigurationRepository.save(testedConfiguration);
        testedConfiguration.setName(CHANGED_NAME);
        testedConfiguration = buildConfigurationRepository.save(testedConfiguration);
    }

    @Test
    public void shouldCreateAuditedBuildConfigurationWhenUpdating() throws Exception {
        //given
        BuildConfiguration testedConfiguration = buildConfigurationRepository.queryById(testedConfigurationId);

        //when
        List<Revision<BuildConfiguration, Integer>> revisions = auditedBuildConfigurationRepository.getAllRevisions();
        // -1 is the current entity, -2 is the last modification
        Revision<BuildConfiguration, Integer> lastModification = revisions.get(revisions.size() - 2);

        //than
        assertThat(lastModification.getId()).isEqualTo(testedConfiguration.getId());
        assertThat(lastModification.getAuditedEntity().getName()).isEqualTo("original name");
    }

}
