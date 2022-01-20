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
package org.jboss.pnc.executor;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.cdi.TestInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BuildDriverFactoryTest {

    private static String backupConfigPath;

    @BeforeClass
    public static void setUpTestConfigPath() {
        backupConfigPath = System.getProperty("pnc-config-file");
        System.setProperty("pnc-config-file", "testConfig.json");
    }

    @AfterClass
    public static void restoreConfigPath() {
        if (backupConfigPath != null)
            System.setProperty("pnc-config-file", backupConfigPath);
        else
            System.getProperties().remove("pnc-config-file");
    }

    @Test(expected = ExecutorException.class)
    public void shouldSkipDriversWhichAreNotMentionedInConfiguration() throws Exception {
        // given
        ProperDriver testedBuildDriver = new ProperDriver();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        Configuration configuration = new Configuration();
        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, configuration);
        factory.initConfiguration();

        // when
        factory.getBuildDriver();
    }

    @Test
    public void shouldPickProperDriver() throws Exception {
        // given
        ProperDriver testedBuildDriver = new ProperDriver();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        Configuration configuration = mock(Configuration.class);

        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, configuration);

        // when
        BuildDriver buildDriver = factory.getBuildDriver();

        // then
        assertThat(buildDriver).isEqualTo(testedBuildDriver);
    }

    class BuildDriverWhichCanNotBuild implements BuildDriver {

        @Override
        public String getDriverId() {
            return null;
        }

        @Override
        public RunningBuild startProjectBuild(
                BuildExecutionSession currentBuildExecution,
                RunningEnvironment runningEnvironment,
                Consumer<CompletedBuild> onComplete,
                Consumer<Throwable> onError) {
            return null;
        }
    }

    class ProperDriver implements BuildDriver {

        @Override
        public String getDriverId() {
            return null;
        }

        @Override
        public RunningBuild startProjectBuild(
                BuildExecutionSession currentBuildExecution,
                RunningEnvironment runningEnvironment,
                Consumer<CompletedBuild> onComplete,
                Consumer<Throwable> onError) {
            return null;
        }
    }

}