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

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.NamedThreadFactory;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.termdbuilddriver.transfer.TermdFileTranser;
import org.jboss.pnc.termdbuilddriver.transfer.TransferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.buildagent.api.Status.COMPLETED;
import static org.jboss.pnc.buildagent.api.Status.FAILED;
import static org.jboss.pnc.buildagent.api.Status.INTERRUPTED;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver { //TODO rename class

    public static final String DRIVER_ID = "termd-build-driver";

    private static final Logger logger = LoggerFactory.getLogger(TermdBuildDriver.class);

    //connect to build agent on internal or on public address
    private boolean useInternalNetwork = true; //TODO configurable

    private ExecutorService executor;

    private Set<Consumer<StatusUpdateEvent>> statusUpdateConsumers = new HashSet<>();

    private Consumer<StatusUpdateEvent> onStatusUpdate = (status) -> {
        statusUpdateConsumers.forEach(consumer -> consumer.accept(status));
    };

    @Deprecated
    public TermdBuildDriver() {
    }

    @Inject
    public TermdBuildDriver(Configuration configuration) {
        int threadPoolSize = 12; //TODO configurable
        try {
            String executorThreadPoolSizeStr = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class))
                    .getBuilderThreadPoolSize();
            if (executorThreadPoolSizeStr != null) {
                threadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
            }
        } catch (ConfigurationParseException e) {
            logger.warn("Unable parse config. Using defaults.");
        }

        executor = Executors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory("termd-build-driver"));
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

        logger.info("[{}] Starting build for Build Execution Session {}", runningEnvironment.getId(), buildExecutionSession.getId());

        TermdRunningBuild termdRunningBuild = new TermdRunningBuild(
                runningEnvironment,
                buildExecutionSession.getBuildExecutionConfiguration(),
                onComplete,
                onError);

        DebugData debugData = runningEnvironment.getDebugData();
        String buildScript = prepareBuildScript(termdRunningBuild, debugData);

        uploadScript(termdRunningBuild, buildScript)
                .thenComposeAsync(scriptPath -> invokeRemoteScript(termdRunningBuild, scriptPath, debugData), executor)
                //no cancellation after this point ... collecting partial results
                .thenComposeAsync(status -> collectResults(termdRunningBuild, status), executor)
                .handle((completedBuild, exception) -> complete(termdRunningBuild, completedBuild, exception));

        return termdRunningBuild;
    }

    private CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, String command) {
        CompletableFuture<String> result = new CompletableFuture<>();

        Runnable task = () -> {
            try {
                if (termdRunningBuild.isCanceled()) {
                    logger.debug("Skipping script uploading (cancel flag) ...");
                    result.complete("");
                    return;
                }

                logger.debug("[{}] Uploading script ...", termdRunningBuild.getRunningEnvironment().getId());
                logger.debug("[{}] Full script:\n {}", termdRunningBuild.getRunningEnvironment().getId(), command);

                try {
                    new TermdFileTranser(URI.create(getBuildAgentUrl(termdRunningBuild))).uploadScript(command,
                            Paths.get(termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString(),
                                    "run.sh"));
                } catch (TransferException e) {
                    result.completeExceptionally(e);
                }

                String scriptPath =
                        termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString() + "/run.sh";
                result.complete(scriptPath);
            } catch (Throwable e ) {
                logger.warn("Caught unhandled exception.", e);
            }
        };

        Future<?> taskFuture = executor.submit(task);
        Runnable cancel = () -> {
            logger.info("Cancelling script upload ...");
            logger.debug("taskFuture.isDone: {}.", taskFuture.isDone());
            boolean canceled = taskFuture.cancel(true);
            if (canceled) { //if not canceled, it should be completed
                result.complete("");
            }
            logger.debug("taskFuture.isDone: {}; taskFuture.isCanceled: {}.", taskFuture.isDone(), taskFuture.isCancelled());
        };

        termdRunningBuild.setCancelHook(cancel);

        return result;
    }

    private CompletableFuture<org.jboss.pnc.buildagent.api.Status> invokeRemoteScript(
            TermdRunningBuild termdRunningBuild,
            String scriptPath,
            DebugData debugData) {

        CompletableFuture<org.jboss.pnc.buildagent.api.Status> invocation = new CompletableFuture<>();

        if (termdRunningBuild.isCanceled()) {
            logger.debug("Skipping remote script invocation (cancel flag) ...");
            invocation.complete(INTERRUPTED);
            return invocation;
        }
        logger.debug("Invoking remote script ...");

        Consumer<TaskStatusUpdateEvent> onStatusUpdate = (event) -> {
            final org.jboss.pnc.buildagent.api.Status newStatus;
            if (termdRunningBuild.isCanceled() && event.getNewStatus().equals(FAILED) ) {
                newStatus = INTERRUPTED; //TODO fix returned status and remove this workaround
            } else {
                newStatus = event.getNewStatus();
            }
            logger.debug("Driver received new status update {}.", newStatus);
            statusUpdateConsumers.forEach(consumer -> consumer.accept(new StatusUpdateEvent(termdRunningBuild, newStatus)));
            if (newStatus.isFinal()) {
                logger.debug("Script invocation completed with status {}.", newStatus);
                if (newStatus == org.jboss.pnc.buildagent.api.Status.FAILED && debugData.isEnableDebugOnFailure()) {
                    debugData.setDebugEnabled(true);
                    enableSsh(termdRunningBuild);
                }
                invocation.complete(newStatus);
            }
        };

        BuildAgentClient buildAgentClient = createBuildAgentClient(termdRunningBuild, invocation, onStatusUpdate);

        termdRunningBuild.setBuildAgentClient(buildAgentClient);

        try {
            String command = "sh " + scriptPath;
            logger.info("Invoking remote command {}.", command);
            buildAgentClient.executeCommand(command);
            logger.debug("Remote command invoked.");
        } catch (TimeoutException | BuildAgentClientException e) {
            invocation.completeExceptionally(new BuildDriverException("Cannot execute remote script.", e));
        }

        termdRunningBuild.setCancelHook(() -> {
            try {
                logger.info("Canceling running build {}.", termdRunningBuild.getName());
                buildAgentClient.executeNow('C' - 64); //send ctrl+C
            } catch (BuildAgentClientException e) {
                invocation.completeExceptionally(new BuildDriverException("Cannot cancel remote script.", e));
            }
        });

        return invocation;
    }

    public boolean addStatusUpdateConsumer(Consumer<StatusUpdateEvent> consumer) {
        return statusUpdateConsumers.add(consumer);
    }

    public boolean removeStatusUpdateConsumer(Consumer<StatusUpdateEvent> consumer) {
        return statusUpdateConsumers.remove(consumer);
    }

    private void enableSsh(TermdRunningBuild termd) {
        Optional<BuildAgentClient> maybeClient = termd.getBuildAgentClient();
        if (maybeClient.isPresent()) {
            BuildAgentClient client = maybeClient.get();
            try {
                client.executeCommand("/usr/local/bin/startSshd.sh");
            } catch (TimeoutException | BuildAgentClientException e) {
                logger.error("Failed to enable ssh access", e);
            }
        } else {
            logger.error("No build agent client present to enable ssh access");
        }
    }

    private BuildAgentClient createBuildAgentClient(TermdRunningBuild termdRunningBuild, CompletableFuture<Status> invocation, Consumer<TaskStatusUpdateEvent> onStatusUpdate) {
        BuildAgentClient buildAgentClient = null;
        try {
            String terminalUrl = getBuildAgentUrl(termdRunningBuild);
            buildAgentClient = new BuildAgentClient(terminalUrl.replace("http://", "ws://"), Optional.empty(), onStatusUpdate, "");
        } catch (Exception e) {
            invocation.completeExceptionally(new BuildDriverException("Cannot connect build agent client.", e));
        }
        return buildAgentClient;
    }

    private CompletableFuture<CompletedBuild> collectResults(TermdRunningBuild termdRunningBuild, org.jboss.pnc.buildagent.api.Status completionStatus) {
        CompletableFuture<CompletedBuild> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            logger.info("Collecting results ...");

            if (termdRunningBuild.getBuildAgentClient().isPresent()) {
                BuildAgentClient buildAgentClient = termdRunningBuild.getBuildAgentClient().get();
                try { //TODO move to #complete to make sure it's closed every-time
                    buildAgentClient.close();
                } catch (IOException e) {
                    future.completeExceptionally(new BuildDriverException("Cannot close build agent connections.", e));
                }
            } else {
                //cancel has been requested
            }

            TermdFileTranser transfer = new TermdFileTranser();
            StringBuffer stringBuffer = new StringBuffer();

            String logsDirectory = termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toString();

            try {
                URI logsUri = new URI(getBuildAgentUrl(termdRunningBuild)).resolve("servlet/download" + logsDirectory + "/console.log");
                transfer.downloadFileToStringBuilder(stringBuffer, logsUri);
            } catch (URISyntaxException e) {
                future.completeExceptionally(new BuildDriverException("Cannot construct logs uri.", e));
            } catch (TransferException e) {
                future.completeExceptionally(new BuildDriverException("Cannot transfer file.", e));
            }

            CompletedBuild completedBuild = new DefaultCompletedBuild(
                    termdRunningBuild.getRunningEnvironment(), getBuildStatus(completionStatus), stringBuffer.toString());

            future.complete(completedBuild);
        }, executor);
        return future;
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

    private Void complete(TermdRunningBuild termdRunningBuild, CompletedBuild completedBuild, Throwable throwable) {
        logger.debug("[{}] Command result {}", termdRunningBuild.getRunningEnvironment().getId(), completedBuild);
        if(throwable != null) {
            logger.warn("[{}] Exception {}", termdRunningBuild.getRunningEnvironment().getId(), throwable);
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

    private String getBuildAgentUrl(TermdRunningBuild termdRunningBuild) {
        RunningEnvironment runningEnvironment = termdRunningBuild.getRunningEnvironment();
        if (useInternalNetwork) {
            return runningEnvironment.getInternalBuildAgentUrl();
        } else {
            return runningEnvironment.getBuildAgentUrl();
        }
    }
}
