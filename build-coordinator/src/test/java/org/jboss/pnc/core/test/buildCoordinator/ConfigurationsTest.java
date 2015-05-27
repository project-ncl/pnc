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
package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.BuildStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsTest extends ProjectBuilder {

    @Inject
    BuildCoordinator buildCoordinator;

    @Test
    @InSequence(10)
    public void dependsOnItselfConfigurationTestCase() throws Exception {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        BuildConfiguration buildConfiguration = configurationBuilder.buildConfigurationWhichDependsOnItself();

        User user = null;

        BuildTask buildTask = buildCoordinator.build(buildConfiguration, user);
        Assert.assertEquals(BuildStatus.REJECTED, buildTask.getStatus());
        Assert.assertTrue("Invalid status description: " + buildTask.getStatusDescription(), buildTask.getStatusDescription().contains("itself"));
    }

    @Test
    @InSequence(15)
    public void cycleConfigurationTestCase() throws Exception {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSetWithCycleDependency();

        User user = null;

        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, user);
        Assert.assertEquals(BuildSetStatus.REJECTED, buildSetTask.getStatus());
        Assert.assertTrue("Invalid status description: " + buildSetTask.getStatusDescription(), buildSetTask.getStatusDescription().contains("Cycle dependencies found"));
    }

}
