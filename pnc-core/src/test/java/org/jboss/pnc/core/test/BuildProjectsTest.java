package org.jboss.pnc.core.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.BuildConsumer;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.builder.operationHandlers.OperationHandler;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class BuildProjectsTest {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(Resources.class)
                .addClass(BuildDriverFactory.class)
                .addClass(RepositoryManagerFactory.class)
                .addClass(EnvironmentBuilder.class)
                .addClass(EnvironmentDriverProvider.class)
                .addPackage(OperationHandler.class.getPackage())
                .addPackage(ProjectBuilder.class.getPackage())
//                .addPackage(RepositoryManagerDriver.class.getPackage())
                .addPackage(BuildDriverMock.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    ProjectBuilder projectBuilder;

    @Inject
    DatastoreMock datastore;

    @Inject
    Logger log;

    @Inject
    BuildConsumer buildConsumer;

    @Test
    public void buildProjectTestCase() throws Exception {

        //start build consumer
        Thread consumer = new Thread(buildConsumer, "Build-consumer");
        consumer.start();

        Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();
        Environment nativeEnvironment = EnvironmentBuilder.defaultEnvironment().withNative().build();

//        Project p1 = new Project();
//        p1.setId(1);
//        p1.setName("p1-native");
//        ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationA1.setEnvironment(nativeEnvironment);
//        projectBuildConfigurationA1.setProject(p1);
//        p1.addProjectBuildConfiguration(projectBuildConfigurationA1);

        Project p2 = new Project();
        p2.setId(2);
        p2.setName("p2-java");
        ProjectBuildConfiguration projectBuildConfigurationB1 = new ProjectBuildConfiguration();
        projectBuildConfigurationB1.setEnvironment(javaEnvironment);
        projectBuildConfigurationB1.setProject(p2);
        projectBuildConfigurationB1.addDependency(projectBuildConfigurationB1);
        p2.addProjectBuildConfiguration(projectBuildConfigurationB1);

//        Project p3 = new Project();
//        p3.setId(3);
//        p3.setName("p3-java");
//        ProjectBuildConfiguration projectBuildConfigurationC1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationC1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationC1.setProject(p3);
//        p3.addProjectBuildConfiguration(projectBuildConfigurationC1);
//
//        Project p4 = new Project();
//        p4.setId(4);
//        p4.setName("p4-java");
//        ProjectBuildConfiguration projectBuildConfigurationD1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationD1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationD1.setProject(p4);
//        projectBuildConfigurationD1.addDependency(projectBuildConfigurationB1);
//        projectBuildConfigurationD1.addDependency(projectBuildConfigurationC1);
//        p4.addProjectBuildConfiguration(projectBuildConfigurationD1);
//
//        Project p5 = new Project();
//        p5.setId(5);
//        p5.setName("p5-native");
//        ProjectBuildConfiguration projectBuildConfigurationE1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationE1.setEnvironment(nativeEnvironment);
//        projectBuildConfigurationE1.setProject(p5);
//        projectBuildConfigurationE1.addDependency(projectBuildConfigurationD1);
//        p5.addProjectBuildConfiguration(projectBuildConfigurationE1);
//
//        Project p6 = new Project();
//        p6.setId(6);
//        p6.setName("p6-java");
//
//        ProjectBuildConfiguration projectBuildConfigurationF1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationF1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationF1.setProject(p6);
//        p6.addProjectBuildConfiguration(projectBuildConfigurationF1);
//
//        HashSet<ProjectBuildConfiguration> projectBuildConfigurations = new HashSet<>(
//                Arrays.asList(new ProjectBuildConfiguration[] { projectBuildConfigurationA1, projectBuildConfigurationB1,
//                        projectBuildConfigurationC1, projectBuildConfigurationD1, projectBuildConfigurationE1,
//                        projectBuildConfigurationF1 }));
//
//        log.info("Got projectBuilder: " + projectBuilder);
//        log.info("Building projectBuildConfigurations: " + projectBuildConfigurations.size());

        Product product = new Product("foo", "foo description");
        ProductVersion productVersion = new ProductVersion("1.0", product);
        product.addVersion(productVersion);
        BuildCollection buildCollection = new BuildCollection();
        buildCollection.setProductVersion(productVersion);
        buildCollection.setProductBuildBumber(1);
//        projectBuilder.buildProjects(projectBuildConfigurations, buildCollection);
//        assertThat(datastore.getBuildResults()).hasSize(6);


        List<TaskStatus> receivedStatuses = new ArrayList<TaskStatus>();

        final Semaphore semaphore = new Semaphore(6);

        Consumer<TaskStatus> onStatusUpdate = (newStatus) -> {
            receivedStatuses.add(newStatus);
            semaphore.release(1);
            log.finer("Received status update " + newStatus.getOperation());
            log.finer("Semaphore released, there are " + semaphore.availablePermits() + " free entries.");
        };
        Consumer<Exception> onError = (e) -> {
            e.printStackTrace();
        };
        semaphore.acquire(6); //there should be 6 callbacks
        projectBuilder.buildProject(projectBuildConfigurationB1, buildCollection, onStatusUpdate, onError);

        semaphore.tryAcquire(6, 30, TimeUnit.SECONDS); //wait for callback to release

        boolean receivedCREATE_REPOSITORY = false;
        boolean receivedBUILD_SCHEDULED = false;
        boolean receivedCOMPLETING_BUILD = false;
        for (TaskStatus receivedStatus : receivedStatuses) {
            if (receivedStatus.getOperation().equals(TaskStatus.Operation.CREATE_REPOSITORY)) {
                receivedCREATE_REPOSITORY = true;
            }
            if (receivedStatus.getOperation().equals(TaskStatus.Operation.BUILD_SCHEDULED)) {
                receivedBUILD_SCHEDULED = true;
            }
            if (receivedStatus.getOperation().equals(TaskStatus.Operation.COMPLETING_BUILD)) {
                receivedCOMPLETING_BUILD = true;
            }
        }

        Assert.assertTrue("All status updaters were not received." +
                        " receivedCREATE_REPOSITORY: " + receivedCREATE_REPOSITORY +
                        " receivedBUILD_SCHEDULED: " + receivedBUILD_SCHEDULED +
                        " receivedCOMPLETING_BUILD: " + receivedCOMPLETING_BUILD,
                receivedCREATE_REPOSITORY && receivedBUILD_SCHEDULED && receivedCOMPLETING_BUILD);

        consumer.interrupt();
    }

}
