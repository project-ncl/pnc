package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.buildCoordinator.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
public class ProjectBuilder {

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    DatastoreMock datastore;

    @Inject
    TestCDIBuildStatusChangedReceiver statusChangedReceiver;

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(Configuration.class)
                .addClass(Environment.Builder.class)
                .addClass(TestCDIBuildStatusChangedReceiver.class)
                .addPackages(true, BuildDriverFactory.class.getPackage(), BuildDriverMock.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");

        log.debug(jar.toString(true));
        return jar;
    }

    void buildProject(BuildConfiguration buildConfiguration, BuildRecordSet buildRecordSet) throws InterruptedException, CoreException {
        log.info("Building project {}", buildConfiguration.getName());
        List<BuildStatus> receivedStatuses = new ArrayList<>();

        //Defines a number of callbacks, which are executed after buildStatus update
        final int nStatusUpdates = 12;

        final Semaphore semaphore = new Semaphore(nStatusUpdates);

        statusChangedReceiver.addBuildStatusChangedEventListener(statusUpdate -> {
            receivedStatuses.add(statusUpdate.getNewStatus());
            semaphore.release(1);
            log.debug("Received status update {} for project {}", statusUpdate.toString(), buildConfiguration.getId());
            log.debug("Semaphore released, there are {} free entries", semaphore.availablePermits());
        });

        semaphore.acquire(nStatusUpdates);
        BuildTask buildTask = buildCoordinator.build(buildConfiguration);

        List<BuildStatus> errorStates = Arrays.asList(BuildStatus.REJECTED, BuildStatus.SYSTEM_ERROR, BuildStatus.BUILD_ENV_SETUP_COMPLETE_WITH_ERROR);
        if (errorStates.contains(buildTask.getStatus())) {
            fail("Build " + buildTask.getId() + " has status:" + buildTask.getStatus() + " with description: " + buildTask.getStatusDescription());
        }

        log.debug("Build {} has been submitted", buildTask.getId());
        if (!semaphore.tryAcquire(nStatusUpdates, 15, TimeUnit.SECONDS)) { //wait for callback to release
            log.warn("Build {} has status {} with description {}", buildTask.getId(), buildTask.getStatus(),
                    buildTask.getStatusDescription());
            fail("Timeout while waiting for status updates");
        }

        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_SETTING_UP);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_SETUP_COMPLETE_SUCCESS);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.REPO_SETTING_UP);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_SETTING_UP);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_WAITING);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_COMPLETED_SUCCESS);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_DESTROYING);
        assertStatusUpdateReceived(receivedStatuses, BuildStatus.BUILD_ENV_DESTROYED);
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
        assertTrue("Did not received update for status " + status + ".", received);
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
        assertTrue("Missing built artifacts.", builtArtifacts.size() > 0);
        Artifact artifact = builtArtifacts.get(0);
        assertTrue("Invalid built artifact in result.", artifact.getIdentifier().startsWith("test"));
    }

}
