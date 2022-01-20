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

package org.jboss.pnc.common.monitor;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.util.ReadEnvProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class PollingMonitor {
    private static final Logger log = LoggerFactory.getLogger(PollingMonitor.class);

    /** Time how long to wait until all services are fully up and running (in seconds) */
    public static final int DEFAULT_TIMEOUT = 300;

    /** Interval between two checks if the services are fully up and running (in second) */
    public static final int DEFAULT_CHECK_INTERVAL = 1;

    /** */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private static final String POLLING_MONITOR_THREADPOOL_KEY = "polling_monitor_threadpool";
    private static final int DEFAULT_EXECUTOR_THREADPOOL_SIZE = 4;

    private ScheduledExecutorService executorService;
    private ScheduledExecutorServiceWithTimeout scheduledExecutor;

    public PollingMonitor() {
        ReadEnvProperty reader = new ReadEnvProperty();

        int threadSize = reader
                .getIntValueFromPropertyOrDefault(POLLING_MONITOR_THREADPOOL_KEY, DEFAULT_EXECUTOR_THREADPOOL_SIZE);

        executorService = MDCExecutors.newScheduledThreadPool(threadSize);
        scheduledExecutor = new ScheduledExecutorServiceWithTimeout(executorService);
    }

    /**
     * Periodically checks the condition and calls onMonitorComplete when it returns true. If timeout is reached
     * onMonitorError is called.
     *
     * @param condition
     * @return CancellableCompletableFuture
     */
    public CancellableCompletableFuture<Void> monitor(Supplier<Boolean> condition) {
        return monitor(condition, DEFAULT_CHECK_INTERVAL, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * Periodically checks the condition and calls onMonitorComplete when it returns true. If the specified timeout is
     * reached onMonitorError is called.
     *
     * @param condition the condition to check
     * @param checkInterval interval between checks
     * @param timeout
     * @param timeUnit
     * 
     * @return CancellableCompletableFuture
     */
    public CancellableCompletableFuture<Void> monitor(
            Supplier<Boolean> condition,
            int checkInterval,
            int timeout,
            TimeUnit timeUnit) {
        log.debug(
                "Monitoring condition with specified checkInterval of {}, timeout of {}, timeUnit {}",
                checkInterval,
                timeout,
                timeUnit);
        return scheduledExecutor.scheduleWithFixedDelayAndTimeout(condition, 0L, checkInterval, timeout, timeUnit);
    }

    public ScheduledFuture<?> timer(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

}
