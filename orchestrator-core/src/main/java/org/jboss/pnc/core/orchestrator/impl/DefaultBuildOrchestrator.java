package org.jboss.pnc.core.orchestrator.impl;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.buildinfo.BuildInfoRepository;
import org.jboss.pnc.core.buildinfo.model.User;
import org.jboss.pnc.core.buildinfo.model.builder.BuildInfoBuilder;
import org.jboss.pnc.core.environment.EnvironmentDriver;
import org.jboss.pnc.core.environment.model.EnvironmentRecipe;
import org.jboss.pnc.core.environment.EnvironmentRepository;
import org.jboss.pnc.core.environment.model.RunnableEnvironment;
import org.jboss.pnc.core.message.MessageQueueSender;
import org.jboss.pnc.core.orchestrator.BuildOrchestrator;
import org.jboss.pnc.core.project.model.BuildRecipe;
import org.jboss.pnc.core.project.model.ProjectBuildInfo;
import org.jboss.pnc.core.project.ProjectBuilder;
import org.jboss.pnc.core.project.ProjectRepository;
import org.jboss.pnc.core.repository.RepositoryManager;
import org.jboss.pnc.core.repository.model.RunnableRepositoriesConfiguration;

public class DefaultBuildOrchestrator implements BuildOrchestrator {

    EnvironmentRepository environmentRepository;
    BuildInfoRepository buildInfoRepository;
    ProjectRepository projectRepository;
    EnvironmentDriver environmentDriver;
    MessageQueueSender messageQueueSender;
    RepositoryManager repositoryManager;
    ProjectBuilder projectBuilder;

    @Override
    public BuildInfo build(BuildIdentifier buildId, User whoStartedBuild) {
        BuildInfoBuilder buildInfoBuilder = BuildInfoBuilder.fromBuildId(buildId).startedBy(whoStartedBuild);

        messageQueueSender.notifyBuildStarted(buildId, whoStartedBuild);

        BuildRecipe buildRecipe = projectRepository.getBuildRecepie(buildId);
        buildInfoBuilder.withBuildRecepie(buildRecipe);

        EnvironmentRecipe environmentRecipe = environmentRepository.getEnvironmentRecipe(buildRecipe);
        buildInfoBuilder.withEnvironmentRecepie(environmentRecipe);

        RunnableEnvironment environmentForBuild = environmentDriver.buildEnvironment(environmentRecipe);
        buildInfoBuilder.withRunnableEnvironment(environmentForBuild);

        RunnableRepositoriesConfiguration repositoriesForBuild = repositoryManager.configureRepositories(buildRecipe, buildId);
        buildInfoBuilder.withRepositoriesConfiguration(repositoriesForBuild);

        messageQueueSender.notifyEnvironmentAssembled(buildId, environmentForBuild, repositoriesForBuild);

        ProjectBuildInfo projectBuildInfo = projectBuilder.buildProject(environmentForBuild, repositoriesForBuild);
        buildInfoBuilder.withProjectBuildInfo(projectBuildInfo);

        return buildInfoRepository.save(buildInfoBuilder.build());
    }

    /*
     * Called asynchronously
     */
    @Override
    public BuildInfo finishBuild(BuildIdentifier buildId, User whoFinishedBuild) {
        BuildInfo buildInfo = buildInfoRepository.findByBuildId(buildId);
        BuildInfoBuilder buildInfoBuilder = BuildInfoBuilder.fromBuildInfo(buildInfo);

        buildInfoBuilder.finishedBy(whoFinishedBuild);

        ProjectBuildInfo projectBuildInfo = projectBuilder.collectResults(buildInfo);
        buildInfoBuilder.withProjectBuildInfo(projectBuildInfo);

        repositoryManager.cleanupRepositoryConfiguration(buildInfo);
        environmentDriver.cleanupEnvironment(buildInfo);

        messageQueueSender.notifyBuildFinished(buildInfo.getBuildId(), whoFinishedBuild);

        return buildInfoRepository.save(buildInfoBuilder.build());
    }

}
