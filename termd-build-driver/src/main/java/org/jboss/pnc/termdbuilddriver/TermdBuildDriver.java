/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.*;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandBatchExecutionResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandInvoker;
import org.jboss.pnc.termdbuilddriver.transfer.TermdFileTranser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver {

    public static final String DRIVER_ID = "termd-build-driver";

    private static final Logger logger = LoggerFactory.getLogger(TermdBuildDriver.class);

    public TermdBuildDriver() {
    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return BuildType.JAVA.equals(buildType);
    }

    @Override
    public RunningBuild startProjectBuild(final BuildConfiguration buildConfiguration, final RunningEnvironment runningEnvironment)
            throws BuildDriverException {

        logger.info("[{}] Starting build for Build Configuration {}", runningEnvironment.getId(), buildConfiguration.getId());

        TermdRunningBuild termdRunningBuild = new TermdRunningBuild(runningEnvironment, buildConfiguration);

        addScriptDebugOption(termdRunningBuild)
                .thenCompose(returnedBuildScript -> changeToWorkingDirectory(termdRunningBuild, returnedBuildScript))
                .thenCompose(returnedBuildScript -> checkoutSources(termdRunningBuild, returnedBuildScript))
                .thenCompose(returnedBuildScript -> build(termdRunningBuild, returnedBuildScript))
                .thenCompose(returnedBuildScript -> uploadScript(termdRunningBuild, returnedBuildScript))
                .thenCompose(scriptPath -> invokeRemoteScript(termdRunningBuild, scriptPath))
                .handle((results, exception) -> updateStatus(termdRunningBuild, results, exception));

        return termdRunningBuild;
    }

    protected CompletableFuture<StringBuilder> addScriptDebugOption(TermdRunningBuild termdRunningBuild) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Adding debug option", termdRunningBuild.getRunningEnvironment().getId());

            StringBuilder commandAppender = new StringBuilder();
            String debugOption = "set -x";
            commandAppender.append(debugOption).append("\n");
            return commandAppender;
        });
    }

    protected CompletableFuture<StringBuilder> changeToWorkingDirectory(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Changing current directory", termdRunningBuild.getRunningEnvironment().getId());

            String cdCommand = "cd " + termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString();
            commandAppender.append(cdCommand).append("\n");
            return commandAppender;
        });
    }

    protected CompletableFuture<StringBuilder> checkoutSources(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Checking out sources", termdRunningBuild.getRunningEnvironment().getId());

            String cloneCommand = "git clone " + termdRunningBuild.getBuildConfiguration().getScmRepoURL() + " " + termdRunningBuild .getBuildConfiguration().getName();
            commandAppender.append(cloneCommand).append("\n");

            String cdCommand = "cd " + termdRunningBuild.getBuildConfiguration().getName();
            commandAppender.append(cdCommand).append("\n");

            String resetCommand = "git reset --hard " + termdRunningBuild.getBuildConfiguration().getScmRevision();
            commandAppender.append(resetCommand).append("\n");

            return commandAppender;
        });
    }

    protected CompletableFuture<StringBuilder> build(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Building", termdRunningBuild.getRunningEnvironment().getId());

            String buildCommand = termdRunningBuild.getBuildConfiguration().getBuildScript();
            commandAppender.append(buildCommand).append("\n");
            return commandAppender;
        });
    }

    protected CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, StringBuilder commandAppender) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Uploading script", termdRunningBuild.getRunningEnvironment().getId());
            logger.debug("[{}] Full script:\n {}", termdRunningBuild.getRunningEnvironment().getId(), commandAppender.toString());

            new TermdFileTranser(URI.create(termdRunningBuild.getRunningEnvironment().getJenkinsUrl()))
                    .uploadScript(commandAppender,
                            Paths.get(termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString(), "run.sh"));

            return termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString() + "/run.sh";
        });
    }

    protected CompletableFuture<TermdCommandBatchExecutionResult> invokeRemoteScript(TermdRunningBuild termdRunningBuild, String scriptPath) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Invoking script from path {}", termdRunningBuild.getRunningEnvironment().getId(), scriptPath);

            TermdCommandInvoker termdCommandInvoker = new TermdCommandInvoker(URI.create(termdRunningBuild.getRunningEnvironment().getJenkinsUrl()), termdRunningBuild.getRunningEnvironment().getWorkingDirectory());
            termdCommandInvoker.startSession();

            termdCommandInvoker.performCommand("sh " + scriptPath).join();

            return termdCommandInvoker.closeSession();
        });
    }

    protected TermdRunningBuild updateStatus(TermdRunningBuild termdRunningBuild, TermdCommandBatchExecutionResult commandBatchResult, Throwable throwable) {
        logger.debug("[{}] Command result {}", termdRunningBuild.getRunningEnvironment().getId(), commandBatchResult);
        logger.debug("[{}] Exception {}", termdRunningBuild.getRunningEnvironment().getId(), throwable);


        if(throwable != null) {
            termdRunningBuild.setBuildPromiseError((Exception) throwable);
        } else {
            AtomicReference<String> aggregatedLogs = new AtomicReference<>();
            try {
                aggregatedLogs.set(aggregateLogs(termdRunningBuild, commandBatchResult).get().toString());
            } catch (Exception e) {
                termdRunningBuild.setBuildPromiseError(e);
                return termdRunningBuild;
            }

            termdRunningBuild.setCompletedBuild(new CompletedBuild() {
                @Override
                public BuildDriverResult getBuildResult() throws BuildDriverException {
                    return new BuildDriverResult() {
                        @Override
                        public String getBuildLog() {
                            return aggregatedLogs.get();
                        }

                        @Override
                        public BuildDriverStatus getBuildDriverStatus() {
                            return commandBatchResult.isSuccessful() ? BuildDriverStatus.SUCCESS : BuildDriverStatus.FAILED;
                        }

                        @Override
                        public RunningEnvironment getRunningEnvironment() {
                            return termdRunningBuild.getRunningEnvironment();
                        }
                    };
                }

                @Override
                public RunningEnvironment getRunningEnvironment() {
                    return termdRunningBuild.getRunningEnvironment();
                }});
        }
        return termdRunningBuild;
    }

    protected CompletableFuture<StringBuffer> aggregateLogs(TermdRunningBuild termdRunningBuild, TermdCommandBatchExecutionResult allInvokedCommands) {
        logger.debug("[{}] Aggregating logs", termdRunningBuild.getRunningEnvironment().getId());

        TermdFileTranser transer = new TermdFileTranser();
        return CompletableFuture.supplyAsync(() -> allInvokedCommands.getCommandResults().stream()
                .map(invocationResult -> invocationResult.getLogsUri())
                .reduce(new StringBuffer(),
                        (stringBuffer, uri) -> transer.downloadFileToStringBuilder(stringBuffer, uri),
                        (builder1, builder2) -> builder1.append(builder2)));
    }

}
