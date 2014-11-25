package org.jboss.pnc.core.buildinfo.model.builder;


import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.buildinfo.model.User;
import org.jboss.pnc.core.environment.model.EnvironmentRecipe;
import org.jboss.pnc.core.environment.model.RunnableEnvironment;
import org.jboss.pnc.core.project.model.BuildRecipe;
import org.jboss.pnc.core.project.model.ProjectBuildInfo;
import org.jboss.pnc.core.repository.model.RunnableRepositoriesConfiguration;

public class BuildInfoBuilder {

    public static BuildInfoBuilder fromBuildId(BuildIdentifier buildId) {
        return null;
    }

    public BuildInfoBuilder startedBy(User user) {
        return null;
    }

    public BuildInfoBuilder withBuildRecepie(BuildRecipe buildRecipe) {
        return null;
    }

    public BuildInfoBuilder withEnvironmentRecepie(EnvironmentRecipe environmentRecipe) {
        return null;
    }

    public BuildInfoBuilder withRunnableEnvironment(RunnableEnvironment environmentForBuild) {
        return null;
    }

    public BuildInfoBuilder withRepositoriesConfiguration(RunnableRepositoriesConfiguration repositoriesForBuild) {
        return null;
    }

    public BuildInfoBuilder withProjectBuildInfo(ProjectBuildInfo projectBuildInfo) {
        return null;
    }

    public BuildInfo build() {
        return null;
    }

    public static BuildInfoBuilder fromBuildInfo(BuildInfo byBuildId) {
        return null;
    }

    public BuildInfoBuilder finishedBy(User whoFinishedBuild) {
        return null;
    }
}
