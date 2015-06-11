/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.datastore.audit.AuditRepository;
import org.jboss.pnc.spi.datastore.audit.Revision;
import org.jboss.pnc.spi.datastore.repositories.*;
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
    BuildRecordRepository buildRecordRepository;

    @Inject
    EnvironmentRepository environmentRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    AuditRepository<BuildConfiguration, Integer> auditedBuildConfigurationRepository;

    private static int testedConfigurationId;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
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
        Product product = Product.Builder.newBuilder().name("test").build();
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().version("1").product(product).build();
        Environment environment = Environment.Builder.defaultEnvironment().build();
        Project project = Project.Builder.newBuilder().name("test").build();
        environment = environmentRepository.save(environment);
        productVersion = productVersionRepository.save(productVersion);
        project = projectRepository.save(project);

        BuildConfiguration testedConfiguration = BuildConfiguration.Builder.newBuilder()
                .environment(environment).name(ORIGINAL_NAME).project(project).build();

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
