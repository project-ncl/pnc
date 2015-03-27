package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-03-26.
 */
public class BuildSetTask {

    private BuildConfigurationSet buildConfigurationSet;

    private final BuildTaskType buildTaskType;

    private BuildStatus status;

    private String statusDescription;
    private Set<BuildTask> buildTasks = new HashSet<>();

    public BuildSetTask(BuildConfigurationSet buildConfigurationSet, BuildTaskType buildTaskType) {
        this.buildConfigurationSet = buildConfigurationSet;
        this.buildTaskType = buildTaskType;
    }

    public BuildConfigurationSet getBuildConfigurationSet() {
        return buildConfigurationSet;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public Set<BuildTask> getBuildTasks() {
        return buildTasks;
    }

    public void addBuildTask(BuildTask buildTask) {
        buildTasks.add(buildTask);
    }

    public Integer getId() {
        return buildConfigurationSet.getId();
    }

    public BuildTaskType getBuildTaskType() {
        return buildTaskType;
    }
}
