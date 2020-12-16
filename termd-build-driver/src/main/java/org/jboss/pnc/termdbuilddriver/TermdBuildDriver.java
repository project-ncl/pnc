/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.termdbuilddriver.transfer.ClientFileTransfer;
import org.jboss.pnc.termdbuilddriver.transfer.FileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.jboss.pnc.buildagent.api.Status.COMPLETED;
import static org.jboss.pnc.buildagent.api.Status.FAILED;
import static org.jboss.pnc.buildagent.api.Status.INTERRUPTED;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver { // TODO rename class

    public static final String DRIVER_ID = "termd-build-driver";
    private static final int MAX_LOG_SIZE = 90 * 1024 * 1024; // 90MB

    private static final Logger logger = LoggerFactory.getLogger(TermdBuildDriver.class);

    private final ClientFactory clientFactory;
    private Optional<Integer> fileTransferReadTimeout = Optional.empty();

    // connect to build agent on internal or on public address
    private boolean useInternalNetwork = true; // TODO configurable

    private boolean httpCallbackMode = true;

    private Integer internalCancelTimeoutMillis;
    private long livenessProbeFrequency;
    private long livenessFailTimeout;

    private ExecutorService executor;

    private ScheduledExecutorService scheduledExecutorService;

    @Deprecated
    public TermdBuildDriver() {
        clientFactory = null;
    }

    @Inject
    public TermdBuildDriver(
            SystemConfig systemConfig,
            TermdBuildDriverModuleConfig termdBuildDriverModuleConfig,
            ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        int threadPoolSize = 12;
        String executorThreadPoolSizeStr = systemConfig.getBuilderThreadPoolSize();
        if (executorThreadPoolSizeStr != null) {
            threadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
        }
        internalCancelTimeoutMillis = termdBuildDriverModuleConfig.getInternalCancelTimeoutMillis();
        livenessProbeFrequency = termdBuildDriverModuleConfig.getLivenessProbeFrequencyMillis();
        livenessFailTimeout = termdBuildDriverModuleConfig.getLivenessFailTimeoutMillis();
        httpCallbackMode = termdBuildDriverModuleConfig.isHttpCallbackMode();

        executor = MDCExecutors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory("termd-build-driver"));
        scheduledExecutorService = MDCExecutors
                .newScheduledThreadPool(1, new NamedThreadFactory("build-driver-liveness-cancel"));

        fileTransferReadTimeout = Optional.ofNullable(termdBuildDriverModuleConfig.getFileTransferReadTimeout());
    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public RunningBuild startProjectBuild(
            BuildExecutionSession buildExecutionSession,
            RunningEnvironment runningEnvironment,
            Consumer<CompletedBuild> onComplete,
            Consumer<Throwable> onError) throws BuildDriverException {
        return startProjectBuild(buildExecutionSession, runningEnvironment, onComplete, onError, Optional.empty());
    }

    public RunningBuild startProjectBuild(
            BuildExecutionSession buildExecutionSession,
            RunningEnvironment runningEnvironment,
            Consumer<CompletedBuild> onComplete,
            Consumer<Throwable> onError,
            Optional<Consumer<Status>> onStatusUpdate) throws BuildDriverException {

        logger.info(
                "[{}] Starting build for Build Execution Session {}",
                runningEnvironment.getId(),
                buildExecutionSession.getId());

        TermdRunningBuild termdRunningBuild = new TermdRunningBuild(
                runningEnvironment,
                buildExecutionSession.getBuildExecutionConfiguration(),
                onComplete,
                onError);

        DebugData debugData = runningEnvironment.getDebugData();
        String buildScript = prepareBuildScript(termdRunningBuild, debugData);

        if (!termdRunningBuild.isCanceled()) {
            String terminalUrl = getBuildAgentUrl(runningEnvironment);
            final RemoteInvocation remoteInvocation = new RemoteInvocation(
                    clientFactory,
                    terminalUrl,
                    onStatusUpdate,
                    httpCallbackMode,
                    buildExecutionSession.getId().toString(),
                    buildExecutionSession.getAccessToken());
            buildExecutionSession.setBuildStatusUpdateConsumer(remoteInvocation.getClientStatusUpdateConsumer());

            FileTransfer fileTransfer = new ClientFileTransfer(remoteInvocation.getBuildAgentClient(), MAX_LOG_SIZE);
            fileTransferReadTimeout.ifPresent(fileTransfer::setReadTimeout);

            CompletableFuture<Void> prepareBuildFuture = CompletableFuture.supplyAsync(() -> {
                logger.debug("Uploading build script to build environment ...");
                return uploadTask(termdRunningBuild.getRunningEnvironment(), buildScript, fileTransfer);
            }, executor).thenApplyAsync(scriptPath -> {
                logger.debug("Setting the script path ...");
                remoteInvocation.setScriptPath(scriptPath);
                return null;
            }, executor).thenRunAsync(() -> {
                logger.debug("Invoking remote script ...");
                invokeRemoteScript(remoteInvocation);
            }, executor);

            CompletableFuture<RemoteInvocationCompletion> buildLivenessFuture = prepareBuildFuture
                    .thenComposeAsync(nul -> {
                        logger.debug("Starting liveness monitor ...");
                        return monitorBuildLiveness(remoteInvocation);
                    }, executor);

            CompletableFuture<RemoteInvocationCompletion> buildCompletionFuture = prepareBuildFuture
                    .thenComposeAsync(nul -> {
                        logger.debug("Waiting fo remote script to complete...");
                        return remoteInvocation.getCompletionNotifier();
                    }, executor);

            CompletableFuture<RemoteInvocationCompletion> optionallyEnableDebug = buildCompletionFuture
                    .thenApplyAsync(remoteInvocationCompletion -> {
                        Status status = remoteInvocationCompletion.getStatus();
                        if (status.isFinal()) {
                            logger.debug("Script completionNotifier completed with status {}.", status);
                            if (status == FAILED && debugData.isEnableDebugOnFailure()) {
                                debugData.setDebugEnabled(true);
                                remoteInvocation.enableSsh();
                            }
                        }
                        return remoteInvocationCompletion;
                    }, executor);

            CompletableFuture<Object> buildFuture = CompletableFuture.anyOf(buildLivenessFuture, optionallyEnableDebug);

            buildFuture.handle((result, exception) -> {
                RemoteInvocationCompletion completion;
                if (result != null) {
                    // both of combined futures return the same type
                    RemoteInvocationCompletion remoteInvocationCompletion = (RemoteInvocationCompletion) result;
                    if (remoteInvocationCompletion.getException() != null) {
                        logger.warn("Completing build execution.", remoteInvocationCompletion.getException());
                    } else {
                        logger.debug("Completing build execution. Status: {};", remoteInvocationCompletion.getStatus());
                    }
                    completion = remoteInvocationCompletion;
                } else if (exception != null
                        && exception.getCause() instanceof java.util.concurrent.CancellationException) {
                    // canceled in non build operation (completableFuture cancel), non graceful completion
                    logger.warn("Completing build execution. Cancelled;");
                    completion = new RemoteInvocationCompletion(INTERRUPTED, Optional.empty());
                } else {
                    logger.warn("Completing build execution. System error.", exception);
                    completion = new RemoteInvocationCompletion(new BuildDriverException("System error.", exception));
                }

                termdRunningBuild.setCancelHook(null);
                remoteInvocation.close();

                complete(termdRunningBuild, completion, fileTransfer);
                return null;
            });

            termdRunningBuild.setCancelHook(() -> {
                remoteInvocation.cancel(); // try to cancel remote execution
                ScheduledFuture<?> forceCancel_ = scheduledExecutorService.schedule(() -> {
                    logger.debug("Force cancelling build ...");
                    prepareBuildFuture.cancel(true);
                }, internalCancelTimeoutMillis, TimeUnit.MILLISECONDS);
                remoteInvocation.addPreClose(() -> forceCancel_.cancel(false));
            });
        } else {
            logger.debug("Skipping script uploading (cancel flag) ...");
        }
        return termdRunningBuild;
    }

    private CompletionStage<RemoteInvocationCompletion> monitorBuildLiveness(RemoteInvocation remoteInvocation) {

        CompletableFuture completableFuture = new CompletableFuture();

        AtomicReference<Long> lastSuccess = new AtomicReference<>();
        lastSuccess.set(System.currentTimeMillis());

        Runnable isAlive = () -> {
            if (remoteInvocation.isAlive()) {
                lastSuccess.set(System.currentTimeMillis());
            } else {
                Long last = lastSuccess.get();
                if (System.currentTimeMillis() - last > livenessFailTimeout) {
                    logger.warn("Liveness probe failed.");
                    RemoteInvocationCompletion completion = new RemoteInvocationCompletion(
                            new BuildDriverException("Build Agent has gone away."));
                    completableFuture.complete(completion);
                }
            }
        };
        ScheduledFuture<?> livenessMonitor = scheduledExecutorService
                .scheduleWithFixedDelay(isAlive, livenessProbeFrequency, livenessProbeFrequency, TimeUnit.MILLISECONDS);
        remoteInvocation.addPreClose(() -> livenessMonitor.cancel(false));
        return completableFuture;
    }

    private String uploadTask(RunningEnvironment runningEnvironment, String command, FileTransfer fileTransfer) {
        try {
            logger.debug("Full script:\n {}", command);
            fileTransfer.uploadScript(
                    command,
                    Paths.get(runningEnvironment.getWorkingDirectory().toAbsolutePath().toString(), "run.sh"));

            String scriptPath = runningEnvironment.getWorkingDirectory().toAbsolutePath().toString() + "/run.sh";
            return scriptPath;
        } catch (Throwable e) {
            logger.warn("Caught unhandled exception.", e);
            throw new RuntimeException("Unable to upload script.", e);
        }
    }

    private Void invokeRemoteScript(RemoteInvocation remoteInvocation) {
        remoteInvocation.invoke();
        return null;
    }

    private CompletedBuild collectResults(
            RunningEnvironment runningEnvironment,
            RemoteInvocationCompletion remoteInvocationCompletion,
            FileTransfer transfer) {
        logger.info("Collecting results ...");
        try {
            StringBuffer stringBuffer = new StringBuffer();

            String logsDirectory = runningEnvironment.getWorkingDirectory().toString();

            transfer.downloadFileToStringBuilder(stringBuffer, logsDirectory + "/console.log");

            String prependMessage = "";
            BuildStatus buildStatus = getBuildStatus(remoteInvocationCompletion.getStatus());

            if (!transfer.isFullyDownloaded()) {
                prependMessage = "----- build log was cut, storing only last part -----\n";
                if (buildStatus.completedSuccessfully()) {
                    prependMessage = "----- build has completed successfully but it is marked as failed due to log overflow. Max log size is "
                            + MAX_LOG_SIZE + " -----\n";
                    buildStatus = BuildStatus.FAILED;
                }
            }

            return new DefaultCompletedBuild(
                    runningEnvironment,
                    buildStatus,
                    remoteInvocationCompletion.getOutputChecksum(),
                    prependMessage + stringBuffer.toString());
        } catch (Throwable e) {
            throw new RuntimeException("Cannot collect results.", e);
        }
    }

    private BuildStatus getBuildStatus(Status completionStatus) {
        if (COMPLETED.equals(completionStatus)) {
            return BuildStatus.SUCCESS;
        } else if (INTERRUPTED.equals(completionStatus)) {
            return BuildStatus.CANCELLED;
        } else {
            return BuildStatus.FAILED;
        }
    }

    private void complete(
            TermdRunningBuild termdRunningBuild,
            RemoteInvocationCompletion completion,
            FileTransfer fileTransfer) {

        if (completion.getException() != null) {
            logger.warn("Completed with exception.", completion.getException());
            termdRunningBuild.setBuildError(completion.getException());
            return;
        }

        CompletedBuild completedBuild = collectResults(
                termdRunningBuild.getRunningEnvironment(),
                completion,
                fileTransfer);
        logger.debug("Command result {}", completedBuild);

        if (completedBuild == null) {
            termdRunningBuild.setBuildError(new BuildDriverException("Completed build should not be null."));
        } else {
            termdRunningBuild.setCompletedBuild(completedBuild);
        }
    }

    private String prepareBuildScript(TermdRunningBuild termdRunningBuild, DebugData debugData) {
        StringBuilder buildScript = new StringBuilder();

        String workingDirectory = termdRunningBuild.getRunningEnvironment()
                .getWorkingDirectory()
                .toAbsolutePath()
                .toString();
        String name = termdRunningBuild.getName();
        if (debugData.isEnableDebugOnFailure()) {
            String projectDirectory = (workingDirectory.endsWith("/") ? workingDirectory : workingDirectory + "/")
                    + name;
            String enterProjectDirCommand = "echo 'cd " + projectDirectory + "' >> \"${HOME}/.bashrc\"";
            buildScript.append(enterProjectDirCommand).append("\n");
        }

        buildScript.append("set -xe" + "\n");
        buildScript.append("cd " + workingDirectory + "\n");

        buildScript.append("git clone " + termdRunningBuild.getScmRepoURL() + " " + name + "\n");
        buildScript.append("cd " + name + "\n");
        buildScript.append("git reset --hard " + termdRunningBuild.getScmRevision() + "\n");

        buildScript.append(termdRunningBuild.getBuildScript() + "\n");

        return buildScript.toString();
    }

    private String getBuildAgentUrl(RunningEnvironment runningEnvironment) {
        if (useInternalNetwork) {
            return runningEnvironment.getInternalBuildAgentUrl();
        } else {
            return runningEnvironment.getBuildAgentUrl();
        }
    }
}
