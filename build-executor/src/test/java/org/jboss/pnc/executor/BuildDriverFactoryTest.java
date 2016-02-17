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
package org.jboss.pnc.executor;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.test.cdi.TestInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
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
    public void shouldSkipDriversWhichCanNotBuild() throws Exception {
        //given
        BuildDriverWhichCanNotBuild testedBuildDriver = new BuildDriverWhichCanNotBuild();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, new Configuration());

        //when
        factory.getBuildDriver(BuildType.JAVA);
    }

    @Test(expected = ExecutorException.class)
    public void shouldSkipDriversWhichAreNotMentionedInConfiguration() throws Exception {
        //given
        ProperDriver testedBuildDriver = new ProperDriver();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        Configuration configuration = new Configuration();
        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, configuration);
        factory.initConfiguration();

        //when
        factory.getBuildDriver(BuildType.JAVA);
    }

    @Test
    public void shouldPickProperDriver() throws Exception {
        //given
        ProperDriver testedBuildDriver = new ProperDriver();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        Configuration configuration = mock(Configuration.class);
        doReturn(new SystemConfig("ProperDriver", "local-build-scheduler", "10", "10")).when(configuration)
            .getModuleConfig(new PncConfigProvider<SystemConfig>(SystemConfig.class));

        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, configuration);

        //when
        BuildDriver buildDriver = factory.getBuildDriver(BuildType.JAVA);

        //then
        assertThat(buildDriver).isEqualTo(testedBuildDriver);
    }

    class BuildDriverWhichCanNotBuild implements BuildDriver {

        @Override
        public String getDriverId() {
            return null;
        }

        @Override
        public boolean canBuild(BuildType buildType) {
            return false;
        }

        @Override
        public RunningBuild startProjectBuild(
                BuildExecutionSession currentBuildExecution,
                RunningEnvironment runningEnvironment)
                throws BuildDriverException {
            return null;
        }
    }

    class ProperDriver implements BuildDriver {

        @Override
        public String getDriverId() {
            return null;
        }

        @Override
        public boolean canBuild(BuildType buildType) {
            return true;
        }

        @Override
        public RunningBuild startProjectBuild(
                BuildExecutionSession currentBuildExecution,
                RunningEnvironment runningEnvironment)
                throws BuildDriverException {
            return null;
        }
    }


}