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
package org.jboss.pnc.environment.docker;

import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;

import javax.enterprise.context.ApplicationScoped;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Checks state of newly created environments and reports, when the environment is fully up
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class DockerInitializationMonitor {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** Time how long the driver waits until all services are fully up and running (in seconds) */
    private static final int MAX_CONTAINER_LOADING_TIME = 180;

    /** Interval between two checks if the services are fully up and running (in second) */
    private static final int CHECK_INTERVAL = 5;

    private static final int EXECUTOR_THREADS_COUNT = 3;

    private ScheduledExecutorService executorService; // TODO shutdown

    public DockerInitializationMonitor() {
        executorService = Executors.newScheduledThreadPool(EXECUTOR_THREADS_COUNT);
    }

    public void monitor(Runnable onMonitorComplete, Consumer<Exception> onMonitorError, String jenkinsUrl) {
        AtomicInteger timeWaiting = new AtomicInteger(0);

        ObjectWrapper<ScheduledFuture<?>> futureReference = new ObjectWrapper<>();
        Runnable monitor = () -> {
            try {
                int waiting = timeWaiting.addAndGet(CHECK_INTERVAL);

                // Wait until all services are fully up and running
                if (checkServices(jenkinsUrl)) {
                    futureReference.get().cancel(false);
                    onMonitorComplete.run();
                } else {
                    if (waiting >= MAX_CONTAINER_LOADING_TIME)
                        throw new EnvironmentDriverException(
                                "Jenkins server in container was not fully up and running in: "
                                        + MAX_CONTAINER_LOADING_TIME + " seconds");
                }
            } catch (Exception e) {
                futureReference.get().cancel(false);
                onMonitorError.accept(e);
            }
        };
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(monitor, 0L, CHECK_INTERVAL,
                TimeUnit.SECONDS);
        futureReference.set(future);
    }

    /**
     * Wait until all services in container are fully up and running
     * 
     * @param URL to jenkins server in container
     * @throws EnvironmentDriverException Thrown if the services are not initialized in time specified by variable
     *         MAX_CONTAINER_LOADING_TIME
     * @return True if all services are fully up and running otherwise false
     */
    private boolean checkServices(String jenkinsUrl) {
        try {
            HttpUtils.testResponseHttpCode(200, jenkinsUrl);
            return true;
        } catch (Exception e) {
            // Jenkins is not fully up
            logger.fine("Container services are not fully up and running. Waiting ...");
            return false;
        }
    }

}
