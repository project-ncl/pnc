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

package org.jboss.pnc.coordinator.monitor;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.util.collection.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
    private final static Logger log = LoggerFactory.getLogger(SystemConfig.class);

    /** Time how long to wait until all services are fully up and running (in seconds) */
    private static final int DEFAULT_TIMEOUT = 300;

    /** Interval between two checks if the services are fully up and running (in second) */
    private static final int DEFAULT_CHECK_INTERVAL = 1;

    private static final int DEFAULT_MONITOR_THREAD_POOL_SIZE = 4;

    /** */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;


    private ScheduledExecutorService executorService;
    private ScheduledExecutorService timeOutVerifierService;

    private ConcurrentSet<RunningTask> runningTasks;

    @Inject
    Configuration configuration;

    public PullingMonitor() {

        int threadPoolSize = DEFAULT_MONITOR_THREAD_POOL_SIZE;

        try {
            SystemConfig config = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
            threadPoolSize = Integer.parseInt(config.getMonitorThreadPoolSize());
        } catch (ConfigurationParseException | NumberFormatException | NullPointerException e) {
            // NPE because configuration object wasn't injected
            log.warn("Couldn't read/parse Monitor Thread Pool Size from config, using default: " + threadPoolSize, e);
        }

        runningTasks = new ConcurrentSet<>();
        startTimeOutVerifierService();
        executorService = Executors.newScheduledThreadPool(threadPoolSize);

    }

    public void monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition) {
        monitor(onMonitorComplete, onMonitorError, condition, DEFAULT_CHECK_INTERVAL, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * Periodically checks the condition and calls onMonitorComplete when it returns true.
     * If timeout is reached onMonitorError is called.

     * @param onMonitorComplete
     * @param onMonitorError
     * @param condition
     * @param checkInterval
     * @param timeout
     * @param timeUnit Unit used for checkInterval and timeout
     */
    public void monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition, int checkInterval, int timeout, TimeUnit timeUnit) {
        AtomicInteger timeWaiting = new AtomicInteger(0);

        ObjectWrapper<RunningTask> runningTaskReference = new ObjectWrapper<>();
        Runnable monitor = () -> {
            RunningTask runningTask = runningTaskReference.get();
            try {
                int waiting = timeWaiting.addAndGet(checkInterval);

                // Check if given condition is satisfied
                if (condition.get()) {
                    runningTasks.remove(runningTask);
                    runningTask.cancel();
                    onMonitorComplete.run();
                }
            } catch (Exception e) {
                runningTasks.remove(runningTask);
                runningTask.cancel();
                onMonitorError.accept(e);
            }
        };
        ScheduledFuture<?> future = executorService.scheduleWithFixedDelay(monitor, 0L, checkInterval, timeUnit);
        Consumer<RunningTask> onTimeout = (runningTask) -> {
            runningTasks.remove(runningTask);
            onMonitorError.accept(new MonitorException("Service was not ready in: " + timeout + " " + timeUnit.toString()));
        };
        RunningTask runningTask = new RunningTask(future, timeout, TimeUtils.chronoUnit(timeUnit), onTimeout);
        runningTasks.add(runningTask);
        runningTaskReference.set(runningTask);
    }

    public ScheduledFuture<?> timer(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    private void startTimeOutVerifierService() {
        Runnable terminateTimedOutTasks = () -> {
            runningTasks.parallelStream()
                    .forEach(runningTask -> runningTask.terminateIfTimedOut());
        };
        timeOutVerifierService = Executors.newScheduledThreadPool(1);
        timeOutVerifierService.scheduleWithFixedDelay(terminateTimedOutTasks, 0L, 250, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
        timeOutVerifierService.shutdownNow();
    }
}
