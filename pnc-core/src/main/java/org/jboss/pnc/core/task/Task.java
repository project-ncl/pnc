package org.jboss.pnc.core.task;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
*/
public class Task {
    TaskStatus status;

    public Task(ProjectBuildConfiguration projectBuildConfiguration) {

    }

    public TaskStatus getStatus() {
        return status;
    }
}
