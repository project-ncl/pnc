package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.*;
import org.jboss.pnc.model.builder.*;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class DatastoreTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(DatastoreTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldStoreResults() {
        // given
        Product product = ProductBuilder.newBuilder().name("DS_PRODUCT").description("DS_PRODUCT_DESC").build();
        ProductVersion productVersion = ProductVersionBuilder.newBuilder().version("DS_PRODUCT_VERSION").product(product)
                .build();

        Project project = ProjectBuilder.newBuilder().name("DS_PROJECT_NAME").description("DS_PROJECT_NAME_DESC")
                .projectUrl("https://github.com/ds-project-ncl/pnc")
                .issueTrackerUrl("https://projects.engineering.redhat.com/browse/NCL").build();

        BuildConfiguration buildConfiguration = BuildConfigurationBuilder.newBuilder()
                .buildScript("mvn clean deploy -Dmaven.test.skip").environment(EnvironmentBuilder.defaultEnvironment().build())
                .id(1).name("DS_PROJECT_BUILD_CFG_ID").productVersion(productVersion).project(project)
                .scmRepoURL("https://github.com/ds-project-ncl/pnc.git").scmRevision("*/v0.2")
                .description("Test build config for project newcastle").build();

        BuildRecord buildRecord = BuildRecordBuilder.newBuilder().buildScript("mvn clean deploy -Dmaven.test.skip").id(1)
                .name("PNC_PROJECT_BUILD_CFG_ID").buildConfiguration(buildConfiguration)
                .scmRepoURL("https://github.com/project-ncl/pnc.git").scmRevision("*/v0.2")
                .description("DataStore Build record test").status(BuildDriverStatus.CANCELLED).build();

        projectRepository.save(project);

        // when
        buildRecord = buildRecordRepository.save(buildRecord);
        List<BuildRecord> objectsInDb = buildRecordRepository.findAll();

        // then
        assertThat(objectsInDb).isNotEmpty().contains(buildRecord);
    }

}
