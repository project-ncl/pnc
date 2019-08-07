package org.jboss.pnc.bpm.model.mapper;

import org.jboss.pnc.bpm.model.BuildDriverResultRest;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.RepositoryManagerResultRest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class BuildResultMapper {

    @Inject
    RepositoryManagerResultMapper repositoryManagerResultMapper;

    public BuildResult toEntity(BuildResultRest buildResultRest) {
        RepositoryManagerResult repositoryManagerResult = null;
        if (buildResultRest.getRepositoryManagerResult() != null) {
            repositoryManagerResult = repositoryManagerResultMapper.toEntity(buildResultRest.getRepositoryManagerResult());
        }

        return new BuildResult(
                buildResultRest.getCompletionStatus(),
                ofNullable(buildResultRest.getProcessException()),
                buildResultRest.getProcessLog(),
                ofNullable(buildResultRest.getBuildExecutionConfiguration()),
                ofNullable(buildResultRest.getBuildDriverResult()),
                ofNullable(repositoryManagerResult),
                ofNullable(buildResultRest.getEnvironmentDriverResult()),
                ofNullable(buildResultRest.getRepourResult()));
    }

    public BuildResultRest toDTO(BuildResult buildResult) {
        CompletionStatus completionStatus = buildResult.getCompletionStatus();
        ProcessException processException = buildResult.getProcessException().orElse(null);
        String processLog = buildResult.getProcessLog();

        BuildExecutionConfigurationRest buildExecutionConfiguration;
        if (buildResult.getBuildExecutionConfiguration().isPresent()) {
            BuildExecutionConfiguration bec = buildResult.getBuildExecutionConfiguration().get();
            buildExecutionConfiguration = new BuildExecutionConfigurationRest(bec);
        } else {
            buildExecutionConfiguration = null;
        }

        BuildDriverResultRest buildDriverResult;
        if (buildResult.getBuildDriverResult().isPresent()) {
            BuildDriverResult result = buildResult.getBuildDriverResult().get();
            buildDriverResult = new BuildDriverResultRest(result);
        } else {
            buildDriverResult = null;
        }

        RepositoryManagerResultRest repositoryManagerResult;
        if (buildResult.getRepositoryManagerResult().isPresent()) {
            RepositoryManagerResult result = buildResult.getRepositoryManagerResult().get();
            repositoryManagerResult = repositoryManagerResultMapper.toDTO(result);
        } else {
            repositoryManagerResult = null;
        }

        EnvironmentDriverResult environmentDriverResult;
        if (buildResult.getEnvironmentDriverResult().isPresent()) {
            environmentDriverResult = buildResult.getEnvironmentDriverResult().get();
        } else {
            environmentDriverResult = null;
        }

        RepourResult repourResult = buildResult.getRepourResult().orElse(null);

        return new BuildResultRest(completionStatus, processException, processLog, buildExecutionConfiguration,
                buildDriverResult, repositoryManagerResult, environmentDriverResult, repourResult);
    }
}
