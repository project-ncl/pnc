package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildTask extends ProjectBuildConfiguration {
    private ProjectBuildConfiguration projectBuildConfiguration;
    private Consumer<TaskStatus> onStatusUpdate;
    private Consumer<Exception> onError;
    TaskStatus status;
    private RepositoryConfiguration repositoryConfiguration;


    public BuildTask(ProjectBuildConfiguration projectBuildConfiguration, Consumer<TaskStatus> onStatusUpdate, Consumer<Exception> onError) {
        this.projectBuildConfiguration = projectBuildConfiguration;
        this.onStatusUpdate = onStatusUpdate;
        this.onError = onError;
    }


    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void onStatusUpdate(TaskStatus status) {
        onStatusUpdate.accept(status);
    }

    public void onError(Exception e) {
        onError.accept(e);
    }

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }
}
