package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
public class ProjectBuilder {

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    DatastoreMock datastore;

    private static final Logger log = Logger.getLogger(ProjectBuilder.class.getName());

    @Deployment
    public static JavaArchive createDeployment() {

        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
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

    void buildProject(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet) throws InterruptedException, CoreException {
        log.info("Building project " + buildConfiguration.getName());
        List<BuildStatus> receivedStatuses = new ArrayList();

        int nStatusUpdates = 8;

        final Semaphore semaphore = new Semaphore(nStatusUpdates);

        Consumer<BuildStatus> onStatusUpdate = (newStatus) -> {
            receivedStatuses.add(newStatus);
            semaphore.release(1);
            log.fine("Received status update " + newStatus.toString() + " for project " + buildConfiguration.getId());
            log.finer("Semaphore released, there are " + semaphore.availablePermits() + " free entries.");
        };
        Set<Consumer<BuildStatus>> statusUpdateListeners = new HashSet<>();
        statusUpdateListeners.add(onStatusUpdate);
        semaphore.acquire(nStatusUpdates);
        BuildTask buildTask = buildCoordinator.build(buildConfiguration, statusUpdateListeners, new HashSet<Consumer<String>>());

        List<BuildStatus> errorStates = Arrays.asList(BuildStatus.REJECTED, BuildStatus.SYSTEM_ERROR);
        if (errorStates.contains(buildTask.getStatus())) {
            throw new AssertionError("Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: " + buildTask.getStatusDescription() + "");
        }

        log.fine("Build " + buildTask.getId() + " has been submitted.");
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { //wait for callback to release
            log.warning("Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: " + buildTask.getStatusDescription() + ".");
            throw new AssertionError("Timeout while waiting for status updates.");
        }

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
        Assert.assertTrue("Did not received update for status " + status + ".", received);
    }

    protected class TestBuildConfig {
        final BuildConfiguration configuration;
        final BuildRecordSet collection;

        TestBuildConfig(BuildConfiguration configuration, BuildRecordSet collection) {
            this.configuration = configuration;
            this.collection = collection;
        }
    }


    protected void assertBuildArtifactsPresent(List<Artifact> builtArtifacts) {
        Assert.assertTrue("Missing built artifacts.", builtArtifacts.size() > 0);
        Artifact artifact = builtArtifacts.get(0);
        Assert.assertTrue("Invalid built artifact in result.", artifact.getIdentifier().startsWith("test"));
    }

}
