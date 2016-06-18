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

import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.buildagent.client.Client;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.termdbuilddriver.transfer.TermdFileTranser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.jboss.pnc.buildagent.api.Status.COMPLETED;
import static org.jboss.pnc.buildagent.api.Status.INTERRUPTED;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver { //TODO rename class

    public static final String DRIVER_ID = "termd-build-driver";

    private static final Logger logger = LoggerFactory.getLogger(TermdBuildDriver.class);

    //connect to build agent on internal or on public address
    private boolean useInternalNetwork = true; //TODO configurable

    private ExecutorService executor;

    @Deprecated
    public TermdBuildDriver() {
    }

    @Inject
    public TermdBuildDriver(Configuration configuration) {
        int executorThreadPoolSize = 12; //TODO configurable
        try {
            String executorThreadPoolSizeStr = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class))
                    .getBuilderThreadPoolSize();
            if (executorThreadPoolSizeStr != null) {
                executorThreadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
            }
        } catch (ConfigurationParseException e) {
            logger.warn("Unable parse config. Using defaults.");
        }

        executor = Executors.newFixedThreadPool(executorThreadPoolSize);
    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public RunningBuild startProjectBuild(BuildExecutionSession buildExecutionSession, RunningEnvironment runningEnvironment)
            throws BuildDriverException {

        logger.info("[{}] Starting build for Build Execution Session {}", runningEnvironment.getId(), buildExecutionSession.getId());

        TermdRunningBuild termdRunningBuild = new TermdRunningBuild(runningEnvironment, buildExecutionSession.getBuildExecutionConfiguration());


        String buildScript = prepareBuildScript(termdRunningBuild);

        uploadScript(termdRunningBuild, buildScript)
                .thenComposeAsync(scriptPath -> invokeRemoteScript(termdRunningBuild, scriptPath), executor)
                .thenComposeAsync(status -> collectResults(termdRunningBuild, status), executor)
                .handle((completedBuild, exception) -> complete(termdRunningBuild, completedBuild, exception));

        return termdRunningBuild;
    }

    private Void complete(TermdRunningBuild termdRunningBuild, CompletedBuild completedBuild, Throwable throwable) {
        logger.debug("[{}] Command result {}", termdRunningBuild.getRunningEnvironment().getId(), completedBuild);
        if(throwable != null) {
            logger.warn("[{}] Exception {}", termdRunningBuild.getRunningEnvironment().getId(), throwable);
            termdRunningBuild.setBuildPromiseError((Exception) throwable);
        } else {
            termdRunningBuild.setCompletedBuild(completedBuild);
        }
        return null;
    }

    private CompletableFuture<org.jboss.pnc.buildagent.api.Status> invokeRemoteScript(TermdRunningBuild termdRunningBuild, String scriptPath) {
        CompletableFuture invocation = new CompletableFuture();

        Consumer<TaskStatusUpdateEvent> onStatusUpdate = (event) -> {
            org.jboss.pnc.buildagent.api.Status newStatus = event.getNewStatus();
            if (newStatus.isFinal()) {
                invocation.complete(newStatus);
            }
        };

        String terminalUrl = getBuildAgentUrl(termdRunningBuild) + Client.WEB_SOCKET_TERMINAL_PATH; //TODO make the client compose the paths
        String statusUpdatesUrl = getBuildAgentUrl(termdRunningBuild) + Client.WEB_SOCKET_LISTENER_PATH;

        BuildAgentClient buildAgentClient = null;
        try {
            buildAgentClient = new BuildAgentClient(terminalUrl, statusUpdatesUrl, Optional.empty(), onStatusUpdate, "");
        } catch (Exception e) {
            invocation.completeExceptionally(new BuildDriverException("Cannot connect build agent client.", e));
        }

        termdRunningBuild.setBuildAgentClient(buildAgentClient);

        try {
            buildAgentClient.executeCommand("sh " + scriptPath);
        } catch (TimeoutException | BuildAgentClientException e) {
            invocation.completeExceptionally(new BuildDriverException("Cannot execute remote script.", e));
        }
        return invocation;
    }

    private CompletableFuture<CompletedBuild> collectResults(TermdRunningBuild termdRunningBuild, org.jboss.pnc.buildagent.api.Status completionStatus) {
        return CompletableFuture.supplyAsync(() -> {
            if (termdRunningBuild.getBuildAgentClient().isPresent()) {
                BuildAgentClient buildAgentClient = termdRunningBuild.getBuildAgentClient().get();
                try {
                    buildAgentClient.close();
                } catch (IOException e) {
                    throw new RuntimeException("Cannot close build agent connections.", e);
                }
            } else {
                throw new RuntimeException("Build Agent Client is not available.");
            }

            CompletedBuild completedBuild = new CompletedBuild() { //TODO use concrete class
                @Override
                public BuildDriverResult getBuildResult() throws BuildDriverException {
                    return new BuildDriverResult() {
                        @Override
                        public String getBuildLog() throws BuildDriverException {
                            TermdFileTranser transfer = new TermdFileTranser();
                            StringBuffer stringBuffer = new StringBuffer();

                            String logsDirectory = termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toString();

                            try {
                                URI logsUri = new URI(getBuildAgentUrl(termdRunningBuild)).resolve("servlet/download" + logsDirectory + "/console.log");
                                transfer.downloadFileToStringBuilder(stringBuffer, logsUri);
                            } catch (URISyntaxException e) {
                                throw new RuntimeException("Cannot construct logs uri.", e);
                            }
                            return stringBuffer.toString();
                        }

                        @Override
                        public BuildDriverStatus getBuildDriverStatus() {
                            if (COMPLETED.equals(completionStatus)) {
                                return BuildDriverStatus.SUCCESS;
                            } else if (INTERRUPTED.equals(completionStatus)) {
                                return BuildDriverStatus.CANCELLED;
                            } else {
                                return BuildDriverStatus.FAILED;
                            }
                        }
                    };
                }

                @Override
                public RunningEnvironment getRunningEnvironment() {
                    return termdRunningBuild.getRunningEnvironment();
                }
            };

            return completedBuild;
        });
    }

    private String prepareBuildScript(TermdRunningBuild termdRunningBuild) {
        StringBuilder buildScript = new StringBuilder();
        buildScript.append("set -x" + "\n");
        buildScript.append("cd " + termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString() + "\n");

        buildScript.append("git clone " + termdRunningBuild.getScmRepoURL() + " " + termdRunningBuild.getName() + "\n");
        buildScript.append("cd " + termdRunningBuild.getName() + "\n");
        buildScript.append("git reset --hard " + termdRunningBuild.getScmRevision() + "\n");

        buildScript.append(termdRunningBuild.getBuildScript() + "\n");

        return buildScript.toString();
    }

    private CompletableFuture<String> uploadScript(TermdRunningBuild termdRunningBuild, String command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("[{}] Uploading script", termdRunningBuild.getRunningEnvironment().getId());
            logger.debug("[{}] Full script:\n {}", termdRunningBuild.getRunningEnvironment().getId(), command);

            new TermdFileTranser(URI.create(getBuildAgentUrl(termdRunningBuild)))
                    .uploadScript(command,
                            Paths.get(termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString(), "run.sh"));

            return termdRunningBuild.getRunningEnvironment().getWorkingDirectory().toAbsolutePath().toString() + "/run.sh";
        });
    }

    private String getBuildAgentUrl(TermdRunningBuild termdRunningBuild) {
        if (useInternalNetwork) {
            return termdRunningBuild.getRunningEnvironment().getInternalBuildAgentUrl();
        } else {
            return termdRunningBuild.getRunningEnvironment().getBuildAgentUrl();
        }
    }
}
