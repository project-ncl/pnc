package org.jboss.pnc.jenkinsbuilddriver.buildmonitor;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.model.builder.BuildDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-11.
 */
@ApplicationScoped
public class JenkinsBuildMonitor {

    @Inject
    Logger log;

    private ScheduledExecutorService executor; //TODO shutdown

    JenkinsBuildMonitor() {
        int nThreads = 4; //TODO configurable
        executor = Executors.newScheduledThreadPool(nThreads);
    }

    public void monitor(JenkinsServer jenkinsServer, BuildDetails buildDetails, Consumer<String> onMonitorComplete, Consumer<Exception> onMonitorError) {

        MonitorTask monitorTask = new MonitorTask(buildDetails, onMonitorComplete, onMonitorError);
        Runnable monitor = () -> {
            try {
                Build jenkinsBuild = getBuild(jenkinsServer, buildDetails);
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
    private Build getBuild(JenkinsServer jenkinsServer, BuildDetails buildDetails) throws IOException {
        String jobName = buildDetails.getJobName();
        JobWithDetails buildJob = jenkinsServer.getJob(jobName);
        Build jenkinsBuild = buildJob.getLastBuild();
        int buildNumber = jenkinsBuild.getNumber();
        if (buildNumber != buildDetails.getBuildNumber()) {
            log.finer("Waiting job #" + buildDetails.getBuildNumber() + " to start.");
            return null;
        }
        return jenkinsBuild;
    }

}
