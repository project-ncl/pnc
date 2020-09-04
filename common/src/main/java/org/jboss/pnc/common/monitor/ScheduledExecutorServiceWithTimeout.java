/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ScheduledExecutorServiceWithTimeout {

    private ScheduledExecutorService executorService;

    public ScheduledExecutorServiceWithTimeout(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public CancellableCompletableFuture<Void> scheduleWithFixedDelayAndTimeout(
            Supplier<Boolean> condition,
            long initialDelay,
            long delay,
            long timeout,
            TimeUnit timeUnit) {
        Task task = new Task();

        CancellableCompletableFuture<Void> completableFuture = new CancellableCompletableFuture<>(task::cancel);

        Runnable run = () -> {
            try {
                if (condition.get()) {
                    task.cancel();
                    completableFuture.complete(null);
                }
            } catch (Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        };

        task.setRunnable(run);

        Runnable selfTimeout = () -> {
            task.cancel();
            completableFuture.completeExceptionally(
                    new TimeoutException("Condition was not satisfied in: " + timeout + " " + timeUnit.toString()));
        };
        ScheduledFuture<?> timeoutFuture = executorService.schedule(selfTimeout, timeout, timeUnit);
        task.setTimeoutFuture(timeoutFuture);

        ScheduledFuture<?> scheduledFuture = executorService
                .scheduleWithFixedDelay(task, initialDelay, delay, timeUnit);
        task.setTaskFuture(scheduledFuture);

        return completableFuture;
    }

    private static class Task implements Runnable {

        private Runnable runnable;

        private ScheduledFuture<?> timeoutFuture;

        private ScheduledFuture<?> taskFuture;

        private boolean cancelled;

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                // if timeout occurred before the taskFuture has been set
                if (!cancelled) {
                    runnable.run();
                } else {
                    cancel();
                }
            } catch (Exception e) {
                timeoutFuture.cancel(true);
                throw e;
            }
        }

        public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
            this.timeoutFuture = timeoutFuture;
        }

        public void setTaskFuture(ScheduledFuture<?> taskFuture) {
            this.taskFuture = taskFuture;
        }

        public void cancel() {
            cancelled = true;
            if (taskFuture != null) {
                taskFuture.cancel(false);
            }
            if (timeoutFuture != null) {
                timeoutFuture.cancel(true);
            }
        }
    }
}
