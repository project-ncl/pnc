package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.logging.Logger;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-11.
 */
@ApplicationScoped
public class JenkinsBuildMonitor {

    private static final Logger log = Logger.getLogger(JenkinsBuildMonitor.class);

    private ScheduledExecutorService executor; //TODO shutdown
    private JenkinsServerFactory jenkinsServerFactory;
    private static final int MAX_IO_FAILURES = 5; //TODO configurable

    @Deprecated
    public JenkinsBuildMonitor() {}

    @Inject
    public JenkinsBuildMonitor(JenkinsServerFactory jenkinsServerFactory) {
        this.jenkinsServerFactory = jenkinsServerFactory;
        int nThreads = 4; //TODO configurable
        executor = Executors.newScheduledThreadPool(nThreads);
    }

    public void monitor(String jobName, int buildNumber, Consumer<BuildDriverStatus> onMonitorComplete, 
            Consumer<Exception> onMonitorError, String jenkinsUrl) {

//        ObjectWrapper<Integer> statusRetrieveFailed = 0;
        AtomicInteger statusRetrieveFailed = new AtomicInteger(0);


        ObjectWrapper<ScheduledFuture<?>> futureReference = new ObjectWrapper<>();
        Runnable monitor = () -> {
            try {
                Build jenkinsBuild = getBuild(jenkinsServerFactory.getJenkinsServer(jenkinsUrl), jobName, buildNumber);
                if (jenkinsBuild == null)
                    //Build didn't started yet.
                    return;

                BuildWithDetails jenkinsBuildDetails = null;
                try {
                    jenkinsBuildDetails = jenkinsBuild.details();
                    log.tracef("Checking if %s #%s is running.", jobName, buildNumber);
                } catch (IOException e) {
                    //Ignore error if it is not repeating
                    int failed = statusRetrieveFailed.getAndIncrement();
                    if (failed >= MAX_IO_FAILURES) {
                        throw new BuildDriverException("Cannot read job " + jobName + " status.", e);
                    }
                }
                boolean building = jenkinsBuildDetails.isBuilding();
                int duration = jenkinsBuildDetails.getDuration();
                if (!building && duration > 0 ) {
                    BuildStatusAdapter buildStatusAdapter = new BuildStatusAdapter(jenkinsBuildDetails.getResult());
                    onMonitorComplete.accept(buildStatusAdapter.getBuildStatus());
                    futureReference.get().cancel(true);
                }
            } catch (Exception e) {
                onMonitorError.accept(e);
            }
        };

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(monitor, 0L, 5L, TimeUnit.SECONDS); //TODO configurable
        futureReference.set(future);

    }

    /**
     * @return Build or null if build didn't started yet
     */
    private Build getBuild(JenkinsServer jenkinsServer, String jobName, int buildNumber) throws IOException, BuildDriverException {
        JobWithDetails buildJob = jenkinsServer.getJob(jobName);

        //Build build = buildJob.getLastBuild() //throws NPE if there are no build for this job. see https://github.com/RisingOak/jenkins-client/issues/45
        List<Build> builds = buildJob.getBuilds();

        List<Build> buildsMatchingNumber = builds.stream().filter(b -> b.getNumber() == buildNumber).collect(Collectors.toList());

        if (buildsMatchingNumber.size() == 0) {
            log.trace("There is no Job #" + buildNumber + " for " + jobName + " (probably hasn't started yet).");
            return null;
        } else if (buildsMatchingNumber.size() == 1) {
            log.trace("Found Job #" + buildNumber + " for " + jobName + ".");
            return buildsMatchingNumber.get(0);
        } else {
            throw new BuildDriverException("There are multiple builds for " + jobName + " with the same build number " + buildNumber + ".");
        }
    }

}
