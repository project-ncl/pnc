/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex.setup.arquillian;

import java.lang.reflect.Method;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;

public class LifecycleObserver {

    public void executeBeforeDeploy(
            @Observes org.jboss.arquillian.container.spi.event.container.BeforeDeploy event,
            TestClass testClass) {
        execute(testClass.getMethods(BeforeDeploy.class));
    }

    public void executeAfterDeploy(
            @Observes org.jboss.arquillian.container.spi.event.container.AfterDeploy event,
            TestClass testClass) {
        execute(testClass.getMethods(AfterDeploy.class));
    }

    public void executeBeforeUnDeploy(
            @Observes org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy event,
            TestClass testClass) {
        execute(testClass.getMethods(BeforeUnDeploy.class));
    }

    public void executeAfterUnDeploy(
            @Observes org.jboss.arquillian.container.spi.event.container.AfterUnDeploy event,
            TestClass testClass) {
        execute(testClass.getMethods(AfterUnDeploy.class));
    }

    private void execute(Method[] methods) {
        if (methods == null) {
            return;
        }
        for (Method method : methods) {
            try {
                method.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("Could not execute method: " + method, e);
            }
        }
    }
}
