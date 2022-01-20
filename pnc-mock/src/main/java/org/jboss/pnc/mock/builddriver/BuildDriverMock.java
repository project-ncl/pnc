/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class BuildDriverMock extends BuildDriverBase implements BuildDriver {

    public static final Logger log = LoggerFactory.getLogger(BuildDriverMock.class);

    @Override
    public String getDriverId() {
        return "termd-build-driver";
    }

    protected RunningBuild createRunningBuild(final RunningEnvironment runningEnvironment) {
        return new RunningBuild() {

            @Override
            public RunningEnvironment getRunningEnvironment() {
                return runningEnvironment;
            }

            @Override
            public void cancel() {
                throw new UnsupportedOperationException("Not implmented in mock driver");
            }
        };
    }

    protected void complete(
            BuildExecutionSession buildExecutionSession,
            final RunningEnvironment runningEnvironment,
            Consumer<CompletedBuild> onComplete) throws InterruptedException {
        Thread.sleep(RandomUtils.randInt(100, 300));
        setBuildStatus(buildExecutionSession.getBuildExecutionConfiguration().getBuildScript());

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

}
