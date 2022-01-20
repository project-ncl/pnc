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

package org.jboss.pnc.common.test.monitor;

import org.jboss.pnc.common.monitor.CancellableCompletableFuture;
import org.jboss.pnc.common.monitor.PollingMonitor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PollingMonitorTest {

    private static final Logger log = LoggerFactory.getLogger(PollingMonitorTest.class);

    static PollingMonitor pollingMonitor;

    @BeforeClass
    public static void init() {
        pollingMonitor = new PollingMonitor();
    }

    @AfterClass
    public static void destroy() {
        pollingMonitor.destroy();
    }

    @Test
    public void monitorShouldNotifyWhenConditionIsSatisfied() throws InterruptedException {
        AtomicInteger polled = new AtomicInteger(0);
        Supplier<Boolean> condition = () -> {
            log.info("Validating condition ...");
            if (polled.incrementAndGet() > 1) {
                log.info("Satisfied.");
                return true;
            } else {
                return false;
            }
        };
        CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(condition, 100, 500, TimeUnit.MILLISECONDS);
        monitor.exceptionally(t -> {
            Assert.fail("Monitoring failed: " + t.getMessage());
            return null;
        });
        try {
            monitor.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            Assert.fail("Did not receive complete notification." + e.getMessage());
        }
        Assert.assertEquals(2, polled.get());
    }

    @Test
    public void monitorShouldTimeoutDueToUnsatisfiedCondition() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);
        AtomicInteger polled = new AtomicInteger(0);
        Supplier<Boolean> condition = () -> {
            log.info("Validating condition ...");
            polled.incrementAndGet();
            return false;
        };
        CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(condition, 100, 500, TimeUnit.MILLISECONDS);
        monitor.exceptionally(t -> {
            log.info("Handling exception: " + t.getMessage());
            lock.countDown();
            return null;
        });
        lock.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(5, polled.get());
    }

    @Test
    public void monitorShouldTimeoutDueToUnresponsiveCondition() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);
        AtomicInteger polled = new AtomicInteger(0);
        Supplier<Boolean> condition = () -> {
            log.info("Validating condition ...");
            polled.incrementAndGet();
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                log.info(e.getMessage());
            }
            return true;
        };
        CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(condition, 100, 500, TimeUnit.MILLISECONDS);
        monitor.exceptionally(t -> {
            log.info("Handling exception: " + t.getMessage());
            lock.countDown();
            return null;
        });
        lock.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(1, polled.get());
    }

    @Test
    public void shouldCompleteExceptionally() throws InterruptedException {
        CountDownLatch lock = new CountDownLatch(1);
        Supplier<Boolean> condition = () -> {
            log.info("Validating condition ...");
            throw new RuntimeException("bam");
        };
        CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(condition, 100, 500, TimeUnit.MILLISECONDS);
        monitor.exceptionally(t -> {
            log.info("Handling exception: " + t.getMessage());
            if (t.getMessage().equals("bam")) {
                lock.countDown();
            }
            return null;
        });
        boolean await = lock.await(1, TimeUnit.SECONDS);
        Assert.assertTrue("Did not received the exception.", await);
    }
}
