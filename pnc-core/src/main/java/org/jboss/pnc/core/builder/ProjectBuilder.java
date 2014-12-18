package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class ProjectBuilder {

    @Inject
    private BuildTaskQueue buildTaskQueue;

    private Logger log = Logger.getLogger(ProjectBuilder.class);

    private Queue<BuildTask> runningBuilds = new ConcurrentLinkedQueue<BuildTask>();

    public BuildTask buildProject(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<BuildJobDetails> onComplete) throws CoreException {
        try {
            log.debug("Adding build configuration " + projectBuildConfiguration);
            BuildTask buildTask = new BuildTask(runningBuilds, buildTaskQueue, projectBuildConfiguration, onStatusUpdate, onComplete);

            if (!isTaskAlreadyBuilding(buildTask)) {
                this.buildTaskQueue.add(buildTask);
                this.runningBuilds.add(buildTask);
                return buildTask;
            } else {
                log.info("Rejecting already running task.");
                //TODO return status rejected
                return null;
            }
        } catch (Exception e) {
            throw new CoreException(e);
        }
    }

    private boolean isTaskAlreadyBuilding(BuildTask buildTask) {
        return runningBuilds.contains(buildTask);
    }

    /**
     *
     * @return a copy of working queue as a unmodifiableList
     */
    public List<BuildTask> getRunningBuilds() {
        return Collections.unmodifiableList(runningBuilds.stream().collect(Collectors.toList()));
    }


}
