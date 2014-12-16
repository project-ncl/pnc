package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class ProjectBuilder {

    @Inject
    private BuildTaskQueue buildTaskQueue; //TODO protect access

    @Inject
    private Logger log;

    private Set<BuildTask> runningBuilds = new HashSet<>(); //TODO protect access

    public BuildTask buildProject(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<BuildJobDetails> onComplete) throws CoreException {
        try {
            log.fine("Adding build configuration " + projectBuildConfiguration);
            return new BuildTask(runningBuilds, buildTaskQueue, projectBuildConfiguration, onStatusUpdate, onComplete);
            //BuildTask buildTask = new BuildTask(runningBuilds, projectBuildConfiguration, buildCollection, onStatusUpdate, onSuccessComplete, onError);
            //buildTaskQueue.add(buildTask);
            //runningBuilds.add(buildTask);
        } catch (Exception e) {
            throw new CoreException(e);
        }
    }

    public Set<BuildTask> getRunningBuilds() {
        return Collections.unmodifiableSet(runningBuilds);
    }

}
