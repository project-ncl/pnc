/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.buildagent.api.ResponseMode;
import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleconfig.TermdBuildDriverModuleConfig;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.termdbuilddriver.transfer.TermdFileTranser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.jboss.pnc.buildagent.api.Status.COMPLETED;
import static org.jboss.pnc.buildagent.api.Status.FAILED;
import static org.jboss.pnc.buildagent.api.Status.INTERRUPTED;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver { //TODO rename class

    public static final String DRIVER_ID = "termd-build-driver";
    private static final int MAX_LOG_SIZE = 90 * 1024 * 1024; //90MB

    private static final Logger logger = LoggerFactory.getLogger(TermdBuildDriver.class);

    //connect to build agent on internal or on public address
    private boolean useInternalNetwork = true; //TODO configurable

    private Integer internalCancelTimeoutMillis;

    private ExecutorService executor;

    private ScheduledExecutorService scheduledExecutorService;

    private Set<Consumer<StatusUpdateEvent>> statusUpdateConsumers = new HashSet<>();

    @Deprecated
    public TermdBuildDriver() {
    }

    @Inject
    public TermdBuildDriver(SystemConfig systemConfig, TermdBuildDriverModuleConfig termdBuildDriverModuleConfig) {
        int threadPoolSize = 12;
        String executorThreadPoolSizeStr = systemConfig.getBuilderThreadPoolSize();
        if (executorThreadPoolSizeStr != null) {
            threadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
        }
        internalCancelTimeoutMillis = termdBuildDriverModuleConfig.getInternalCancelTimeoutMillis();

        executor = MDCExecutors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory("termd-build-driver"));
        scheduledExecutorService = MDCExecutors.newScheduledThreadPool(1, new NamedThreadFactory("termd-build-driver-cancel"));
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
            Consumer<Throwable> onError)
            throws BuildDriverException {

        logger.info("[{}] Starting build for Build Execution Session {}",
                runningEnvironment.getId(),
                buildExecutionSession.getId());

        String runningName = buildExecutionSession.getBuildExecutionConfiguration().getName();

        TermdRunningBuild termdRunningBuild = new TermdRunningBuild(
                runningEnvironment,
                buildExecutionSession.getBuildExecutionConfiguration(),
                onComplete,
                onError);

        DebugData debugData = runningEnvironment.getDebugData();
        String buildScript = prepareBuildScript(termdRunningBuild, debugData);

        if (!termdRunningBuild.isCanceled()) {

            final RemoteInvocation remoteInvocation = new RemoteInvocation();

            Consumer<TaskStatusUpdateEvent> onStatusUpdate = (event) -> {
                final org.jboss.pnc.buildagent.api.Status newStatus;
                if (remoteInvocation.isCanceled() && event.getNewStatus().equals(FAILED)) {
                    newStatus = INTERRUPTED; //TODO fix returned status and remove this workaround
                } else {
                    newStatus = event.getNewStatus();
                }
                logger.debug("Driver received new status update {}.", newStatus);
                statusUpdateConsumers.forEach(consumer -> consumer.accept(new StatusUpdateEvent(newStatus)));
                if (newStatus.isFinal()) {
                    logger.debug("Script completionNotifier completed with status {}.", newStatus);
                    if (newStatus == org.jboss.pnc.buildagent.api.Status.FAILED && debugData.isEnableDebugOnFailure()) {
                        debugData.setDebugEnabled(true);
                        enableSsh(Optional.ofNullable(remoteInvocation.getBuildAgentClient()));
                    }
                    remoteInvocation.notifyCompleted(newStatus);
                }
            };

            CompletableFuture<String> uploadFuture = CompletableFuture.supplyAsync(uploadTask(termdRunningBuild.getRunningEnvironment(),
                    buildScript), executor);
            CompletableFuture<Void> setClientFuture = uploadFuture.thenApplyAsync(scriptPath ->
                    createBuildAgentClient(remoteInvocation,
                            termdRunningBuild.getRunningEnvironment(),
                            scriptPath,
                            onStatusUpdate), executor);
            CompletableFuture<Void> invokeFuture = setClientFuture
                    .thenRunAsync(() -> invokeRemoteScript(remoteInvocation), executor);

            CompletableFuture<org.jboss.pnc.buildagent.api.Status> buildCompletedFuture = invokeFuture.thenComposeAsync(nul -> remoteInvocation.getCompletionNotifier(), executor);

            AtomicReference<ScheduledFuture> forceCancel = new AtomicReference<>();
            termdRunningBuild.setCancelHook(() -> {
                uploadFuture.cancel(true);
                setClientFuture.cancel(true);
                invokeFuture.cancel(false);
                if (remoteInvocation.getBuildAgentClient() != null) {
                    remoteInvocation.cancel(runningName);
                }

                ScheduledFuture<?> forceCancel_ = scheduledExecutorService.schedule(
                        () -> {
                            logger.debug("Forcing cancel ...");
                            remoteInvocation.notifyCompleted(Status.INTERRUPTED);
                        }, internalCancelTimeoutMillis, TimeUnit.MILLISECONDS);
                forceCancel.set(forceCancel_);
            });

            buildCompletedFuture.handleAsync((status, exception) -> {
                logger.debug("Completing build execution {}. Status: {}; exception: {}.", termdRunningBuild.getName(), status, exception);
                ScheduledFuture forceCancel_ = forceCancel.get();
                if (forceCancel_ != null) {
                    forceCancel_.cancel(true);
                }
                termdRunningBuild.setCancelHook(null);
                remoteInvocation.close();
                return complete(termdRunningBuild, status, exception);
            }, executor);

        } else {
            logger.debug("Skipping script uploading (cancel flag) ...");
        }
        return termdRunningBuild;
    }

    private Supplier<String> uploadTask(RunningEnvironment runningEnvironment, String command) {
        return () -> {
            try {
                logger.debug("[{}] Uploading script ...", runningEnvironment.getId());
                logger.debug("[{}] Full script:\n {}", runningEnvironment.getId(), command);

                new TermdFileTranser(URI.create(getBuildAgentUrl(runningEnvironment)), MAX_LOG_SIZE).uploadScript(command,
                        Paths.get(runningEnvironment.getWorkingDirectory().toAbsolutePath().toString(),
                                "run.sh"));

                String scriptPath =
                        runningEnvironment.getWorkingDirectory().toAbsolutePath().toString() + "/run.sh";
                return scriptPath;
            } catch (Throwable e ) {
                logger.warn("Caught unhandled exception.", e);
                throw new RuntimeException("Unable to upload script.", e);
            }
        };
    }

    private Void invokeRemoteScript(RemoteInvocation remoteInvocation) {
        remoteInvocation.invoke();
        return null;
    }

    public boolean addStatusUpdateConsumer(Consumer<StatusUpdateEvent> consumer) {
        return statusUpdateConsumers.add(consumer);
    }

    public boolean removeStatusUpdateConsumer(Consumer<StatusUpdateEvent> consumer) {
        return statusUpdateConsumers.remove(consumer);
    }

    private void enableSsh(Optional<BuildAgentClient> maybeClient) {
        if (maybeClient.isPresent()) {
            BuildAgentClient client = maybeClient.get();
            try {
                client.executeCommand("/usr/local/bin/startSshd.sh");
            } catch (BuildAgentClientException e) {
                logger.error("Failed to enable ssh access", e);
            }
        } else {
            logger.error("No build agent client present to enable ssh access");
        }
    }

    private Void createBuildAgentClient(
            RemoteInvocation remoteInvocation,
            RunningEnvironment runningEnvironment,
            String scriptPath,
            Consumer<TaskStatusUpdateEvent> onStatusUpdate) {
        try {
            BuildAgentClient buildAgentClient = null;
            String terminalUrl = getBuildAgentUrl(runningEnvironment);
            buildAgentClient = new BuildAgentClient(
                    terminalUrl.replace("http://", "ws://"),
                    Optional.empty(),
                    onStatusUpdate,
                    "",
                    ResponseMode.SILENT,
                    false);
            remoteInvocation.setScriptPath(scriptPath);
            remoteInvocation.setBuildAgentClient(buildAgentClient);
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create BuildAgentClient.", e);
        }
    }

    private CompletedBuild collectResults(RunningEnvironment runningEnvironment, org.jboss.pnc.buildagent.api.Status completionStatus) {
        logger.info("Collecting results ...");
        try {
            TermdFileTranser transfer = new TermdFileTranser(MAX_LOG_SIZE);
            StringBuffer stringBuffer = new StringBuffer();

            String logsDirectory = runningEnvironment.getWorkingDirectory().toString();

            URI logsUri = new URI(getBuildAgentUrl(runningEnvironment)).resolve(
                    "servlet/download" + logsDirectory + "/console.log");
            transfer.downloadFileToStringBuilder(stringBuffer, logsUri);

            String prependMessage = "";
            BuildStatus buildStatus = getBuildStatus(completionStatus);

            if (!transfer.isFullyDownloaded()) {
                prependMessage = "----- build log was cut, storing only last part -----\n";
                if (buildStatus.completedSuccessfully()) {
                    prependMessage = "----- build has completed successfully but it is marked as failed due to log overflow. Max log size is " + MAX_LOG_SIZE + " -----\n";
                    buildStatus = BuildStatus.FAILED;
                }
            }

            return new DefaultCompletedBuild(
                    runningEnvironment, buildStatus, prependMessage + stringBuffer.toString());
        } catch (Exception e) {
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

    private Void complete(TermdRunningBuild termdRunningBuild, Status status, Throwable throwable) {
        boolean isCancelled = INTERRUPTED.equals(status); //canceled while build is running
        if(throwable != null) {
            isCancelled = CancellationException.class.equals(throwable.getCause().getClass()); //canceled in non build operation (completableFuture cancel)
            if (isCancelled) {
                status = INTERRUPTED;
            }
        }

        CompletedBuild completedBuild = collectResults(termdRunningBuild.getRunningEnvironment(), status);
        logger.debug("[{}] Command result {}", termdRunningBuild.getRunningEnvironment().getId(), completedBuild);

        if(throwable != null && !isCancelled) {
            logger.warn("[{}] Completed with exception {}", termdRunningBuild.getRunningEnvironment().getId(), throwable);
            termdRunningBuild.setBuildError((Exception) throwable);
        } else if(completedBuild == null ) {
            termdRunningBuild.setBuildError(new BuildDriverException("Completed build should not be null."));
        } else {
            termdRunningBuild.setCompletedBuild(completedBuild);
        }
        return null;
    }

    private String prepareBuildScript(TermdRunningBuild termdRunningBuild, DebugData debugData) {
        StringBuilder buildScript = new StringBuilder();

        String workingDirectory = termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString();
        String name = termdRunningBuild.getName();
        if (debugData.isEnableDebugOnFailure()) {
            String projectDirectory = (workingDirectory.endsWith("/") ? workingDirectory : workingDirectory + "/") + name;
            String enterProjectDirCommand = "echo 'cd " + projectDirectory + "' >> /home/worker/.bashrc";
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
