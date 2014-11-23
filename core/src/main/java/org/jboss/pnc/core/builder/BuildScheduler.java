package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.Project;

import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
class BuildScheduler {

    private Set<ProjectBuildTask> projects;
    private boolean anyBuildableProject;

    BuildScheduler(Set<Project> projects) {
        projects.forEach(project -> wrap(project));
        projects.stream().peek(project -> this.projects.add(wrap(project)));
    }

    private ProjectBuildTask wrap(Project project) {
        return new ProjectBuildTask(project);
    }

    /**
     * Method blocks until there are no new projects to build or all are build.
     *
     * @return Project with resolved dependencies or null if there are no projects left
     */
    ProjectBuildTask getNext() throws InterruptedException {
        for (ProjectBuildTask projectBuildTask : projects) {
            if (projectBuildTask.isNew() && projectBuildTask.hasResolvedDependencies(projects)) {
                return projectBuildTask;
            }
        }
        if (isAnyProjectStillBuilding()) {
            wait(); //TODO max timeout
            return getNext();
        }
        return null;
    }

    private boolean isAnyProjectStillBuilding() {
        for (ProjectBuildTask project : projects) {
            if (project.isBuilding()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyNewProject() {
        for (ProjectBuildTask project : projects) {
            if (project.isNew()) {
                return true;
            }
        }
        return false;
    }

}
