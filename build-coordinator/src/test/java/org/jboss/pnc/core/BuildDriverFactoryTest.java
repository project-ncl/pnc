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
package org.jboss.pnc.core;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.BuildDriverRouterModuleConfig;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BuildDriverFactoryTest {

    @Test(expected = CoreException.class)
    public void shouldSkipDriversWhichCanNotBuild() throws Exception {
        //given
        BuildDriverWhichCanNotBuild testedBuildDriver = new BuildDriverWhichCanNotBuild();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        BuildDriverFactory factory = new BuildDriverFactory(allDrivers, new Configuration());

        //when
        factory.getBuildDriver(BuildType.JAVA);
    }

    @Test(expected = CoreException.class)
    public void shouldSkipDriversWhichAreNotMentionedInConfiguration() throws Exception {
        //given
        ProperDriver testedBuildDriver = new ProperDriver();
        TestInstance<BuildDriver> allDrivers = new TestInstance<>(testedBuildDriver);

        Configuration configuration = mock(Configuration.class);
        doReturn(new BuildDriverRouterModuleConfig("not me")).when(configuration).getModuleConfig(BuildDriverRouterModuleConfig.class);

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
        doReturn(new BuildDriverRouterModuleConfig("ProperDriver")).when(configuration).getModuleConfig(BuildDriverRouterModuleConfig.class);

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
        public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RunningEnvironment runningEnvironment)
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
        public RunningBuild startProjectBuild(BuildConfiguration buildConfiguration, RunningEnvironment runningEnvironment)
                throws BuildDriverException {
            return null;
        }
    }


}