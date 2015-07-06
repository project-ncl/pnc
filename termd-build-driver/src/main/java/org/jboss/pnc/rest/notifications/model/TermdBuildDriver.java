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
package org.jboss.pnc.rest.notifications.model;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.*;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

@ApplicationScoped
public class TermdBuildDriver implements BuildDriver {

    public static final String DRIVER_ID = "TermdBuildDriver";

    private static final Logger log = Logger.getLogger(TermdBuildDriver.class);

    public TermdBuildDriver() {
    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return false;
    }

    @Override
    public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, final RunningEnvironment runningEnvironment)
            throws BuildDriverException {

        return new RunningBuild() {

            @Override
            public void monitor(Consumer<CompletedBuild> onComplete, Consumer<Exception> onError) {
                onComplete.accept(new CompletedBuild() {
                    @Override
                    public BuildDriverResult getBuildResult() throws BuildDriverException {
                        return new BuildDriverResult() {
                            @Override
                            public String getBuildLog() throws BuildDriverException {
                                return "Logs... logs everywhere...";
                            }

                            @Override
                            public BuildDriverStatus getBuildDriverStatus() {
                                return BuildDriverStatus.SUCCESS;
                            }

                            @Override
                            public RunningEnvironment getRunningEnvironment() {
                                return runningEnvironment;
                            }
                        };
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
    }

}
