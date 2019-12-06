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

package org.jboss.pnc.common.monitor;

import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.common.util.ReadEnvProperty;
import org.jboss.pnc.common.util.TimeUtils;
import org.jboss.util.collection.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@ApplicationScoped
public class PullingMonitor {
    private static final Logger log = LoggerFactory.getLogger(PullingMonitor.class);

    /** Time how long to wait until all services are fully up and running (in seconds) */
    private static final int DEFAULT_TIMEOUT = 300;
    private static final String PULLING_MONITOR_TIMEOUT_KEY = "pulling_monitor_timeout";

    /** Interval between two checks if the services are fully up and running (in second) */
    private static final int DEFAULT_CHECK_INTERVAL = 1;
    private static final String PULLING_MONITOR_CHECK_INTERVAL_KEY = "pulling_monitor_check_interval";

    /** */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private static final String PULLING_MONITOR_THREADPOOL_KEY = "pulling_monitor_threadpool";
    private static final int DEFAULT_EXECUTOR_THREADPOOL_SIZE = 4;

    private ScheduledExecutorService executorService;
    private ScheduledExecutorService timeOutVerifierService;

    private ConcurrentSet<RunningTask> runningTasks;

    private int timeout;
    private int checkInterval;

    public PullingMonitor() {

        ReadEnvProperty reader = new ReadEnvProperty();

        timeout = reader.getIntValueFromPropertyOrDefault(PULLING_MONITOR_TIMEOUT_KEY, DEFAULT_TIMEOUT);
        checkInterval = reader.getIntValueFromPropertyOrDefault(PULLING_MONITOR_CHECK_INTERVAL_KEY, DEFAULT_CHECK_INTERVAL);

        int threadSize = reader.getIntValueFromPropertyOrDefault(PULLING_MONITOR_THREADPOOL_KEY, DEFAULT_EXECUTOR_THREADPOOL_SIZE);

        runningTasks = new ConcurrentSet<>();
        startTimeOutVerifierService();
        executorService = MDCExecutors.newScheduledThreadPool(threadSize);
    }

    public RunningTask monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition) {
        return monitor(onMonitorComplete, onMonitorError, condition, checkInterval, timeout, DEFAULT_TIME_UNIT);
    }

    /**
     * Periodically checks the condition and calls onMonitorComplete when it returns true.
     * If timeout is reached onMonitorError is called.
     *
     * @param onMonitorComplete
     * @param onMonitorError
     * @param condition
     * @param checkInterval
     * @param timeout
     * @param timeUnit Unit used for checkInterval and timeout
     */
    public RunningTask monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, Supplier<Boolean> condition, int checkInterval, int timeout, TimeUnit timeUnit) {

        ObjectWrapper<RunningTask> runningTaskReference = new ObjectWrapper<>();
        Runnable monitor = () -> {
            RunningTask runningTask = runningTaskReference.get();

            if (runningTask == null) {
                /* There might be a situation where this runnable is called before runningTaskReference is set with the
                 * running task since this runnable is scheduled to run before runningTaskReference is set. In that
                 * case, we just skip this runnable and re-run on the next scheduled interval with the assumption that
                 * runningTaskReference will be set before the next re-run
                 */
                log.debug("runningTask not set yet inside the 'monitor' runnable! Skipping!");
                return;
            }
            try {
                // Check if given condition is satisfied
                if (condition.get()) {
                    onMonitorComplete.run();
                    runningTasks.remove(runningTask);
                    runningTask.cancel();
                }
            } catch (Exception e) {
                log.error("Exception in monitor runnable", e);
                try {
                    onMonitorError.accept(e);
                } catch (Exception e1) {
                    log.error("Error while notifying error.", e1);
                }
                runningTasks.remove(runningTask);
                runningTask.cancel();
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

        return runningTask;
    }

    public void cancelRunningTask(RunningTask task) {
        runningTasks.remove(task);
        task.cancel();
    }



    public ScheduledFuture<?> timer(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    private void startTimeOutVerifierService() {
        Runnable terminateTimedOutTasks = () -> {
            runningTasks.parallelStream()
                    .forEach(runningTask -> runningTask.terminateIfTimedOut());
        };
        timeOutVerifierService = MDCExecutors.newScheduledThreadPool(1);
        timeOutVerifierService.scheduleWithFixedDelay(terminateTimedOutTasks, 0L, 250, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
        timeOutVerifierService.shutdownNow();
    }

}
