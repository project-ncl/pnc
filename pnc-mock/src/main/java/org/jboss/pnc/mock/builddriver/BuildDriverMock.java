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
package org.jboss.pnc.mock.builddriver;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class BuildDriverMock implements BuildDriver {

    public static final Logger log = Logger.getLogger(BuildDriverMock.class);

    private BuildDriverStatus buildDriverStatus;

    @Override
    public String getDriverId() {
        return "termd-build-driver";
    }

    @Override
    public RunningBuild startProjectBuild(BuildExecutionSession buildExecutionSession, RunningEnvironment runningEnvironment) throws BuildDriverException {
        try {
            log.debug("Building " + buildExecutionSession.getId());
            Thread.sleep(RandomUtils.randInt(100, 300));
            setBuildDriverStatus(buildExecutionSession.getBuildExecutionConfiguration().getBuildScript());
            return new RunningBuild() {

                @Override
                public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Throwable> onError) {
                    onComplete.accept(new CompletedBuild() {
                        @Override
                        public BuildDriverResult getBuildResult() throws BuildDriverException {
                            return getBuildResultMock(runningEnvironment);
                        }

                        @Override
                        public RunningEnvironment getRunningEnvironment() {
                            return runningEnvironment;
                        }
                    });
                }

                @Override
                public RunningEnvironment getRunningEnvironment() {
                    return runningEnvironment;
                }
            };
        } catch (InterruptedException e) {
            log.error(e);
            return null;
        }
    }

    private void setBuildDriverStatus(String buildScript){
        if (buildScript.equals(TestProjectConfigurationBuilder.FAIL))
            buildDriverStatus = BuildDriverStatus.FAILED;
        else
            buildDriverStatus = BuildDriverStatus.SUCCESS;

    }

    private BuildDriverResult getBuildResultMock(final RunningEnvironment runningEnvironment) {
        return new BuildDriverResult() {
            @Override
            public String getBuildLog() throws BuildDriverException {
                return "Building in workspace ... Finished: SUCCESS";
            }

            @Override
            public BuildDriverStatus getBuildDriverStatus() {
                return buildDriverStatus;
            }
        };
    }

}
