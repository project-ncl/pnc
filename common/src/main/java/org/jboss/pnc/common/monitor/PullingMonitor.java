/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.util.ObjectWrapper;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class PullingMonitor {

    /** Time how long to wait until all services are fully up and running (in seconds) */
    private static final int DEFAULT_TIMEOUT = 30;

    /** Interval between two checks if the services are fully up and running (in second) */
    private static final int DEFAULT_CHECK_INTERVAL = 1;

    private ScheduledExecutorService executorService;

    public PullingMonitor() {
        executorService = Executors.newScheduledThreadPool(4); //TODO configurable
    }

    public void monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition) {
        monitor(onMonitorComplete, onMonitorError, condition, DEFAULT_CHECK_INTERVAL, DEFAULT_TIMEOUT);
    }

    /**
     * Periodically checks the condition and calls onMonitorComplete when it returns true.
     * An exception is thrown if timeout is reached.
     *
     * @param onMonitorComplete
     * @param onMonitorError
     * @param condition
     * @param checkInterval
     * @param timeout
     */
    public void monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition, int checkInterval, int timeout) {
        AtomicInteger timeWaiting = new AtomicInteger(0);

        ObjectWrapper<ScheduledFuture<?>> futureReference = new ObjectWrapper<>();
        Runnable monitor = () -> {
            try {
                int waiting = timeWaiting.addAndGet(checkInterval);

                // Check if given condition is satisfied
                if (condition.get()) {
                    futureReference.get().cancel(false);
                    onMonitorComplete.run();
                } else {
                    if (waiting >= timeout) {
                        throw new MonitorException( "Service was not ready in: " + timeout + " seconds");
                    }
                }
            } catch (Exception e) {
                futureReference.get().cancel(false);
                onMonitorError.accept(e);
            }
        };
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(monitor, 0L, checkInterval, TimeUnit.SECONDS);
        futureReference.set(future);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }
}
