package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.EnvironmentRepository;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.*;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
@Category(ContainerTest.class)
public class DatastoreTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(DatastoreTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldStoreResults() {
        // given
        Product product = Product.Builder.newBuilder().name("DS_PRODUCT").description("DS_PRODUCT_DESC").build();
        ProductVersion productVersion = ProductVersion.Builder.newBuilder().version("DS_PRODUCT_VERSION").product(product)
                .build();
        productVersion = productVersionRepository.save(productVersion);

        Project project = Project.Builder.newBuilder().name("DS_PROJECT_NAME").description("DS_PROJECT_NAME_DESC")
                .projectUrl("https://github.com/ds-project-ncl/pnc")
                .build();
        project = projectRepository.save(project);

        Environment environment = Environment.Builder.defaultEnvironment().build();
        environment = environmentRepository.save(environment);

        BuildConfigurationSet buildConfigurationSet = BuildConfigurationSet.Builder.newBuilder().name("Test build config set")
                .productVersion(productVersion).build();

        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                .buildScript("mvn clean deploy -Dmaven.test.skip").environment(environment)
                .name("DS_PROJECT_BUILD_CFG_ID").buildConfigurationSet(buildConfigurationSet).project(project)
                .scmRepoURL("https://github.com/ds-project-ncl/pnc.git").scmRevision("*/v0.2")
                .description("Test build config for project newcastle").build();

        User user = User.Builder.newBuilder().username("test-user").email("test@test.com")
                .firstName("firstname").lastName("lastname").build();
        user = userRepository.save(user);

        BuildRecord buildRecord = BuildRecord.Builder.newBuilder().buildScript("mvn clean deploy -Dmaven.test.skip").id(1)
                .name("PNC_PROJECT_BUILD_CFG_ID").buildConfiguration(buildConfiguration)
                .scmRepoURL("https://github.com/project-ncl/pnc.git").scmRevision("*/v0.2")
                .description("DataStore Build record test").status(BuildDriverStatus.CANCELLED)
                .user(user).startTime(Timestamp.from(Instant.now())).endTime(Timestamp.from(Instant.now())).build();
        buildRecord = buildRecordRepository.saveAndFlush(buildRecord);

        List<BuildRecord> objectsInDb = buildRecordRepository.findAll();

        // then
        assertThat(objectsInDb).isNotEmpty().contains(buildRecord);
    }

}
