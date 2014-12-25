package org.jboss.pnc.core.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.SubmittedBuild;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .addPackage(BuildCoordinator.class.getPackage())
                .addPackage(BuildDriverMock.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    DatastoreMock datastore;

    Logger log = Logger.getLogger(BuildProjectsTest.class);

    Thread consumer;

    @Before
    public void startConsumer() {
    }

    @After
    public void stopConsumer() {
    }

    @Test
    @InSequence(10)
    public void buildSingleProjectTestCase() throws Exception {


        BuildCollection buildCollection = new TestBuildCollectionBuilder().build("foo", "Foo desc.", "1.0");
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

//TODO move this test to datastore test
//        buildCoordinator.buildProjects(projectBuildConfigurations, buildCollection);
//        assertThat(datastore.getBuildResults()).hasSize(6);

        buildProject(configurationBuilder.build(1, "c1-java"), buildCollection);

    }

    @Test
    @InSequence(10)
    public void buildMultipleProjectsTestCase() throws Exception {

        BuildCollection buildCollection = new TestBuildCollectionBuilder().build("foo", "Foo desc.", "1.0");
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        Function<TestBuildConfig, Runnable> createJob = (config) -> {
            Runnable task = () -> {
                try {
                    buildProject(config.configuration, config.collection);
                } catch (InterruptedException | CoreException e) {
                    throw new AssertionError("Something went wrong.", e);
                }
            };
            return task;
        };

        List<Runnable> list = new ArrayList();
        for (int i = 0; i < 100; i++) { //create 100 project configurations
            list.add(createJob.apply(new TestBuildConfig(configurationBuilder.build(i, "c" + i + "-java"), buildCollection)));
        }

        Function<Runnable, Thread> runInNewThread = (r) -> {
            Thread t = new Thread(r);
            t.start();
            return t;
        };

        Consumer<Thread> waitToComplete = (t) -> {
            try {
                t.join(30000);
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting threads to complete", e);
            }
        };

        List<Thread> threads = list.stream().map(runInNewThread).collect(Collectors.toList());

        Assert.assertTrue("There are no running builds.", buildCoordinator.getSubmittedBuilds().size() > 0);
        SubmittedBuild submittedBuild = buildCoordinator.getSubmittedBuilds().iterator().next();
        Assert.assertTrue("Build has no status.", submittedBuild.getStatus() != null);

        threads.forEach(waitToComplete);
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<ProjectBuildResult> buildResults = datastore.getBuildResults();
        Assert.assertTrue("Missing datastore results.", buildResults.size() > 10);

        ProjectBuildResult projectBuildResult = buildResults.get(0);
        String buildLog = projectBuildResult.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));
    }

    private void buildProject(ProjectBuildConfiguration projectBuildConfiguration, BuildCollection buildCollection) throws InterruptedException, CoreException {
        List<BuildStatus> receivedStatuses = new ArrayList();

        int nStatusUpdates = 10;

        final Semaphore semaphore = new Semaphore(nStatusUpdates);

        Consumer<BuildStatus> onStatusUpdate = (newStatus) -> {
            receivedStatuses.add(newStatus);
            semaphore.release(1);
            log.debug("Received status update " + newStatus.toString());
            log.trace("Semaphore released, there are " + semaphore.availablePermits() + " free entries.");
        };
        Set<Consumer<BuildStatus>> statusUpdateListeners = new HashSet<>();
        statusUpdateListeners.add(onStatusUpdate);
        semaphore.acquire(nStatusUpdates); //there should be 6 callbacks
        SubmittedBuild submittedBuild = buildCoordinator.build(projectBuildConfiguration, statusUpdateListeners, new HashSet<Consumer<String>>());
        submittedBuild.registerStatusUpdateListener(onStatusUpdate);
        semaphore.tryAcquire(nStatusUpdates, 30, TimeUnit.SECONDS); //wait for callback to release

        assertStatusUpdateReceived(receivedStatuses, BuildStatus.REPO_SETTING_UP);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_SETTING_UP);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_WAITING);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_COMPLETED_SUCCESS);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.STORING_RESULTS);

    }

    private void assertStatusUpdateReceived(List<BuildStatus> receivedStatuses, BuildStatus status) {
        boolean received = false;
        for (BuildStatus receivedStatus : receivedStatuses) {
            if (receivedStatus.equals(status)) {
                received = true;
                break;
            }
        }
        Assert.assertTrue("Did not received status update for " + status +".", received );
    }

    class TestBuildConfig {
        private final ProjectBuildConfiguration configuration;
        private final BuildCollection collection;

        TestBuildConfig(ProjectBuildConfiguration configuration, BuildCollection collection) {
            this.configuration = configuration;
            this.collection = collection;
        }
    }

}
