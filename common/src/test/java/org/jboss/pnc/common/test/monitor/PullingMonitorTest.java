/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.common.test.monitor;

import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PullingMonitorTest {

    static PullingMonitor pullingMonitor;

    @BeforeClass
    public static void init() {
        pullingMonitor = new PullingMonitor();
    }

    @AfterClass
    public static void destroy() {
        pullingMonitor.destroy();
    }


    @Test
    public void monitorShouldNotifyWhenConditionIsSatisfied() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        ObjectWrapper<Boolean> notificationReceived = new ObjectWrapper<>(false);
        Runnable onComplete = () -> {
            notificationReceived.set(true);
            latch.countDown();
        };
        Consumer<Exception> onError = (e) -> {
            Assert.fail("Monitoring failed: " + e.getMessage());
        };

        ObjectWrapper<Integer> pulledWrapper = new ObjectWrapper<>(0);
        Supplier<Boolean> condition = () -> {
            Integer pulled = pulledWrapper.get();
            pulledWrapper.set(pulled + 1);
            if (pulled > 1) {
                return true;
            } else {
                return false;
            }
        };
        pullingMonitor.monitor(onComplete, onError, condition, 100, 500, TimeUnit.MILLISECONDS);

        latch.await(1, TimeUnit.SECONDS);
        Assert.assertTrue("Did not receive complete notification.", notificationReceived.get());
    }

    @Test
    public void monitorShouldTimeoutDueToUnsatisfiedCondition() throws InterruptedException {
        ObjectWrapper<Integer> pulledWrapper = new ObjectWrapper<>(0);
        Supplier<Boolean> condition = () -> {
            Integer pulled = pulledWrapper.get();
            pulledWrapper.set(pulled + 1);
            if (pulled > 8) {
                return true;
            } else {
                return false;
            }
        };
        failingMonitor(condition);
    }

    @Test
    public void monitorShouldTimeoutDueToUnresponsiveCondition() throws InterruptedException {
        Supplier<Boolean> condition = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return true;
        };
        failingMonitor(condition);
    }

    private void failingMonitor(Supplier<Boolean> condition) throws InterruptedException {
        PullingMonitor pullingMonitor = new PullingMonitor();

        CountDownLatch latch = new CountDownLatch(1);

        ObjectWrapper<Boolean> notificationReceived = new ObjectWrapper<>(false);
        Runnable onComplete = () -> {
            Assert.fail("Success should not be received.");
        };

        Consumer<Exception> onError = (e) -> {
            Assert.assertNotNull("Exception should be set.", e);
            notificationReceived.set(true);
            latch.countDown();
        };

        pullingMonitor.monitor(onComplete, onError, condition, 100, 500, TimeUnit.MILLISECONDS);

        latch.await(1, TimeUnit.SECONDS);
        Assert.assertTrue("Did not receive error notification.", notificationReceived.get());
    }
}
