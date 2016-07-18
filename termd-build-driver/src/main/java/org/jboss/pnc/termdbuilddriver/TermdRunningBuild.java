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

import org.jboss.pnc.buildagent.client.BuildAgentClient;
import org.jboss.pnc.buildagent.client.BuildAgentClientException;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Consumer;

public class TermdRunningBuild implements RunningBuild {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final RunningEnvironment runningEnvironment;
    private final BuildExecutionConfiguration buildExecutionConfiguration;

    private BuildAgentClient buildAgentClient;
    private Consumer<CompletedBuild> onComplete;
    private Consumer<Throwable> onError;

    private StringBuilder buildLogFooter = new StringBuilder();

    public TermdRunningBuild(RunningEnvironment runningEnvironment, BuildExecutionConfiguration buildExecutionConfiguration, Consumer<CompletedBuild> onComplete, Consumer<Throwable> onError) {
        this.runningEnvironment = runningEnvironment;
        this.buildExecutionConfiguration = buildExecutionConfiguration;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    public void setCompletedBuild(CompletedBuild completedBuild) {
        logger.debug("[{}] Setting completed build {}", runningEnvironment.getId(), completedBuild);
        onComplete.accept(completedBuild);
    }

    public void setBuildError(Exception error) {
        onError.accept(error);
    }

    @Override
    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }

    @Override
    public void cancel() throws BuildDriverException {
        //TODO cancel at any step (if we are not in the running execution the execution is not canceled)
        try {
            if (buildAgentClient != null) {
                buildAgentClient.executeNow('C' - 64); //send ctrl+C
            } else {
                logger.warn("Cannot cancel build at his point. It loks like there is no running build. TBD.");
            }
        } catch (BuildAgentClientException e) {
            throw new BuildDriverException("Cannot cancel the execution.", e);
        }
    }

    public String getBuildScript() {
        return buildExecutionConfiguration.getBuildScript();
    }

    public String getName() {
        return buildExecutionConfiguration.getName();
    }

    public String getScmRepoURL() { //TODO determine this before calling the execution
        if (buildExecutionConfiguration.getScmMirrorRepoURL() != null) {
            return buildExecutionConfiguration.getScmMirrorRepoURL();
        } else {
            return buildExecutionConfiguration.getScmRepoURL();
        }
    }

    public String getScmRevision() { //TODO determine this before calling the execution
        if (buildExecutionConfiguration.getScmMirrorRevision() != null) {
            return buildExecutionConfiguration.getScmMirrorRevision();
        } else {
            return buildExecutionConfiguration.getScmRevision();
        }
    }

    public void setBuildAgentClient(BuildAgentClient buildAgentClient) {
        this.buildAgentClient = buildAgentClient;
    }

    public Optional<BuildAgentClient> getBuildAgentClient() {
        return Optional.ofNullable(buildAgentClient);
    }

    public void appendToBuildLog(String message) {
        buildLogFooter.append(message);
    }

    public String getBuildLogFooter() {
        return buildLogFooter.toString();
    }
}
