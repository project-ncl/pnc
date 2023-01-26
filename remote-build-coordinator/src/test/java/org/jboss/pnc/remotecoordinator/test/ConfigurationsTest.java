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
package org.jboss.pnc.remotecoordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(BuildCoordinatorDeployments.Options.WITH_DATASTORE);
    }

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Test
    public void dependsOnItselfConfigurationTestCase() throws Exception {
        try {
            configurationBuilder.buildConfigurationWhichDependsOnItself();
        } catch (PersistenceException e) {
            String message = "itself";
            Assert.assertTrue("Expected exception message to contain " + message, e.getMessage().contains(message));
            return;
        }
        Assert.fail("Did not receive expected exception.");
    }

    @Test
    public void cycleConfigurationTestCase() throws Exception {
        try {
            configurationBuilder.buildConfigurationSetWithCycleDependency();
        } catch (PersistenceException e) {
            String message = "circular reference";
            Assert.assertTrue(
                    "Expected exception message to contain [" + message + "] but it has [" + e.getMessage() + "]",
                    e.getMessage().contains(message));
            return;
        }
        Assert.fail("Did not receive expected exception.");
    }

}
