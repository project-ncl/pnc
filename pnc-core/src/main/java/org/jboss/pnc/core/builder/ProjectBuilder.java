package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.builder.BuildDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public void buildProject(ProjectBuildConfiguration projectBuildConfiguration, BuildCollection buildCollection, Consumer<TaskStatus> onStatusUpdate, Consumer<BuildDetails> onComplete) throws CoreException {
        try {
            log.fine("Adding build configuration " + projectBuildConfiguration);
            BuildTask buildTask = new BuildTask(runningBuilds, buildTaskQueue, projectBuildConfiguration, buildCollection, onStatusUpdate, onComplete);
            //BuildTask buildTask = new BuildTask(runningBuilds, projectBuildConfiguration, buildCollection, onStatusUpdate, onSuccessComplete, onError);
            //buildTaskQueue.add(buildTask);
            //runningBuilds.add(buildTask);
        } catch (Exception e) {
            throw new CoreException(e);
        }
    }

    public Set<BuildTask> getRunningBuilds() {
        //return runningBuilds.stream().map(t -> t.getBuildDetails()).collect(Collectors.toList());
        return runningBuilds.stream().collect(Collectors.toSet()); //TODO protect state methods
    }

}
