package org.jboss.pnc.jenkinsbuilddriver.buildmonitor;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.logging.Logger;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-11.
 */
@ApplicationScoped
public class JenkinsBuildMonitor {

    private static final Logger log = Logger.getLogger(JenkinsBuildMonitor.class);

    private ScheduledExecutorService executor; //TODO shutdown

    JenkinsBuildMonitor() {
        int nThreads = 4; //TODO configurable
        executor = Executors.newScheduledThreadPool(nThreads);
    }

    public void monitor(JenkinsServer jenkinsServer, BuildJobDetails buildJobDetails, Consumer<String> onMonitorComplete, Consumer<Exception> onMonitorError) {

        MonitorTask monitorTask = new MonitorTask(onMonitorComplete, onMonitorError);
        Runnable monitor = () -> {
            try {
                Build jenkinsBuild = getBuild(jenkinsServer, buildJobDetails);
                if (jenkinsBuild == null)
                    //Build didn't started yet.
                    return;

                BuildWithDetails jenkinsBuildDetails = null;
                try {
                    jenkinsBuildDetails = jenkinsBuild.details();
                } catch (IOException e) {
                    e.printStackTrace();//TODO
                }
                boolean building = jenkinsBuildDetails.isBuilding();
                int duration = jenkinsBuildDetails.getDuration();
                if (!building && duration > 0 ) {
                    monitorTask.onMonitorComplete.accept(""); //TODO pass anything ?
                    monitorTask.getCancelHook().cancel(true);
                }
            } catch (Exception e) {
                onMonitorError.accept(e);
            }
        };

        ScheduledFuture future = executor.scheduleAtFixedRate(monitor, 0L, 5L, TimeUnit.SECONDS); //TODO configurable
        monitorTask.setCancelHook(future);

    }

    /**
     * @return Build or null if build didn't started yet
     */
    private Build getBuild(JenkinsServer jenkinsServer, BuildJobDetails buildJobDetails) throws IOException, BuildDriverException {
        String jobName = buildJobDetails.getJobName();
        JobWithDetails buildJob = jenkinsServer.getJob(jobName);

        //Build build = buildJob.getLastBuild() //throws NPE if there are no build for this job. see https://github.com/RisingOak/jenkins-client/issues/45
        List<Build> builds = buildJob.getBuilds();

        List<Build> buildsMatchingNumber = builds.stream().filter(b -> b.getNumber() == buildJobDetails.getBuildNumber()).collect(Collectors.toList());

        if (buildsMatchingNumber.size() == 0) {
            log.trace("There is no Job #" + buildJobDetails.getBuildNumber() + " for " + buildJobDetails.getJobName() + " (probably hasn't started yet).");
            return null;
        } else if (buildsMatchingNumber.size() == 1) {
            log.trace("Found Job #" + buildJobDetails.getBuildNumber() + " for " + buildJobDetails.getJobName() + ".");
            return buildsMatchingNumber.get(0);
        } else {
            throw new BuildDriverException("There are multiple builds for " + buildJobDetails.getJobName() + " with the same build number " + buildJobDetails.getBuildNumber() + ".");
        }
    }

}
