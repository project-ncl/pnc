/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsTestIT {

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
